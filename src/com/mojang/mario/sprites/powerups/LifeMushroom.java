package com.mojang.mario.sprites.powerups;

import com.mojang.mario.scene.LevelScene;

public class LifeMushroom extends Mushroom {

    public LifeMushroom(LevelScene world, int x, int y, boolean sproutUp, boolean moveRight){
    	super(world, x, y, sproutUp, moveRight);
        
        setXPicO(8);
        setYPicO(15);
        setXPic(2);
        setYPic(0);
    }

    public void collideCheck(){
        float xMarioD = getWorld().mario.getX() - getX();
        float yMarioD = getWorld().mario.getY() - getY();
        if (xMarioD > -16 && xMarioD < 16){
            if (yMarioD > -getHeight() && yMarioD < getWorld().mario.getHeight()){
                getWorld().mario.getLifeMushroom();
                spriteContext.removeSprite(this);
            }
        }
    }
}