<?php
declare(strict_types=1);
require_once __DIR__ . '/bootstrap.php';

function airi_integrity_report(string $installationId, string $binaryHash): array {
    $binaryHash = airi_hex64($binaryHash);
    if ($installationId === '' || $binaryHash === '') return ['ok'=>false,'error'=>'invalid_integrity_report'];
    $pdo=airi_db();
    $q=$pdo->prepare('SELECT * FROM installations WHERE installation_id=? LIMIT 1');$q->execute([$installationId]);$install=$q->fetch();
    if (!$install) return ['ok'=>true,'state'=>'pending_install_registration','enforced'=>false];
    $version=(string)$install['app_version'];$channel=(string)$install['channel'];
    $rq=$pdo->prepare('SELECT * FROM release_builds WHERE app_version=? AND channel=? ORDER BY id DESC');$rq->execute([$version,$channel]);$builds=$rq->fetchAll();
    if (!$builds) {
        $pdo->prepare("UPDATE installations SET binary_hash=?,integrity_state='unregistered' WHERE installation_id=?")->execute([$binaryHash,$installationId]);
        return ['ok'=>true,'state'=>'unregistered','enforced'=>false];
    }
    $matched=null;$enforce=false;
    foreach($builds as $b){if((int)$b['enforce_integrity']===1)$enforce=true;if(hash_equals((string)$b['exe_sha256'],$binaryHash)){$matched=$b;break;}}
    if ($matched && $matched['status']==='allowed') {
        $pdo->prepare("UPDATE installations SET binary_hash=?,integrity_state='verified' WHERE installation_id=?")->execute([$binaryHash,$installationId]);
        return ['ok'=>true,'state'=>'verified','enforced'=>$enforce];
    }
    $previous=(string)$install['integrity_state'];
    $state=($matched && $matched['status']==='revoked')?'revoked_build':'mismatch';
    $pdo->prepare('UPDATE installations SET binary_hash=?,integrity_state=? WHERE installation_id=?')->execute([$binaryHash,$state,$installationId]);
    if ($previous!==$state) airi_event($installationId,null,$state==='revoked_build'?'revoked_binary_detected':'binary_integrity_mismatch','critical',80,['app_version'=>$version,'binary_hash_prefix'=>substr($binaryHash,0,16)]);
    if ($state==='revoked_build' || $enforce) $pdo->prepare("UPDATE installations SET license_state='blocked' WHERE installation_id=?")->execute([$installationId]);
    return ['ok'=>true,'state'=>$state,'enforced'=>($state==='revoked_build'||$enforce)];
}

function airi_integrity_policy(string $installationId): array {
    if ($installationId==='') return ['allowed'=>true,'state'=>'unknown'];
    $q=airi_db()->prepare('SELECT integrity_state,license_state FROM installations WHERE installation_id=? LIMIT 1');$q->execute([$installationId]);$r=$q->fetch();
    if (!$r) return ['allowed'=>true,'state'=>'unknown'];
    if ($r['license_state']==='blocked') return ['allowed'=>false,'state'=>(string)$r['integrity_state'],'reason'=>'installation_blocked'];
    return ['allowed'=>true,'state'=>(string)$r['integrity_state']];
}
