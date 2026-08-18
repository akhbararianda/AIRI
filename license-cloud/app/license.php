<?php
declare(strict_types=1);
require_once __DIR__ . '/bootstrap.php';

function airi_b64url_decode(string $s): string|false {
    $s = strtr($s, '-_', '+/');
    $pad = strlen($s) % 4;
    if ($pad) $s .= str_repeat('=', 4-$pad);
    return base64_decode($s, true);
}

function airi_verify_license(string $key, string $machineId): array {
    $parts = explode('.', trim($key));
    if (count($parts) !== 3 || $parts[0] !== 'AIRI1') return ['ok'=>false,'error'=>'invalid_format'];
    $payloadRaw = airi_b64url_decode($parts[1]);
    $signature = airi_b64url_decode($parts[2]);
    if ($payloadRaw === false || $signature === false) return ['ok'=>false,'error'=>'invalid_encoding'];
    $payload = json_decode($payloadRaw, true);
    if (!is_array($payload)) return ['ok'=>false,'error'=>'invalid_payload'];
    $pub = @openssl_pkey_get_public((string)file_get_contents((string)airi_config()['public_key']));
    if ($pub === false) throw new RuntimeException('AIRI public key could not be loaded.');
    $verified = openssl_verify($payloadRaw, $signature, $pub, OPENSSL_ALGO_SHA256) === 1;
    if (!$verified) return ['ok'=>false,'error'=>'invalid_signature'];
    if (($payload['product'] ?? '') !== 'AIRI-DM') return ['ok'=>false,'error'=>'wrong_product'];
    $binding = strtoupper(airi_text($payload['machine'] ?? '', 80));
    $machineId = strtoupper(airi_text($machineId, 80));
    if ($binding !== '*' && !hash_equals($binding, $machineId)) return ['ok'=>false,'error'=>'wrong_machine'];
    $expiry = (int)($payload['expiry'] ?? 0);
    if ($expiry > 0 && time() > $expiry) return ['ok'=>false,'error'=>'expired','payload'=>$payload];
    return [
        'ok'=>true,
        'payload'=>$payload,
        'license_hash'=>hash('sha256', trim($key)),
        'machine_hash'=>hash('sha256', $machineId),
        'binding_hash'=>$binding === '*' ? null : hash('sha256', $binding),
        'expiry'=>$expiry,
    ];
}

function airi_max_devices(string $edition): int {
    return match (strtolower($edition)) {
        'business' => 3,
        'enterprise' => 20,
        default => 1,
    };
}

function airi_install_upsert(string $installationId, string $machineHash, array $d): array {
    $pdo = airi_db();
    $q = $pdo->prepare('SELECT * FROM installations WHERE installation_id=? LIMIT 1');
    $q->execute([$installationId]);
    $existing = $q->fetch();
    if ($existing && !hash_equals((string)$existing['machine_hash'], $machineHash)) {
        airi_event($installationId, null, 'cloned_installation_id', 'critical', 90, ['previous_machine_hash'=>substr((string)$existing['machine_hash'],0,12),'new_machine_hash'=>substr($machineHash,0,12)]);
        $u = $pdo->prepare("UPDATE installations SET license_state='blocked',last_seen=UTC_TIMESTAMP(),last_ip_hash=? WHERE installation_id=?");
        $u->execute([airi_ip_hash(),$installationId]);
        return ['ok'=>false,'blocked'=>true,'reason'=>'installation_clone'];
    }
    $state = strtolower(airi_text($d['license_state'] ?? 'unlicensed', 24));
    if (!in_array($state,['trial','licensed','expired','unlicensed','blocked'],true)) $state='unlicensed';
    $appVersion = airi_text($d['app_version'] ?? '', 32);
    $osVersion = airi_text($d['os_version'] ?? '', 128);
    $arch = airi_text($d['architecture'] ?? '', 32);
    $channel = airi_text($d['channel'] ?? 'commercial', 32);
    $sql = "INSERT INTO installations (installation_id,machine_hash,app_version,os_version,architecture,channel,license_state,last_seen,last_heartbeat,last_ip_hash)
            VALUES (?,?,?,?,?,?,?,UTC_TIMESTAMP(),UTC_TIMESTAMP(),?)
            ON DUPLICATE KEY UPDATE app_version=VALUES(app_version),os_version=VALUES(os_version),architecture=VALUES(architecture),channel=VALUES(channel),license_state=IF(license_state='blocked','blocked',VALUES(license_state)),last_seen=UTC_TIMESTAMP(),last_heartbeat=UTC_TIMESTAMP(),last_ip_hash=VALUES(last_ip_hash)";
    $pdo->prepare($sql)->execute([$installationId,$machineHash,$appVersion,$osVersion,$arch,$channel,$state,airi_ip_hash()]);
    return ['ok'=>true,'blocked'=>false];
}

