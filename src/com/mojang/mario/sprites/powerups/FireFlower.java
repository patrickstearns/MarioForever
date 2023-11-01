package com.mojang.mario.sprites.powerups;

import com.mojang.mario.Art;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Sprite;

public class FireFlower extends Sprite {

    private boolean sproutUp;
    public boolean avoidCliffs = false;
    public int facing;
    private int life;

    public FireFlower(LevelScene world, int x, int y, boolean sproutUp){
    	super(world);
    	setSheet(Art.items);

        setX(x);
        setY(y);
        this.sproutUp = sproutUp;
        
        setXPicO(8);
        setYPicO(15);

        setXPic(1);
        setYPic(0);
        setHeight(12);
        facing = 1;
        setWPic(16);
        setHPic(16);
        life = 0;
    }

    public void collideCheck(){
        float xMarioD = getWorld().mario.getX() - getX();
        float yMarioD = getWorld().mario.getY() - getY();
        if (xMarioD > -16 && xMarioD < 16){
            if (yMarioD > -getHeight() && yMarioD < getWorld().mario.getHeight()){
                getWorld().mario.getFlower();
                spriteContext.removeSprite(this);
            }
        }
    }

    public void move(){
        if (life<9){
            setLayer(0);
            if (sproutUp) setY(getY()-1);
            else setY(getY()+1);
            life++;
        }
        else{
        	if (!getWorld().level.isBlocking((int)getX()/16, (int)getY()/16, 0f, 1f, getX(), getY())) setYa((getYa()*0.95f)+1);
        	else setYa(0);

        	for (int i = 0; i < getYa(); i++){
        		if (!getWorld().level.isBlocking((int)getX()/16, (int)getY()/16, 0f, 1f, getX(), getY()))
        			setY(getY()+1);
        	}
        	
        	life++;
        }
    }
}