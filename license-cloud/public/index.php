<?php
declare(strict_types=1);
require_once dirname(__DIR__) . '/app/bootstrap.php';
require_once dirname(__DIR__) . '/app/license.php';

header('X-Frame-Options: DENY');
header('X-Content-Type-Options: nosniff');
header('Referrer-Policy: no-referrer');
header("Permissions-Policy: camera=(), microphone=(), geolocation=()");

$path = (string)(parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH) ?: '/');
$method = strtoupper((string)($_SERVER['REQUEST_METHOD'] ?? 'GET'));

if ($path === '/health' || str_ends_with($path, '/health')) {
    try { airi_db()->query('SELECT 1'); airi_json_response(['ok'=>true,'service'=>'AIRI License Cloud','time'=>time()]); }
    catch (Throwable $e) { airi_json_response(['ok'=>false,'service'=>'AIRI License Cloud'],503); }
}

$apiRoutes = [
    '/api/v1/install/register' => 'airi_api_register',
    '/api/v1/license/activate' => 'airi_api_activate',
    '/api/v1/license/heartbeat' => 'airi_api_heartbeat',
    '/api/v1/license/deactivate' => 'airi_api_deactivate',
    '/api/v1/security/event' => 'airi_api_security_event',
];
foreach ($apiRoutes as $suffix=>$handler) {
    if ($path === $suffix || str_ends_with($path,$suffix)) {
        if ($method !== 'POST') airi_json_response(['ok'=>false,'error'=>'method_not_allowed'],405);
        try {
            $result = $handler(airi_json_input());
            airi_json_response($result['body'], (int)$result['http']);
        } catch (Throwable $e) {
            error_log('AIRI License Cloud API error: '.$e->getMessage());
            airi_json_response(['ok'=>false,'error'=>'server_error'],500);
        }
    }
}