function airi_api_register(array $d): array {
    $installationId = airi_installation_id($d['installation_id'] ?? '');
    $machineHash = airi_hex64($d['machine_hash'] ?? '');
    if ($installationId === '' || $machineHash === '') return ['http'=>400,'body'=>['ok'=>false,'error'=>'invalid_device_identity']];
    $up = airi_install_upsert($installationId,$machineHash,$d);
    if (!$up['ok']) return ['http'=>403,'body'=>['ok'=>false,'allowed'=>false,'reason'=>$up['reason'],'heartbeat_seconds'=>900,'offline_grace_seconds'=>604800]];
    airi_event($installationId,null,'install_checkin','low',0,['app_version'=>airi_text($d['app_version'] ?? '',32)]);
    return ['http'=>200,'body'=>['ok'=>true,'allowed'=>true,'heartbeat_seconds'=>3600,'offline_grace_seconds'=>604800,'server_time'=>time()]];
}

function airi_api_activate(array $d): array {
    $installationId = airi_installation_id($d['installation_id'] ?? '');
    $machineId = strtoupper(airi_text($d['machine_id'] ?? '', 80));
    $key = trim((string)($d['license_key'] ?? ''));
    if ($installationId === '' || !preg_match('/^AIRI-[A-F0-9-]{10,}$/', $machineId) || strlen($key) < 40) {
        return ['http'=>400,'body'=>['ok'=>false,'allowed'=>false,'error'=>'invalid_activation_request']];
    }
    $machineHash = hash('sha256',$machineId);
    $up = airi_install_upsert($installationId,$machineHash,$d + ['license_state'=>'licensed']);
    if (!$up['ok']) return ['http'=>403,'body'=>['ok'=>false,'allowed'=>false,'reason'=>$up['reason']]];
    $verify = airi_verify_license($key,$machineId);
    if (!$verify['ok']) {
        $risk = in_array($verify['error'],['invalid_signature','wrong_machine'],true) ? 50 : 20;
        airi_event($installationId,null,'activation_rejected','high',$risk,['reason'=>$verify['error']]);
        return ['http'=>403,'body'=>['ok'=>false,'allowed'=>false,'reason'=>$verify['error']]];
    }
    $payload = $verify['payload'];
    $edition = airi_text($payload['edition'] ?? 'Pro',64);
    $customer = airi_text($payload['customer'] ?? '',255);
    $expiry = (int)$verify['expiry'];
    $expiryAt = $expiry > 0 ? gmdate('Y-m-d H:i:s',$expiry) : null;
    $maxDevices = airi_max_devices($edition);
    $pdo = airi_db();
    $pdo->beginTransaction();
    try {
        $insert = $pdo->prepare("INSERT INTO licenses (license_hash,customer,edition,status,expiry_at,machine_binding_hash,max_devices,first_activated_at,last_seen)
            VALUES (?,?,?,'active',?,?,?,UTC_TIMESTAMP(),UTC_TIMESTAMP())
            ON DUPLICATE KEY UPDATE customer=VALUES(customer),edition=VALUES(edition),expiry_at=VALUES(expiry_at),machine_binding_hash=VALUES(machine_binding_hash),last_seen=UTC_TIMESTAMP()");
        $insert->execute([$verify['license_hash'],$customer,$edition,$expiryAt,$verify['binding_hash'],$maxDevices]);
        $q=$pdo->prepare('SELECT * FROM licenses WHERE license_hash=? FOR UPDATE');$q->execute([$verify['license_hash']]);$license=$q->fetch();
        if (!$license) throw new RuntimeException('license row missing');
        $licenseId=(int)$license['id'];
        if (in_array($license['status'],['suspended','revoked','expired'],true)) {
            $pdo->rollBack();
            airi_event($installationId,$licenseId,'activation_blocked_by_status','high',25,['status'=>$license['status']]);
            return ['http'=>403,'body'=>['ok'=>false,'allowed'=>false,'reason'=>'license_'.$license['status']]];
        }
        if (!empty($license['expiry_at']) && strtotime((string)$license['expiry_at'].' UTC') < time()) {
            $pdo->prepare("UPDATE licenses SET status='expired' WHERE id=?")->execute([$licenseId]);
            $pdo->commit();
            return ['http'=>403,'body'=>['ok'=>false,'allowed'=>false,'reason'=>'license_expired']];
        }
        $dq=$pdo->prepare('SELECT * FROM license_devices WHERE license_id=? AND machine_hash=? LIMIT 1');$dq->execute([$licenseId,$machineHash]);$device=$dq->fetch();
        if ($device && $device['status']==='revoked') {
            $pdo->rollBack();
            airi_event($installationId,$licenseId,'revoked_device_activation','critical',60,[]);
            return ['http'=>403,'body'=>['ok'=>false,'allowed'=>false,'reason'=>'device_revoked']];
        }
        if (!$device) {
            $cq=$pdo->prepare("SELECT COUNT(*) FROM license_devices WHERE license_id=? AND status='active'");$cq->execute([$licenseId]);$active=(int)$cq->fetchColumn();
            if ($active >= (int)$license['max_devices']) {
                $pdo->rollBack();
                airi_event($installationId,$licenseId,'device_limit_exceeded','critical',65,['active_devices'=>$active,'max_devices'=>(int)$license['max_devices']]);
                return ['http'=>403,'body'=>['ok'=>false,'allowed'=>false,'reason'=>'device_limit','max_devices'=>(int)$license['max_devices']]];
            }
            $pdo->prepare("INSERT INTO license_devices (license_id,installation_id,machine_hash,status) VALUES (?,?,?,'active')")->execute([$licenseId,$installationId,$machineHash]);
        } else {
            $pdo->prepare("UPDATE license_devices SET installation_id=?,status='active',last_seen=UTC_TIMESTAMP() WHERE id=?")->execute([$installationId,(int)$device['id']]);
        }
        $pdo->prepare("UPDATE licenses SET activation_count=activation_count+1,last_seen=UTC_TIMESTAMP() WHERE id=?")->execute([$licenseId]);
        $pdo->prepare("UPDATE installations SET license_state='licensed',last_seen=UTC_TIMESTAMP() WHERE installation_id=?")->execute([$installationId]);
        $pdo->commit();
        airi_event($installationId,$licenseId,'activation_success','low',0,['edition'=>$edition]);
        $aq=$pdo->prepare("SELECT COUNT(*) FROM security_events WHERE license_id=? AND event_type='activation_success' AND created_at>=DATE_SUB(UTC_TIMESTAMP(),INTERVAL 10 MINUTE)");$aq->execute([$licenseId]);$recent=(int)$aq->fetchColumn();
        if ($recent > 5) airi_event($installationId,$licenseId,'activation_burst','high',25,['activations_10m'=>$recent]);
        return ['http'=>200,'body'=>['ok'=>true,'allowed'=>true,'license_hash'=>$verify['license_hash'],'status'=>'active','edition'=>$edition,'customer'=>$customer,'heartbeat_seconds'=>3600,'offline_grace_seconds'=>604800,'server_time'=>time()]];
    } catch (Throwable $e) {
        if ($pdo->inTransaction()) $pdo->rollBack();
        throw $e;
    }
}

function airi_api_heartbeat(array $d): array {
    $installationId = airi_installation_id($d['installation_id'] ?? '');
    $machineHash = airi_hex64($d['machine_hash'] ?? '');
    if ($installationId==='' || $machineHash==='') return ['http'=>400,'body'=>['ok'=>false,'error'=>'invalid_device_identity']];
    $up=airi_install_upsert($installationId,$machineHash,$d);
    if (!$up['ok']) return ['http'=>403,'body'=>['ok'=>false,'allowed'=>false,'reason'=>$up['reason'],'offline_grace_seconds'=>604800]];
    $licenseHash=airi_hex64($d['license_hash'] ?? '');
    if ($licenseHash==='') return ['http'=>200,'body'=>['ok'=>true,'allowed'=>true,'status'=>'trial_or_unlicensed','heartbeat_seconds'=>3600,'offline_grace_seconds'=>604800,'server_time'=>time()]];
    $pdo=airi_db();
    $q=$pdo->prepare('SELECT * FROM licenses WHERE license_hash=? LIMIT 1');$q->execute([$licenseHash]);$license=$q->fetch();
    if (!$license) {
        airi_event($installationId,null,'licensed_client_unknown_to_cloud','medium',10,['license_hash_prefix'=>substr($licenseHash,0,12)]);
        return ['http'=>200,'body'=>['ok'=>true,'allowed'=>true,'status'=>'unknown_license','heartbeat_seconds'=>900,'offline_grace_seconds'=>604800,'server_time'=>time()]];
    }
    $licenseId=(int)$license['id'];
    if (!empty($license['expiry_at']) && strtotime((string)$license['expiry_at'].' UTC') < time() && $license['status']==='active') {
        $pdo->prepare("UPDATE licenses SET status='expired' WHERE id=?")->execute([$licenseId]);$license['status']='expired';
    }
    if (in_array($license['status'],['suspended','revoked','expired'],true)) {
        airi_event($installationId,$licenseId,'heartbeat_denied','high',5,['status'=>$license['status']]);
        return ['http'=>403,'body'=>['ok'=>true,'allowed'=>false,'status'=>$license['status'],'reason'=>'license_'.$license['status'],'heartbeat_seconds'=>900,'offline_grace_seconds'=>604800,'server_time'=>time()]];
    }
    $dq=$pdo->prepare('SELECT * FROM license_devices WHERE license_id=? AND machine_hash=? LIMIT 1');$dq->execute([$licenseId,$machineHash]);$device=$dq->fetch();
    if ($device && $device['status']==='revoked') return ['http'=>403,'body'=>['ok'=>true,'allowed'=>false,'status'=>'revoked','reason'=>'device_revoked','heartbeat_seconds'=>900,'offline_grace_seconds'=>604800,'server_time'=>time()]];
    if (!$device) {
        airi_event($installationId,$licenseId,'unregistered_device_heartbeat','high',35,[]);
        return ['http'=>403,'body'=>['ok'=>true,'allowed'=>false,'status'=>'review','reason'=>'device_not_activated','heartbeat_seconds'=>900,'offline_grace_seconds'=>604800,'server_time'=>time()]];
    }
    $pdo->prepare('UPDATE license_devices SET installation_id=?,last_seen=UTC_TIMESTAMP() WHERE id=?')->execute([$installationId,(int)$device['id']]);
    $pdo->prepare('UPDATE licenses SET last_seen=UTC_TIMESTAMP() WHERE id=?')->execute([$licenseId]);
    $pdo->prepare("UPDATE installations SET license_state='licensed',last_seen=UTC_TIMESTAMP(),last_heartbeat=UTC_TIMESTAMP() WHERE installation_id=?")->execute([$installationId]);
    return ['http'=>200,'body'=>['ok'=>true,'allowed'=>true,'status'=>'active','edition'=>$license['edition'],'heartbeat_seconds'=>3600,'offline_grace_seconds'=>604800,'risk_score'=>(int)$license['risk_score'],'server_time'=>time()]];
}

function airi_api_deactivate(array $d): array {
    $installationId=airi_installation_id($d['installation_id'] ?? '');
    $licenseHash=airi_hex64($d['license_hash'] ?? '');
    if ($installationId==='' || $licenseHash==='') return ['http'=>400,'body'=>['ok'=>false,'error'=>'invalid_request']];
    $pdo=airi_db();$q=$pdo->prepare('SELECT id FROM licenses WHERE license_hash=?');$q->execute([$licenseHash]);$licenseId=(int)$q->fetchColumn();
    if ($licenseId<=0) return ['http'=>200,'body'=>['ok'=>true]];
    $pdo->prepare("UPDATE license_devices SET status='inactive',last_seen=UTC_TIMESTAMP() WHERE license_id=? AND installation_id=? AND status='active'")->execute([$licenseId,$installationId]);
    airi_event($installationId,$licenseId,'device_deactivated','low',0,[]);
    return ['http'=>200,'body'=>['ok'=>true]];
}

function airi_api_security_event(array $d): array {
    $installationId=airi_installation_id($d['installation_id'] ?? '');
    if ($installationId==='') return ['http'=>400,'body'=>['ok'=>false,'error'=>'invalid_installation_id']];
    $type=airi_text($d['event_type'] ?? '',80);
    $map=[
        'invalid_license'=>['high',40], 'clock_rollback'=>['high',35], 'integrity_failure'=>['critical',80],
        'debugger_detected'=>['high',50], 'cloud_denied'=>['medium',10], 'version_mismatch'=>['medium',10],
        'unexpected_license_state'=>['medium',15], 'trial_tamper'=>['critical',70]
    ];
    if (!isset($map[$type])) return ['http'=>400,'body'=>['ok'=>false,'error'=>'unsupported_event_type']];
    [$severity,$risk]=$map[$type];
    $meta=is_array($d['metadata'] ?? null)?$d['metadata']:[];
    airi_event($installationId,null,$type,$severity,$risk,$meta);
    return ['http'=>202,'body'=>['ok'=>true]];
}
