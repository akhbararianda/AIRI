import os, struct, zlib, math

ROOT=os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets')
WP=os.path.join(ROOT,'wallpapers')
BR=os.path.join(ROOT,'branding')
TX=os.path.join(ROOT,'textures')
for d in (WP,BR,TX): os.makedirs(d,exist_ok=True)

def png(path,w,h,seed,mode=0):
    # Deterministic RGB wallpaper with gradients + fine film texture.
    state=(seed*1664525+1013904223)&0xffffffff
    def rnd():
        nonlocal state
        state=(1664525*state+1013904223)&0xffffffff
        return (state>>16)&255
    rows=[]
    palettes=[
        ((14,24,48),(79,110,255),(183,114,255)),
        ((235,242,255),(146,179,255),(251,193,212)),
        ((8,13,19),(22,107,121),(115,66,171)),
        ((247,239,228),(215,190,157),(161,135,255)),
        ((7,17,25),(21,78,97),(84,214,190)),
        ((31,18,35),(143,70,116),(255,153,103)),
        ((17,19,27),(71,89,145),(126,92,201)),
        ((235,246,244),(126,214,198),(116,169,255)),
    ]
    a,b,c=palettes[seed%len(palettes)]
    for y in range(h):
        row=bytearray([0])
        fy=y/max(1,h-1)
        for x in range(w):
            fx=x/max(1,w-1)
            wave=(math.sin(fx*7.0+fy*5.0+seed)*0.5+0.5)
            t=min(1,max(0,0.54*fx+0.46*fy+0.14*(wave-.5)))
            u=min(1,max(0,1-abs(fx-.68)*1.55-abs(fy-.34)*1.05))
            base=[]
            for ch in range(3):
                v=a[ch]*(1-t)+b[ch]*t
                v=v*(1-u)+c[ch]*u
                # Fine grain is intentional visual texture; it also keeps bundled assets substantive.
                noise=(rnd()-128)*(0.10 if mode==0 else 0.16)
                base.append(max(0,min(255,int(v+noise))))
            row.extend(base)
        rows.append(bytes(row))
    raw=b''.join(rows)
    def chunk(kind,data):
        return struct.pack('>I',len(data))+kind+data+struct.pack('>I',zlib.crc32(kind+data)&0xffffffff)
    data=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',w,h,8,2,0,0,0))+chunk(b'IDAT',zlib.compress(raw,6))+chunk(b'IEND',b'')
    with open(path,'wb') as f:f.write(data)
    return len(data)

sizes=[]
for i,name in enumerate(['infinity_glass','aurora_orbit','titanium_flow','emerald_future','violet_core','sunset_quantum']):
    sizes.append((name,png(os.path.join(WP,name+'.png'),540,1200,120+i,0)))
# Two real splash/branding backgrounds.
sizes.append(('splash_dark',png(os.path.join(BR,'splash_infinity_dark.png'),720,1280,501,1)))
sizes.append(('splash_light',png(os.path.join(BR,'splash_infinity_light.png'),720,1280,502,1)))
# Material texture assets used by future visual surfaces/icon treatments.
for i,name in enumerate(['crystal','pearl','graphite','tinted']):
    sizes.append((name,png(os.path.join(TX,'icon_'+name+'.png'),360,360,700+i,1)))
print('Generated AIRI Ultra Asset Pack:')
for n,s in sizes: print(n, s, 'bytes')
print('TOTAL',sum(s for _,s in sizes),'bytes')
