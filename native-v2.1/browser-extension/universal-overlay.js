(()=>{
  if(window.__AIRI_UNIVERSAL_MEDIA_35)return;
  window.__AIRI_UNIVERSAL_MEDIA_35=true;
  const mounted=new WeakMap();
  const MIN_W=240,MIN_H=135;

  function eligible(el){
    const r=el.getBoundingClientRect();
    return r.width>=MIN_W&&r.height>=MIN_H&&r.bottom>0&&r.right>0;
  }

  async function sendMedia(el,button){
    const old=button.textContent;
    button.disabled=true;
    button.textContent='AIRI: detecting…';
    const direct=[];
    const push=u=>{try{if(u&&/^https?:\/\//i.test(u)&&!direct.includes(u))direct.push(u)}catch{}};
    push(el.currentSrc);push(el.src);
    el.querySelectorAll?.('source[src]').forEach(s=>push(s.src));
    const response=await chrome.runtime.sendMessage({
      type:'airi-universal-media',
      pageUrl:location.href,
      title:document.title||'Media',
      directUrls:direct,
      mediaType:el.tagName.toLowerCase()
    });
    if(response?.ok) button.textContent='✓ Started in AIRI';
    else button.textContent='AIRI: '+String(response?.error||response?.response?.message||'failed').slice(0,36);
    setTimeout(()=>{button.textContent=old;button.disabled=false},2600);
  }

  function mount(el){
    if(mounted.has(el)||!eligible(el))return;
    const wrap=document.createElement('div');
    wrap.className='airi-universal-wrap';
    const btn=document.createElement('button');
    btn.className='airi-universal-btn';
    btn.textContent='↓ AIRI DM';
    btn.title='Download this media with AIRI Download Manager';
    btn.addEventListener('click',e=>{e.preventDefault();e.stopPropagation();sendMedia(el,btn)});
    wrap.appendChild(btn);
    document.documentElement.appendChild(wrap);
    mounted.set(el,wrap);

    const place=()=>{
      if(!el.isConnected){wrap.remove();return}
      const r=el.getBoundingClientRect();
      if(!eligible(el)){wrap.style.display='none';return}
      wrap.style.display='block';
      wrap.style.left=Math.max(8,r.right-126)+'px';
      wrap.style.top=Math.max(8,r.top+12)+'px';
    };
    place();
    const ro=new ResizeObserver(place);ro.observe(el);
    const iv=setInterval(()=>{if(!el.isConnected){clearInterval(iv);ro.disconnect();wrap.remove()}else place()},700);
  }

  function scan(){
    document.querySelectorAll('video,audio').forEach(mount);
  }
  const mo=new MutationObserver(scan);
  mo.observe(document.documentElement,{subtree:true,childList:true,attributes:true,attributeFilter:['src']});
  window.addEventListener('scroll',scan,{passive:true});
  window.addEventListener('resize',scan,{passive:true});
  setInterval(scan,1500);
  scan();
})();
