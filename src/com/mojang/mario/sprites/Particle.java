package com.mojang.mario.sprites;

import com.mojang.mario.Art;

public class Particle extends Sprite{

	//types: 0=yellow, 1=orange, 2=blue
	
	public int life;
    
    public Particle(int x, int y, float xa, float ya, int type){ this(x, y, xa, ya, (int)(Math.random()*2), 0, type); }
    public Particle(int x, int y, float xa, float ya, int xPic, int yPic, int type){
    	super(null);
        setSheet(Art.particles);
        setX(x);
        setY(y);
        setXa(xa);
        setYa(ya);
        setXPic(xPic+type*2);
        setYPic(yPic);
        setXPicO(4);
        setYPicO(4);
        setWPic(8);
        setHPic(8);
        life = 10;
    }

    public void move(){
        if (life-- < 0) Sprite.spriteContext.removeSprite(this);
        setX(getX()+getXa());
        setY(getY()+getYa());
        setYa((getYa()*0.95f)+3);
    }
}