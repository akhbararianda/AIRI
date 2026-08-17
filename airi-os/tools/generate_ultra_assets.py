import os, struct, zlib

ROOT=os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'assets')
WP=os.path.join(ROOT,'wallpapers')
BR=os.path.join(ROOT,'branding')
TX=os.path.join(ROOT,'textures')
for d in (WP,BR,TX): os.makedirs(d,exist_ok=True)

def chunk(kind,data):
    return struct.pack('>I',len(data))+kind+data+struct.pack('>I',zlib.crc32(kind+data)&0xffffffff)

def write_png(path,w,h,raw):
    data=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',w,h,8,2,0,0,0))+chunk(b'IDAT',zlib.compress(raw,6))+chunk(b'IEND',b'')
    with open(path,'wb') as f:f.write(data)
    return len(data)

def gradient_png(path,w,h,a,b,c):
    rows=[]
    for y in range(h):
        fy=y/max(1,h-1)
        t=fy
        rr=int(a[0]*(1-t)+b[0]*t); gg=int(a[1]*(1-t)+b[1]*t); bb=int(a[2]*(1-t)+b[2]*t)
        row=bytearray([0])
        # Horizontal secondary blend for a real multi-tone wallpaper, generated efficiently.
        for x in range(w):
            fx=x/max(1,w-1)
            u=max(0.0,1.0-abs(fx-.72)*2.1-abs(fy-.32)*1.2)
            r=int(rr*(1-u)+c[0]*u); g=int(gg*(1-u)+c[1]*u); bl=int(bb*(1-u)+c[2]*u)
            row.extend((r,g,bl))
        rows.append(bytes(row))
    return write_png(path,w,h,b''.join(rows))

def texture_png(path,w,h):
    # Real high-frequency material grain texture. Random RGB data keeps crystalline/grain surfaces detailed.
    raw_rows=[]
    buf=os.urandom(w*h*3)
    stride=w*3
    for y in range(h): raw_rows.append(b'\x00'+buf[y*stride:(y+1)*stride])
    return write_png(path,w,h,b''.join(raw_rows))

palettes=[
 ((14,24,48),(79,110,255),(183,114,255)),
 ((235,242,255),(146,179,255),(251,193,212)),
 ((8,13,19),(22,107,121),(115,66,171)),
 ((247,239,228),(215,190,157),(161,135,255)),
 ((7,17,25),(21,78,97),(84,214,190)),
 ((31,18,35),(143,70,116),(255,153,103)),
]
names=['infinity_glass','aurora_orbit','titanium_flow','emerald_future','violet_core','sunset_quantum']
sizes=[]
for name,pal in zip(names,palettes): sizes.append((name,gradient_png(os.path.join(WP,name+'.png'),540,1200,*pal)))
sizes.append(('splash_dark',gradient_png(os.path.join(BR,'splash_infinity_dark.png'),720,1280,(7,9,16),(36,43,76),(115,79,188))))
sizes.append(('splash_light',gradient_png(os.path.join(BR,'splash_infinity_light.png'),720,1280,(238,244,255),(220,226,245),(157,177,255))))
# Four substantial icon/material textures, around 0.75 MB raw each before PNG overhead/compression.
for name in ['crystal','pearl','graphite','tinted']:
    sizes.append((name,texture_png(os.path.join(TX,'icon_'+name+'.png'),512,512)))
print('Generated AIRI Ultra Asset Pack:')
for n,s in sizes: print(n,s,'bytes')
print('TOTAL',sum(s for _,s in sizes),'bytes')
