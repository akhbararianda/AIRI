const $=s=>document.querySelector(s);
const monitor=$('#monitor'),overlay=$('#overlay'),bridge=$('#bridge'),test=$('#test'),page=$('#page'),siteState=$('#siteState'),siteToggle=$('#siteToggle'),hostEl=$('#host'),count=$('#count'),mediaList=$('#mediaList'),refresh=$('#refresh'),downloadAll=$('#downloadAll'),types=$('#types'),saveTypes=$('#saveTypes');
let activeTab=null,currentHost='',siteEnabled=true;

async function msg(payload){try{return await chrome.runtime.sendMessage(payload)}catch(e){return{ok:false,error:String(e)}}}
async function getActiveTab(){const tabs=await chrome.tabs.query({active:true,currentWindow:true});activeTab=tabs[0]||null;try{currentHost=new URL(activeTab?.url||'').hostname}catch{currentHost=''}hostEl.textContent=currentHost||'This page cannot be managed';}
async function loadConfig(){const c=await msg({type:'airi-get-config'});const v=c?.config||{};monitor.checked=v.monitorAllDownloads!==false;overlay.checked=v.overlayEnabled!==false;types.value=(v.fileExtensions||[]).join(',');siteEnabled=!((v.excludedHosts||[]).includes(currentHost));renderSite();}
function renderSite(){siteState.textContent=siteEnabled?'Enabled':'Disabled';siteState.className='pill '+(siteEnabled?'ok':'bad');siteToggle.textContent=siteEnabled?'Disable AIRI on this site':'Enable AIRI on this site';siteToggle.className=siteEnabled?'warn':'secondary';siteToggle.disabled=!currentHost;}
async function ping(){bridge.textContent='Checking...';bridge.className='pill';const r=await msg({type:'airi-bridge-ping'});bridge.textContent=r?.ok?'Connected':'Not connected';bridge.className='pill '+(r?.ok?'ok':'bad');}
function shortName(url){try{const u=new URL(url);return decodeURIComponent(u.pathname.split('/').filter(Boolean).pop()||u.hostname).slice(0,70)}catch{return String(url).slice(0,70)}}
async function loadMedia(){if(!activeTab?.id)return;const r=await msg({type:'airi-get-tab-media',tabId:activeTab.id});const items=r?.items||[];count.textContent=String(items.length);downloadAll.disabled=!items.length;mediaList.innerHTML='';if(!items.length){mediaList.innerHTML='<div class="hint">No media detected yet. Start playback, then refresh.</div>';return;}for(const x of items.slice(0,12)){const d=document.createElement('div');d.className='media';const n=document.createElement('div');n.className='name';n.textContent=shortName(x.url);const m=document.createElement('div');m.className='meta';m.textContent=(x.type||'media')+' · '+new URL(x.url).hostname;d.append(n,m);mediaList.appendChild(d)}}
monitor.onchange=()=>msg({type:'airi-set-config',patch:{monitorAllDownloads:monitor.checked}});
overlay.onchange=()=>msg({type:'airi-set-config',patch:{overlayEnabled:overlay.checked}});
test.onclick=ping;
page.onclick=async()=>{page.disabled=true;page.textContent='Sending...';const r=await msg({type:'airi-download-page',tabId:activeTab?.id});page.textContent=r?.ok?'Started in AIRI':'Failed';setTimeout(()=>{page.disabled=false;page.textContent='Download page media'},1600)};
siteToggle.onclick=async()=>{if(!currentHost)return;const r=await msg({type:'airi-toggle-site',host:currentHost,enabled:!siteEnabled});if(r?.ok){siteEnabled=!siteEnabled;renderSite()}};
refresh.onclick=loadMedia;
downloadAll.onclick=async()=>{downloadAll.disabled=true;downloadAll.textContent='Sending...';const r=await msg({type:'airi-download-all-media',tabId:activeTab?.id});downloadAll.textContent=r?.ok?`Sent ${r.sent||0}`:'Failed';setTimeout(()=>{downloadAll.disabled=false;downloadAll.textContent='Download all'},1800)};
saveTypes.onclick=async()=>{const arr=types.value.split(',').map(x=>x.trim().toLowerCase().replace(/^\./,'')).filter(Boolean);await msg({type:'airi-set-config',patch:{fileExtensions:[...new Set(arr)]}});saveTypes.textContent='Saved';setTimeout(()=>saveTypes.textContent='Save filter',1200)};
(async()=>{await getActiveTab();await loadConfig();await Promise.all([ping(),loadMedia()]);})();
