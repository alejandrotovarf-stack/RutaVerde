import math, numpy as np
from PIL import Image, ImageDraw, ImageFilter
S=1024
def curva(p0,p1,p2,p3,n=200):
    for i in range(n+1):
        t=i/n; u=1-t
        yield (u**3*p0[0]+3*u*u*t*p1[0]+3*u*t*t*p2[0]+t**3*p3[0], u**3*p0[1]+3*u*u*t*p1[1]+3*u*t*t*p2[1]+t**3*p3[1], t)
y,x=np.mgrid[0:S,0:S]
k=np.clip((x*0.35+y*0.65)/S,0,1)
a=np.array([0x1B,0x4A,0x37]);b=np.array([0x6E,0x82,0x3C])
fondo=(a[None,None,:]*(1-k[...,None])+b[None,None,:]*k[...,None]).astype(np.uint8)
lienzo=Image.fromarray(fondo,'RGB').convert('RGBA')
capa=Image.new('RGBA',(S,S),(0,0,0,0)); d=ImageDraw.Draw(capa)
d.ellipse((-200,700,700,1300),fill=(23,58,43,255))
d.ellipse((350,760,1250,1350),fill=(36,92,62,255))
def palma(x,base,cima,ancho):
    d.polygon([(x-ancho,base),(x+ancho,base),(x+ancho*0.55,cima),(x-ancho*0.55,cima)],fill=(239,226,198,255))
    for ang in (-150,-120,-90,-60,-30):
        r=math.radians(ang); lx=x+math.cos(r)*95; ly=cima+math.sin(r)*70+30
        d.line([(x,cima),(lx,ly)],fill=(176,203,106,255),width=16)
        d.ellipse((lx-9,ly-9,lx+9,ly+9),fill=(176,203,106,255))
palma(300,800,300,15); palma(440,800,400,13); palma(210,800,470,11)
hoja=[]
for (px,py,t) in curva((640,820),(560,600),(760,420),(880,330),60): hoja.append((px-70*math.sin(math.pi*t),py))
for (px,py,t) in reversed(list(curva((640,820),(760,720),(900,560),(880,330),60))): hoja.append((px+0*t,py))
d.polygon(hoja,fill=(143,194,92,255))
d.line([(640,820),(700,640),(790,480),(878,334)],fill=(227,176,75,255),width=10)
for (px,py,t) in curva((540,990),(380,900),(700,820),(540,640),160):
    r=70*(1-t)+8
    d.ellipse((px-r,py-r*0.55,px+r,py+r*0.55),fill=(227,176,75,255))
cx,cy=190,860
d.ellipse((cx-70,cy-95,cx+70,cy+95),fill=(194,96,60,255))
d.arc((cx-45,cy-80,cx+45,cy+80),100,260,fill=(120,52,30,255),width=14)
sombra=capa.filter(ImageFilter.GaussianBlur(14)).point(lambda v:int(v*0.35))
sh=Image.new('RGBA',(S,S),(0,0,0,0)); sh.putalpha(sombra.split()[3])
lienzo=Image.alpha_composite(lienzo,sh.transform(sh.size,Image.AFFINE,(1,0,0,0,1,-10)))
lienzo=Image.alpha_composite(lienzo,capa)
brillo=Image.new('RGBA',(S,S),(255,255,255,0)); bd=ImageDraw.Draw(brillo)
bd.ellipse((-150,-520,1170,420),fill=(255,255,255,34))
lienzo=Image.alpha_composite(lienzo,brillo)
m=Image.new('L',(S*2,S*2),0); ImageDraw.Draw(m).rounded_rectangle((0,0,S*2-1,S*2-1),radius=int(S*2*0.23),fill=255)
m=m.resize((S,S),Image.LANCZOS)
lienzo.putalpha(m)
borde=Image.new('RGBA',(S,S),(0,0,0,0))
bm=Image.new('L',(S*2,S*2),0); ImageDraw.Draw(bm).rounded_rectangle((6,6,S*2-7,S*2-7),radius=int(S*2*0.23),outline=255,width=10)
borde.putalpha(bm.resize((S,S),Image.LANCZOS).point(lambda v:int(v*0.35))); borde.paste((255,255,255,255),mask=borde.split()[3])
lienzo=Image.alpha_composite(lienzo,borde)
import os
os.makedirs('out',exist_ok=True)
for t in (256,64,32,16): lienzo.resize((t,t),Image.LANCZOS).save(f'out/icono-{t}.png')
lienzo.resize((512,512),Image.LANCZOS).save('out/icono-512.png')
hoja=Image.new('RGBA',(640,300),(251,246,236,255)); 
x0=20
for t in (256,64,32,16):
    im=Image.open(f'out/icono-{t}.png'); hoja.paste(im,(x0,20),im); x0+=t+30
hoja.save('out/muestra.png')
