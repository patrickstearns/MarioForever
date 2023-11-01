package com.mojang.mario.sprites;

import com.mojang.mario.*;
import com.mojang.mario.level.*;

public class CoinAnim extends Sprite {

	private int life = 10;

    public CoinAnim(int xTile, int yTile){
    	super(null);
    	setSheet(Art.level);
        setWPic(16);
        setHPic(16);
        setX(xTile * 16);
        setY(yTile * 16 - 16);
        setXa(0);
        setYa(-6f);
        setXPic(Level.COIN%16);
        setYPic(Level.COIN/16);
    }

    public void move(){
        if (life-- < 0){
            Sprite.spriteContext.removeSprite(this);
            for (int xx = 0; xx < 2; xx++)
                for (int yy = 0; yy < 2; yy++)
                    Sprite.spriteContext.addSprite(new Sparkle((int)getX() + xx * 8 + (int) (Math.random() * 8), (int)getY() + yy * 8 + (int) (Math.random() * 8), 0, 0, 0, 2, 5));
        }

        setXPic(Level.COIN%16+(life & 3));
        setX(getX()+getXa());
        setY(getY()+getYa());
        setYa(getYa()+1);
    }
}