function h(string $s): string { return htmlspecialchars($s, ENT_QUOTES|ENT_SUBSTITUTE, 'UTF-8'); }
function airi_admin_header(string $title): void {
    $csrf = airi_csrf();
    echo '<!doctype html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>'.h($title).'</title><style>
    :root{--cream:#fff8ea;--white:#fff;--blue:#0f4c81;--sky:#58a6ff;--navy:#17283f;--muted:#607086;--line:#dce6f0;--green:#17643a;--red:#a12a2a;--amber:#9a6200}
    *{box-sizing:border-box}body{margin:0;background:var(--cream);color:var(--navy);font:14px "Segoe UI",system-ui,sans-serif}a{color:var(--blue);text-decoration:none}.wrap{max-width:1450px;margin:auto;padding:26px}.top{display:flex;align-items:center;justify-content:space-between;gap:16px;margin-bottom:22px}.brand small{display:block;color:var(--blue);font-weight:800;letter-spacing:.11em}.brand h1{margin:3px 0 0;font-size:28px}.pill{padding:8px 12px;border-radius:999px;background:#edf5fc;color:var(--blue);font-weight:700}.grid{display:grid;grid-template-columns:repeat(6,minmax(0,1fr));gap:12px}.card{background:var(--white);border:1px solid var(--line);border-radius:16px;padding:16px;box-shadow:0 5px 18px #17283f0b}.stat b{display:block;font-size:26px;margin-top:6px}.muted{color:var(--muted)}.section{margin-top:18px}.section h2{font-size:16px;margin:0 0 10px}.tablewrap{overflow:auto;background:var(--white);border:1px solid var(--line);border-radius:16px}table{border-collapse:collapse;width:100%;min-width:850px}th,td{padding:11px 12px;border-bottom:1px solid #edf1f5;text-align:left;vertical-align:middle}th{font-size:12px;color:var(--muted);background:#fbfdff;position:sticky;top:0}.risk-high{color:var(--red);font-weight:800}.risk-mid{color:var(--amber);font-weight:700}.ok{color:var(--green);font-weight:700}.bad{color:var(--red);font-weight:700}.btn{border:0;border-radius:10px;padding:8px 11px;font-weight:700;cursor:pointer;background:#edf5fc;color:var(--blue)}.btn.primary{background:var(--blue);color:white}.btn.danger{background:#fff0f0;color:var(--red)}select,input{border:1px solid var(--line);border-radius:9px;padding:8px;background:white;color:var(--navy)}form.inline{display:inline-flex;gap:5px;align-items:center}.login{max-width:430px;margin:9vh auto;background:white;border:1px solid var(--line);border-radius:20px;padding:26px}.login input{width:100%;margin:7px 0 14px;padding:12px}.login .btn{width:100%;padding:12px}.flash{background:#edf8f2;border:1px solid #c7e6d3;color:var(--green);padding:10px 13px;border-radius:12px;margin-bottom:14px}.warn{background:#fff8e8;border:1px solid #eed9a8;color:#845400;padding:10px 13px;border-radius:12px;margin-bottom:14px}@media(max-width:1000px){.grid{grid-template-columns:repeat(2,1fr)}}
    </style></head><body><div class="wrap">';
}
function airi_admin_footer(): void { echo '</div></body></html>'; }

$admin = (string)($_GET['admin'] ?? '');
if ($admin === 'logout') {
    airi_session_start(); $_SESSION=[]; session_destroy(); header('Location: ./?admin=login'); exit;
}
if ($admin === 'login') {
    airi_session_start();
    $error='';
    if ($method==='POST') {
        airi_require_csrf();
        $cfg=airi_config();
        $user=airi_text($_POST['username'] ?? '',128);$pass=(string)($_POST['password'] ?? '');
        $hash=(string)$cfg['admin_password_hash'];
        if ($hash!=='' && hash_equals((string)$cfg['admin_user'],$user) && password_verify($pass,$hash)) {
            session_regenerate_id(true);$_SESSION['airi_admin']=$user;airi_admin_audit('login');header('Location: ./?admin=dashboard');exit;
        }
        $error='Login failed.';
    }
    airi_admin_header('AIRI License Cloud — Login');
    echo '<div class="login"><div class="brand"><small>AIRI TECHNOLOGY</small><h1>License Cloud</h1></div><p class="muted">Founder: Akhbar Arianda · Private Admin Dashboard</p>';
    if ($error) echo '<div class="warn">'.h($error).'</div>';
    echo '<form method="post"><input type="hidden" name="csrf" value="'.h(airi_csrf()).'"><label>Username</label><input name="username" autocomplete="username" required><label>Password</label><input type="password" name="password" autocomplete="current-password" required><button class="btn primary">Sign in</button></form></div>';
    airi_admin_footer();exit;
}

if ($admin === '' && ($path === '/' || str_ends_with($path,'/index.php'))) {
    header('Location: ./?admin=dashboard'); exit;
}
if ($admin !== 'dashboard') { http_response_code(404); echo 'Not found'; exit; }

airi_admin_require();
$pdo=airi_db();
$flash='';
if ($method==='POST') {
    airi_require_csrf();
    $action=airi_text($_POST['action'] ?? '',40);
    if ($action==='license_status') {
        $id=(int)($_POST['id'] ?? 0);$status=airi_text($_POST['status'] ?? '',24);
        if ($id>0 && in_array($status,['active','suspended','revoked'],true)) {
            $pdo->prepare('UPDATE licenses SET status=? WHERE id=?')->execute([$status,$id]);airi_admin_audit('license_status','license',(string)$id,['status'=>$status]);$flash='License updated.';
        }
    } elseif ($action==='max_devices') {
        $id=(int)($_POST['id'] ?? 0);$max=max(1,min(100,(int)($_POST['max_devices'] ?? 1)));
        if ($id>0) {$pdo->prepare('UPDATE licenses SET max_devices=? WHERE id=?')->execute([$max,$id]);airi_admin_audit('max_devices','license',(string)$id,['max'=>$max]);$flash='Device limit updated.';}
    } elseif ($action==='device_status') {
        $id=(int)($_POST['id'] ?? 0);$status=airi_text($_POST['status'] ?? '',24);
        if ($id>0 && in_array($status,['active','inactive','revoked'],true)) {$pdo->prepare('UPDATE license_devices SET status=? WHERE id=?')->execute([$status,$id]);airi_admin_audit('device_status','device',(string)$id,['status'=>$status]);$flash='Device updated.';}
    } elseif ($action==='risk_reset') {
        $type=airi_text($_POST['type'] ?? '',20);$id=(int)($_POST['id'] ?? 0);
        if ($id>0 && $type==='license') {$pdo->prepare('UPDATE licenses SET risk_score=0 WHERE id=?')->execute([$id]);airi_admin_audit('risk_reset','license',(string)$id);$flash='License risk reset.';}
        if ($id>0 && $type==='install') {$pdo->prepare('UPDATE installations SET risk_score=0 WHERE id=?')->execute([$id]);airi_admin_audit('risk_reset','installation',(string)$id);$flash='Installation risk reset.';}
    }
}

$stats=[
 'installs'=>(int)$pdo->query('SELECT COUNT(*) FROM installations')->fetchColumn(),
 'active24'=>(int)$pdo->query("SELECT COUNT(*) FROM installations WHERE last_seen>=DATE_SUB(UTC_TIMESTAMP(),INTERVAL 1 DAY)")->fetchColumn(),
 'mau'=>(int)$pdo->query("SELECT COUNT(*) FROM installations WHERE last_seen>=DATE_SUB(UTC_TIMESTAMP(),INTERVAL 30 DAY)")->fetchColumn(),
 'licensed'=>(int)$pdo->query("SELECT COUNT(*) FROM installations WHERE license_state='licensed'")->fetchColumn(),
 'licenses'=>(int)$pdo->query("SELECT COUNT(*) FROM licenses WHERE status='active'")->fetchColumn(),
 'suspicious'=>(int)$pdo->query("SELECT (SELECT COUNT(*) FROM licenses WHERE risk_score>=50)+(SELECT COUNT(*) FROM installations WHERE risk_score>=50)")->fetchColumn(),
];
$licenses=$pdo->query("SELECT l.*, (SELECT COUNT(*) FROM license_devices d WHERE d.license_id=l.id AND d.status='active') active_devices FROM licenses l ORDER BY GREATEST(l.risk_score,0) DESC,l.last_seen DESC LIMIT 100")->fetchAll();
$devices=$pdo->query("SELECT d.*,l.customer,l.edition,l.status license_status FROM license_devices d JOIN licenses l ON l.id=d.license_id ORDER BY d.last_seen DESC LIMIT 100")->fetchAll();
$events=$pdo->query("SELECT e.*,l.customer FROM security_events e LEFT JOIN licenses l ON l.id=e.license_id ORDER BY e.id DESC LIMIT 100")->fetchAll();
$installs=$pdo->query("SELECT * FROM installations ORDER BY risk_score DESC,last_seen DESC LIMIT 100")->fetchAll();

airi_admin_header('AIRI License Cloud — Dashboard');
echo '<div class="top"><div class="brand"><small>AIRI TECHNOLOGY</small><h1>License Intelligence</h1><div class="muted">Founder Akhbar Arianda · privacy-minimal licensing telemetry</div></div><div><span class="pill">LIVE CLOUD</span> &nbsp; <a href="?admin=logout">Sign out</a></div></div>';
if ($flash) echo '<div class="flash">'.h($flash).'</div>';
echo '<div class="grid">';
foreach ([['Total installs',$stats['installs']],['Active 24h',$stats['active24']],['Monthly active',$stats['mau']],['Licensed PCs',$stats['licensed']],['Active licenses',$stats['licenses']],['Suspicious',$stats['suspicious']]] as [$label,$value]) echo '<div class="card stat"><span class="muted">'.h((string)$label).'</span><b>'.number_format((int)$value).'</b></div>';
echo '</div>';

echo '<div class="section"><h2>Licenses</h2><div class="tablewrap"><table><thead><tr><th>Customer</th><th>Edition</th><th>Status</th><th>Devices</th><th>Risk</th><th>Last seen</th><th>Controls</th></tr></thead><tbody>';
foreach($licenses as $r){$risk=(int)$r['risk_score'];$rc=$risk>=50?'risk-high':($risk>=20?'risk-mid':'ok');echo '<tr><td><b>'.h((string)$r['customer']).'</b><br><span class="muted">'.h(substr((string)$r['license_hash'],0,14)).'…</span></td><td>'.h((string)$r['edition']).'</td><td class="'.(in_array($r['status'],['active'],true)?'ok':'bad').'">'.h((string)$r['status']).'</td><td>'.(int)$r['active_devices'].' / '.(int)$r['max_devices'].'</td><td class="'.$rc.'">'.$risk.'</td><td>'.h((string)($r['last_seen']??'-')).'</td><td><form class="inline" method="post"><input type="hidden" name="csrf" value="'.h(airi_csrf()).'"><input type="hidden" name="action" value="license_status"><input type="hidden" name="id" value="'.(int)$r['id'].'"><select name="status"><option>active</option><option>suspended</option><option>revoked</option></select><button class="btn">Apply</button></form> <form class="inline" method="post"><input type="hidden" name="csrf" value="'.h(airi_csrf()).'"><input type="hidden" name="action" value="max_devices"><input type="hidden" name="id" value="'.(int)$r['id'].'"><input name="max_devices" type="number" min="1" max="100" value="'.(int)$r['max_devices'].'" style="width:65px"><button class="btn">Limit</button></form></td></tr>';}
echo '</tbody></table></div></div>';

echo '<div class="section"><h2>Devices</h2><div class="tablewrap"><table><thead><tr><th>Customer</th><th>Installation</th><th>Machine hash</th><th>Device status</th><th>License</th><th>Last seen</th><th>Control</th></tr></thead><tbody>';
foreach($devices as $r){echo '<tr><td>'.h((string)$r['customer']).'<br><span class="muted">'.h((string)$r['edition']).'</span></td><td>'.h(substr((string)$r['installation_id'],0,22)).'…</td><td>'.h(substr((string)$r['machine_hash'],0,16)).'…</td><td>'.h((string)$r['status']).'</td><td>'.h((string)$r['license_status']).'</td><td>'.h((string)$r['last_seen']).'</td><td><form class="inline" method="post"><input type="hidden" name="csrf" value="'.h(airi_csrf()).'"><input type="hidden" name="action" value="device_status"><input type="hidden" name="id" value="'.(int)$r['id'].'"><select name="status"><option>active</option><option>inactive</option><option>revoked</option></select><button class="btn">Apply</button></form></td></tr>';}
echo '</tbody></table></div></div>';

echo '<div class="section"><h2>Security events</h2><div class="tablewrap"><table><thead><tr><th>Time</th><th>Severity</th><th>Event</th><th>Customer</th><th>Installation</th><th>Risk +</th><th>Metadata</th></tr></thead><tbody>';
foreach($events as $r){$class=in_array($r['severity'],['high','critical'],true)?'risk-high':($r['severity']==='medium'?'risk-mid':'');echo '<tr><td>'.h((string)$r['created_at']).'</td><td class="'.$class.'">'.h((string)$r['severity']).'</td><td><b>'.h((string)$r['event_type']).'</b></td><td>'.h((string)($r['customer']??'-')).'</td><td>'.h(substr((string)($r['installation_id']??'-'),0,22)).'</td><td>'.(int)$r['risk_delta'].'</td><td class="muted">'.h(substr((string)($r['metadata_json']??''),0,180)).'</td></tr>';}
echo '</tbody></table></div></div>';

echo '<div class="section"><h2>Installations</h2><div class="tablewrap"><table><thead><tr><th>Installation</th><th>Version</th><th>OS</th><th>License state</th><th>Risk</th><th>First seen</th><th>Last seen</th></tr></thead><tbody>';
foreach($installs as $r){$risk=(int)$r['risk_score'];$rc=$risk>=50?'risk-high':($risk>=20?'risk-mid':'ok');echo '<tr><td>'.h(substr((string)$r['installation_id'],0,26)).'…<br><span class="muted">'.h(substr((string)$r['machine_hash'],0,14)).'…</span></td><td>'.h((string)$r['app_version']).'</td><td>'.h((string)$r['os_version']).'<br><span class="muted">'.h((string)$r['architecture']).'</span></td><td>'.h((string)$r['license_state']).'</td><td class="'.$rc.'">'.$risk.'</td><td>'.h((string)$r['first_seen']).'</td><td>'.h((string)$r['last_seen']).'</td></tr>';}
echo '</tbody></table></div></div>';

airi_admin_footer();
