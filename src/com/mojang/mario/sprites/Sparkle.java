package com.mojang.mario.sprites;

import com.mojang.mario.Art;

public class Sparkle extends Sprite{

	public int life;
    public int xPicStart;
    
    public Sparkle(int x, int y, float xa, float ya){ this(x, y, xa, ya, 1+(int)(Math.random()*2), 0, 5); }
    public Sparkle(int x, int y, float xa, float ya, int xPic, int yPic, int timeSpan){
    	super(null);
    	setSheet(Art.particles);
        setX(x);
        setY(y);
        setXa(xa);
        setYa(ya);
        setXPic(xPic);
        xPicStart = xPic;
        setYPic(yPic);
        setXPicO(4);
        setYPicO(4);
        
        setWPic(8);
        setHPic(8);
        life = 10+(int)(Math.random()*timeSpan);
    }

    public void move(){
        if (life > 10) setXPic(1);
        else setXPic(xPicStart+(10-life)*3/10);
        
        if (life-- < 0) Sprite.spriteContext.removeSprite(this);

        setX(getX()+getXa());
        setY(getY()+getYa());
    }
}