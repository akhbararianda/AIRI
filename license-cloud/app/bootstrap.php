<?php
declare(strict_types=1);

function airi_env(string $name, ?string $default = null): ?string {
    $v = getenv($name);
    return ($v === false || $v === '') ? $default : $v;
}

function airi_config(): array {
    static $cfg;
    if ($cfg !== null) return $cfg;
    $base = [
        'dsn' => airi_env('AIRI_DB_DSN', 'mysql:host=127.0.0.1;dbname=airi_license;charset=utf8mb4'),
        'db_user' => airi_env('AIRI_DB_USER', 'airi_license'),
        'db_pass' => airi_env('AIRI_DB_PASS', ''),
        'app_secret' => airi_env('AIRI_APP_SECRET', ''),
        'admin_user' => airi_env('AIRI_ADMIN_USER', 'admin'),
        'admin_password_hash' => airi_env('AIRI_ADMIN_PASSWORD_HASH', ''),
        'base_url' => airi_env('AIRI_BASE_URL', ''),
    ];
    $localPath = dirname(__DIR__) . '/config.php';
    $local = is_file($localPath) ? require $localPath : [];
    if (!is_array($local)) $local = [];
    $cfg = array_replace($base, $local);
    $cfg['public_key'] = dirname(__DIR__) . '/keys/airi-license-public.pem';
    $cfg['base_url'] = rtrim((string)($cfg['base_url'] ?? ''), '/');
    if (strlen((string)$cfg['app_secret']) < 32) {
        throw new RuntimeException('AIRI_APP_SECRET/app_secret must be at least 32 characters.');
    }
    return $cfg;
}

function airi_db(): PDO {
    static $pdo;
    if ($pdo instanceof PDO) return $pdo;
    $c = airi_config();
    $pdo = new PDO((string)$c['dsn'], (string)$c['db_user'], (string)$c['db_pass'], [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);
    return $pdo;
}

function airi_json_response(array $data, int $status = 200): never {
    http_response_code($status);
    header('Content-Type: application/json; charset=utf-8');
    header('Cache-Control: no-store');
    header('X-Content-Type-Options: nosniff');
    echo json_encode($data, JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
    exit;
}

function airi_json_input(int $maxBytes = 65536): array {
    $len = (int)($_SERVER['CONTENT_LENGTH'] ?? 0);
    if ($len > $maxBytes) airi_json_response(['ok'=>false,'error'=>'payload_too_large'], 413);
    $raw = file_get_contents('php://input');
    if ($raw === false || strlen($raw) > $maxBytes) airi_json_response(['ok'=>false,'error'=>'payload_too_large'], 413);
    if ($raw === '') return [];
    $data = json_decode($raw, true);
    if (!is_array($data)) airi_json_response(['ok'=>false,'error'=>'invalid_json'], 400);
    return $data;
}

function airi_text(mixed $v, int $max = 255): string {
    $s = trim((string)$v);
    if (mb_strlen($s) > $max) $s = mb_substr($s, 0, $max);
    return $s;
}

function airi_hex64(mixed $v): string {
    $s = strtolower(airi_text($v, 64));
    return preg_match('/^[a-f0-9]{64}$/', $s) ? $s : '';
}

function airi_installation_id(mixed $v): string {
    $s = airi_text($v, 64);
    return preg_match('/^[A-Za-z0-9-]{20,64}$/', $s) ? $s : '';
}

function airi_ip_hash(): string {
    $ip = (string)($_SERVER['REMOTE_ADDR'] ?? 'unknown');
    return hash_hmac('sha256', $ip, (string)airi_config()['app_secret']);
}

function airi_now(): string { return gmdate('Y-m-d H:i:s'); }

function airi_event(?string $installationId, ?int $licenseId, string $type, string $severity='low', int $riskDelta=0, array $meta=[]): void {
    $allowed = ['low','medium','high','critical'];
    if (!in_array($severity, $allowed, true)) $severity = 'low';
    $cleanMeta = [];
    foreach ($meta as $k=>$v) {
        $key = preg_replace('/[^a-zA-Z0-9_.-]/', '', (string)$k) ?: 'field';
        $cleanMeta[$key] = airi_text(is_scalar($v) ? $v : json_encode($v), 300);
        if (count($cleanMeta) >= 12) break;
    }
    $stmt = airi_db()->prepare('INSERT INTO security_events (installation_id,license_id,event_type,severity,risk_delta,metadata_json,ip_hash) VALUES (?,?,?,?,?,?,?)');
    $stmt->execute([$installationId ?: null,$licenseId,$type,$severity,$riskDelta,json_encode($cleanMeta, JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE),airi_ip_hash()]);
    if ($riskDelta !== 0 && $installationId) {
        $u = airi_db()->prepare('UPDATE installations SET risk_score=GREATEST(0,risk_score+?) WHERE installation_id=?');
        $u->execute([$riskDelta,$installationId]);
    }
    if ($riskDelta !== 0 && $licenseId) {
        $u = airi_db()->prepare('UPDATE licenses SET risk_score=GREATEST(0,risk_score+?) WHERE id=?');
        $u->execute([$riskDelta,$licenseId]);
    }
}

function airi_session_start(): void {
    if (session_status() === PHP_SESSION_ACTIVE) return;
    $secure = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off');
    session_set_cookie_params(['httponly'=>true,'secure'=>$secure,'samesite'=>'Strict','path'=>'/']);
    session_name('AIRIADMIN');
    session_start();
}

function airi_csrf(): string {
    airi_session_start();
    if (empty($_SESSION['csrf'])) $_SESSION['csrf'] = bin2hex(random_bytes(24));
    return (string)$_SESSION['csrf'];
}

function airi_require_csrf(): void {
    airi_session_start();
    $got = (string)($_POST['csrf'] ?? '');
    if (!hash_equals((string)($_SESSION['csrf'] ?? ''), $got)) {
        http_response_code(403); exit('CSRF validation failed');
    }
}

function airi_admin_logged_in(): bool {
    airi_session_start();
    return !empty($_SESSION['airi_admin']);
}

function airi_admin_require(): void {
    if (!airi_admin_logged_in()) { header('Location: ./?admin=login'); exit; }
}

function airi_admin_audit(string $action, string $targetType='', string $targetId='', array $meta=[]): void {
    airi_session_start();
    $stmt = airi_db()->prepare('INSERT INTO admin_audit (admin_user,action,target_type,target_id,metadata_json,ip_hash) VALUES (?,?,?,?,?,?)');
    $stmt->execute([(string)($_SESSION['airi_admin'] ?? 'unknown'),$action,$targetType,$targetId,json_encode($meta,JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE),airi_ip_hash()]);
}
