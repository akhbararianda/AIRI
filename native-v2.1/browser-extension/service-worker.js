const HOST='com.airi.downloadmanager';
const handled=new Set();
const mediaByTab=new Map();

async function cfg(){return chrome.storage.local.get({monitorAllDownloads:true});}
async function cookieHeader(url){try{return (await chrome.cookies.getAll({url})).map(c=>`${c.name}=${c.value}`).join('; ')}catch{return ''}}
function nativeSend(payload){return new Promise(resolve=>chrome.runtime.sendNativeMessage(HOST,payload,response=>{const e=chrome.runtime.lastError;resolve({ok:!e&&!!response?.ok,response,error:e?.message||response?.message||''})}))}
function rememberMedia(tabId,url,type=''){if(tabId<0||!/^https?:\/\//i.test(url))return;let arr=mediaByTab.get(tabId)||[];arr=arr.filter(x=>x.url!==url);arr.unshift({url,type,ts:Date.now()});if(arr.length>40)arr.length=40;mediaByTab.set(tabId,arr)}
function likelyMediaUrl(url){return /\.(mp4|m4v|webm|mov|mkv|mp3|m4a|aac|ogg|wav|flac|m3u8|mpd)(?:[?#]|$)/i.test(url)||/[?&](mime|type)=(?:video|audio)/i.test(decodeURIComponent(url));}

chrome.webRequest.onHeadersReceived.addListener(details=>{
  if(details.tabId<0)return;
  const ct=(details.responseHeaders||[]).find(h=>h.name?.toLowerCase()==='content-type')?.value||'';
  if(/^video\//i.test(ct)||/^audio\//i.test(ct)||/application\/(vnd\.apple\.mpegurl|dash\+xml)/i.test(ct)||likelyMediaUrl(details.url)) rememberMedia(details.tabId,details.url,ct);
},{urls:['http://*/*','https://*/*']},['responseHeaders']);

chrome.webRequest.onBeforeRequest.addListener(details=>{
  if(details.tabId<0)return;
  if(details.type==='media'||likelyMediaUrl(details.url))rememberMedia(details.tabId,details.url,details.type);
},{urls:['http://*/*','https://*/*']});

async function handoff(item){if(!item||handled.has(item.id))return;handled.add(item.id);if(!(await cfg()).monitorAllDownloads)return;const url=item.finalUrl||item.url||'';if(!/^https?:\/\//i.test(url))return;try{await chrome.downloads.pause(item.id)}catch{}const result=await nativeSend({action:'download',url,filename:item.filename?item.filename.split(/[\\/]/).pop():'',referer:item.referrer||'',cookie:await cookieHeader(url),userAgent:navigator.userAgent||'',source:'browser-download',mime:item.mime||''});if(result.ok){try{await chrome.downloads.cancel(item.id)}catch{}try{await chrome.downloads.erase({id:item.id})}catch{}}else{try{await chrome.downloads.resume(item.id)}catch{}await chrome.storage.local.set({lastBridgeError:result.error||'Desktop rejected request'})}}
chrome.downloads.onCreated.addListener(handoff);

chrome.runtime.onInstalled.addListener(()=>{chrome.storage.local.get({monitorAllDownloads:true},v=>chrome.storage.local.set(v));chrome.contextMenus.create({id:'airi-download-link',title:'Download with AIRI Download Manager',contexts:['link']});chrome.contextMenus.create({id:'airi-download-page',title:'Download page media with AIRI',contexts:['page']});});
chrome.contextMenus.onClicked.addListener(async(info,tab)=>{if(info.menuItemId==='airi-download-link'&&info.linkUrl)await nativeSend({action:'download',url:info.linkUrl,filename:'',referer:tab?.url||'',cookie:await cookieHeader(info.linkUrl),userAgent:navigator.userAgent||'',source:'context-menu'});if(info.menuItemId==='airi-download-page'&&tab?.url)await nativeSend({action:'media_page',pageUrl:tab.url,title:tab.title||'Media',quality:'best',cookie:await cookieHeader(tab.url),referer:tab.url,userAgent:navigator.userAgent||'',source:'context-menu'});});

chrome.runtime.onMessage.addListener((m,s,send)=>{
  if(m?.type==='airi-universal-media'){
    (async()=>{
      const tabId=s.tab?.id??-1;
      const direct=[...(m.directUrls||[]),...(mediaByTab.get(tabId)||[]).map(x=>x.url)].filter((u,i,a)=>/^https?:\/\//i.test(u)&&a.indexOf(u)===i);
      const pageUrl=m.pageUrl||s.tab?.url||'';
      let result=null;
      for(const url of direct.slice(0,8)){
        result=await nativeSend({action:'download',url,filename:'',referer:pageUrl,cookie:await cookieHeader(url),userAgent:navigator.userAgent||'',source:'universal-media-direct'});
        if(result.ok){send(result);return;}
      }
      result=await nativeSend({action:'media_page',pageUrl,title:m.title||s.tab?.title||'Media',quality:'best',cookie:await cookieHeader(pageUrl),referer:pageUrl,userAgent:navigator.userAgent||'',source:'universal-media-page'});
      send(result);
    })();
    return true;
  }
  if(m?.type==='airi-media-page'){
    (async()=>{const pageUrl=m.pageUrl||s.tab?.url||'';send(await nativeSend({action:'media_page',pageUrl,title:m.title||s.tab?.title||'Media',quality:m.quality||'best',cookie:await cookieHeader(pageUrl),referer:pageUrl,userAgent:navigator.userAgent||'',source:'youtube-overlay'}))})();
    return true;
  }
  if(m?.type==='airi-bridge-ping'){nativeSend({action:'ping'}).then(send);return true;}
});

chrome.tabs?.onRemoved?.addListener(tabId=>mediaByTab.delete(tabId));
