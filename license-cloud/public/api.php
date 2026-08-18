<?php
declare(strict_types=1);
require_once dirname(__DIR__) . '/app/bootstrap.php';
require_once dirname(__DIR__) . '/app/license.php';
require_once dirname(__DIR__) . '/app/integrity.php';

header('X-Content-Type-Options: nosniff');
header('Cache-Control: no-store');
$method=strtoupper((string)($_SERVER['REQUEST_METHOD']??'GET'));
$path=(string)(parse_url($_SERVER['REQUEST_URI']??'/',PHP_URL_PATH)?:'/');
if($method!=='POST') airi_json_response(['ok'=>false,'error'=>'method_not_allowed'],405);
$data=airi_json_input();

if(str_ends_with($path,'/api/v1/security/event') && airi_text($data['event_type']??'',80)==='binary_fingerprint'){
    $id=airi_installation_id($data['installation_id']??'');
    $hash=airi_hex64($data['detail']??'');
    $r=airi_integrity_report($id,$hash);
    airi_json_response($r,$r['ok']?202:400);
}

$routes=[
    '/api/v1/install/register'=>'airi_api_register',
    '/api/v1/license/activate'=>'airi_api_activate',
    '/api/v1/license/heartbeat'=>'airi_api_heartbeat',
    '/api/v1/license/deactivate'=>'airi_api_deactivate',
    '/api/v1/security/event'=>'airi_api_security_event',
];
$handler=null;
foreach($routes as $suffix=>$fn) if(str_ends_with($path,$suffix)){$handler=$fn;break;}
if(!$handler) airi_json_response(['ok'=>false,'error'=>'not_found'],404);
try{
    $result=$handler($data);
    if(in_array($handler,['airi_api_register','airi_api_heartbeat','airi_api_activate'],true)){
        $id=airi_installation_id($data['installation_id']??'');
        $policy=airi_integrity_policy($id);
        if(!$policy['allowed']){
            $result=['http'=>403,'body'=>array_merge($result['body'],['ok'=>true,'allowed'=>false,'reason'=>$policy['reason']??'integrity_blocked','integrity_state'=>$policy['state']])];
        }else{
            $result['body']['integrity_state']=$policy['state'];
        }
    }
    airi_json_response($result['body'],(int)$result['http']);
}catch(Throwable $e){
    error_log('AIRI License Cloud API error: '.$e->getMessage());
    airi_json_response(['ok'=>false,'error'=>'server_error'],500);
}
