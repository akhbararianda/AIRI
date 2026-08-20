const HOST='com.airi.downloadmanager';
const handled=new Set();
const mediaByTab=new Map();
const DEFAULTS={monitorAllDownloads:true,overlayEnabled:true,excludedHosts:[],fileExtensions:[]};

async function cfg(){return chrome.storage.local.get(DEFAULTS)}
function hostOf(url){try{return new URL(url).hostname.toLowerCase()}catch{return''}}
async function siteAllowed(url){const c=await cfg();const h=hostOf(url);return !h||!(c.excludedHosts||[]).includes(h)}
function extOf(url){try{const p=new URL(url).pathname;const m=p.match(/\.([a-z0-9]{1,10})$/i);return m?m[1].toLowerCase():''}catch{return''}}
async function extensionAllowed(url){const c=await cfg();const list=(c.fileExtensions||[]).map(x=>String(x).toLowerCase());if(!list.length)return true;const e=extOf(url);return !e||list.includes(e)}
async function cookieHeader(url){try{return (await chrome.cookies.getAll({url})).map(c=>`${c.name}=${c.value}`).join('; ')}catch{return ''}}
function nativeSend(payload){return new Promise(resolve=>chrome.runtime.sendNativeMessage(HOST,payload,response=>{const e=chrome.runtime.lastError;resolve({ok:!e&&!!response?.ok,response,error:e?.message||response?.message||''})}))}
function updateBadge(tabId){const n=(mediaByTab.get(tabId)||[]).length;chrome.action.setBadgeBackgroundColor({color:'#0f4c81',tabId}).catch(()=>{});chrome.action.setBadgeText({text:n?String(Math.min(n,99)):'',tabId}).catch(()=>{})}
function rememberMedia(tabId,url,type=''){if(tabId<0||!/^https?:\/\//i.test(url))return;let arr=mediaByTab.get(tabId)||[];arr=arr.filter(x=>x.url!==url);arr.unshift({url,type,ts:Date.now()});if(arr.length>80)arr.length=80;mediaByTab.set(tabId,arr);updateBadge(tabId)}
function likelyMediaUrl(url){return /\.(mp4|m4v|webm|mov|mkv|avi|ts|mp3|m4a|aac|ogg|opus|wav|flac|m3u8|mpd)(?:[?#]|$)/i.test(url)||/[?&](mime|type)=(?:video|audio)/i.test(decodeURIComponent(url))}
function manifestUrl(url){return /\.(m3u8|mpd)(?:[?#]|$)/i.test(url)}

chrome.webRequest.onHeadersReceived.addListener(details=>{if(details.tabId<0)return;const ct=(details.responseHeaders||[]).find(h=>h.name?.toLowerCase()==='content-type')?.value||'';if(/^video\//i.test(ct)||/^audio\//i.test(ct)||/application\/(vnd\.apple\.mpegurl|dash\+xml)/i.test(ct)||likelyMediaUrl(details.url)){siteAllowed(details.url).then(ok=>{if(ok)rememberMedia(details.tabId,details.url,ct)})}},{urls:['http://*/*','https://*/*']},['responseHeaders']);
chrome.webRequest.onBeforeRequest.addListener(details=>{if(details.tabId<0)return;if(details.type==='media'||likelyMediaUrl(details.url)){siteAllowed(details.url).then(ok=>{if(ok)rememberMedia(details.tabId,details.url,details.type)})}},{urls:['http://*/*','https://*/*']});

async function handoff(item){if(!item||handled.has(item.id))return;handled.add(item.id);const c=await cfg();if(!c.monitorAllDownloads)return;const url=item.finalUrl||item.url||'';if(!/^https?:\/\//i.test(url)||!(await siteAllowed(url))||!(await extensionAllowed(url)))return;try{await chrome.downloads.pause(item.id)}catch{}const result=await nativeSend({action:'download',url,filename:item.filename?item.filename.split(/[\\/]/).pop():'',referer:item.referrer||'',cookie:await cookieHeader(url),userAgent:navigator.userAgent||'',source:'browser-download',mime:item.mime||''});if(result.ok){try{await chrome.downloads.cancel(item.id)}catch{}try{await chrome.downloads.erase({id:item.id})}catch{}}else{try{await chrome.downloads.resume(item.id)}catch{}await chrome.storage.local.set({lastBridgeError:result.error||'Desktop rejected request'})}}
chrome.downloads.onCreated.addListener(handoff);

function createMenus(){chrome.contextMenus.removeAll(()=>{chrome.contextMenus.create({id:'airi-download-link',title:'Download with AIRI Download Manager',contexts:['link']});chrome.contextMenus.create({id:'airi-download-page',title:'Download page media with AIRI',contexts:['page']});chrome.contextMenus.create({id:'airi-download-all',title:'Download all detected media with AIRI',contexts:['page']})})}
chrome.runtime.onInstalled.addListener(()=>{chrome.storage.local.get(DEFAULTS,v=>chrome.storage.local.set(v));createMenus()});
chrome.runtime.onStartup.addListener(createMenus);
chrome.contextMenus.onClicked.addListener(async(info,tab)=>{if(info.menuItemId==='airi-download-link'&&info.linkUrl){await nativeSend({action:'download',url:info.linkUrl,filename:'',referer:tab?.url||'',cookie:await cookieHeader(info.linkUrl),userAgent:navigator.userAgent||'',source:'context-menu'})}if(info.menuItemId==='airi-download-page'&&tab?.url)await sendPage(tab);if(info.menuItemId==='airi-download-all'&&tab?.id!=null)await downloadAll(tab.id,tab)});

async function sendPage(tab,quality='best'){if(!tab?.url||!(await siteAllowed(tab.url)))return{ok:false,error:'AIRI disabled on this site'};return nativeSend({action:'media_page',pageUrl:tab.url,title:tab.title||'Media',quality,cookie:await cookieHeader(tab.url),referer:tab.url,userAgent:navigator.userAgent||'',source:'page-media'})}
async function downloadAll(tabId,tab=null){const items=(mediaByTab.get(tabId)||[]).filter(x=>!manifestUrl(x.url)).slice(0,24);let sent=0;for(const x of items){if(!(await extensionAllowed(x.url)))continue;const r=await nativeSend({action:'download',url:x.url,filename:'',referer:tab?.url||'',cookie:await cookieHeader(x.url),userAgent:navigator.userAgent||'',source:'download-all'});if(r.ok)sent++}if(!sent&&tab) {const r=await sendPage(tab);if(r.ok)sent=1}return{ok:sent>0,sent}}

chrome.runtime.onMessage.addListener((m,s,send)=>{
  if(m?.type==='airi-universal-media'){
    (async()=>{const c=await cfg();if(!c.overlayEnabled){send({ok:false,error:'Overlay disabled'});return}const tabId=s.tab?.id??-1;const pageUrl=m.pageUrl||s.tab?.url||'';if(!(await siteAllowed(pageUrl))){send({ok:false,error:'AIRI disabled on this site'});return}for(const u of m.directUrls||[])rememberMedia(tabId,u,m.mediaType||'media');const all=[...(m.directUrls||[]),...(mediaByTab.get(tabId)||[]).map(x=>x.url)].filter((u,i,a)=>/^https?:\/\//i.test(u)&&a.indexOf(u)===i);for(const url of all.filter(u=>!manifestUrl(u)).slice(0,8)){if(!(await extensionAllowed(url)))continue;const result=await nativeSend({action:'download',url,filename:'',referer:pageUrl,cookie:await cookieHeader(url),userAgent:navigator.userAgent||'',source:'universal-media-direct'});if(result.ok){send(result);return}}send(await nativeSend({action:'media_page',pageUrl,title:m.title||s.tab?.title||'Media',quality:'best',cookie:await cookieHeader(pageUrl),referer:pageUrl,userAgent:navigator.userAgent||'',source:'universal-media-page',detectedStreams:all.slice(0,20)}))})();return true;
  }
  if(m?.type==='airi-media-page'){(async()=>{const pageUrl=m.pageUrl||s.tab?.url||'';if(!(await siteAllowed(pageUrl))){send({ok:false,error:'AIRI disabled on this site'});return}send(await nativeSend({action:'media_page',pageUrl,title:m.title||s.tab?.title||'Media',quality:m.quality||'best',cookie:await cookieHeader(pageUrl),referer:pageUrl,userAgent:navigator.userAgent||'',source:'youtube-overlay'}))})();return true}
  if(m?.type==='airi-bridge-ping'){nativeSend({action:'ping'}).then(send);return true}
  if(m?.type==='airi-get-config'){cfg().then(config=>send({ok:true,config}));return true}
  if(m?.type==='airi-set-config'){(async()=>{const current=await cfg();const next={...current,...(m.patch||{})};await chrome.storage.local.set(next);send({ok:true,config:next})})();return true}
  if(m?.type==='airi-toggle-site'){(async()=>{const c=await cfg();const h=String(m.host||'').toLowerCase();let list=[...(c.excludedHosts||[])].filter(Boolean);list=list.filter(x=>x!==h);if(m.enabled===false&&h)list.push(h);await chrome.storage.local.set({excludedHosts:[...new Set(list)]});send({ok:true})})();return true}
  if(m?.type==='airi-get-tab-media'){send({ok:true,items:mediaByTab.get(Number(m.tabId))||[]});return true}
  if(m?.type==='airi-download-all-media'){(async()=>{const tabId=Number(m.tabId);const tab=await chrome.tabs.get(tabId).catch(()=>null);send(await downloadAll(tabId,tab))})();return true}
  if(m?.type==='airi-download-page'){(async()=>{const tab=await chrome.tabs.get(Number(m.tabId)).catch(()=>null);send(await sendPage(tab))})();return true}
  if(m?.type==='airi-config-for-page'){(async()=>{const c=await cfg();send({ok:true,overlayEnabled:c.overlayEnabled!==false,siteEnabled:await siteAllowed(m.url||s.tab?.url||'')})})();return true}
});

chrome.commands.onCommand.addListener(async command=>{if(command!=='download-page-media')return;const tabs=await chrome.tabs.query({active:true,currentWindow:true});if(tabs[0])await sendPage(tabs[0])});
chrome.tabs.onRemoved.addListener(tabId=>mediaByTab.delete(tabId));
chrome.tabs.onUpdated.addListener((tabId,change)=>{if(change.status==='loading'){mediaByTab.delete(tabId);updateBadge(tabId)}});
