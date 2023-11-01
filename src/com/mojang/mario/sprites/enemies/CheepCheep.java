package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Bubble;
import com.mojang.mario.sprites.Enemy;
import com.mojang.mario.sprites.Sparkle;

public class CheepCheep extends Enemy {

	private float originalY;
	private boolean jumping = false;
    private int type; //0==dasher, 1==seeker
	
	public CheepCheep(LevelScene world, int x, int y, int dir, int type){
		super(world, x, y, dir, false);

        this.type = type;

		int xPicBase = 0;
		if (type == 0){
			xPicBase = 6;
			if (Math.random() > 0.5f) xPicBase = 8;
		}
		if (type == 1) xPicBase = 10;
		setXPicBase(xPicBase);
		setXPic(getXPicBase());
		setYPic(2);
		setHeight(14);
		originalY = y;
		setXa(facing);
		setYa(0);

    	squishable = true;
        swims = true;       //only moves freely in water
	}
	
    public void move(){
        if (deadTime > 0){
            deadTime--;

            if (deadTime == 0){
                deadTime = 1;
                for (int i = 0; i < 8; i++)
                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int) (getY() - Math.random() * 8) + 4, (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
                spriteContext.removeSprite(this);
            }

            if (flyDeath){
                setX(getX()+getXa());
                setY(getY()+getYa());
                setYa((getYa() * 0.95f)+1);
            }
            return;
        }

        if (getXa() > 0) facing = 1;
        if (getXa() < 0) facing = -1;
        setXFlipPic(facing == -1);

        runTime += (Math.abs(getXa())) + 5;
        int runFrame = ((int) (runTime / 20)) % 2;
        setXPic(getXPicBase()+runFrame);

        if (!move(getXa(), 0)){
        	facing = -facing;
        	setXa(facing);
        }
        onGround = false;
        move(0, getYa());

        //gravity if in air
        if (jumping){
        	setYa(getYa()+0.2f);
        	
        	//shortcut jump when get back to original depth
        	if (getY() >= originalY  || onGround){
        		jumping = false;
        	}
        }

        //kind-based behaviors
        if (type == 0){
        	wander();
        }
        else if (type == 1){
        	if (Math.abs(getX()-getWorld().mario.getX()) < 128 && Math.abs(getY()-getWorld().mario.getY()) < 128 &&
        			getWorld().mario.inWater){
        		seek();
        	}
        	else{
        		setXa(facing);
        		wander();
        	}
        }

    	//possibly create a bubble
    	if (!jumping && Math.random() > 0.96){
    		spriteContext.addSprite(new Bubble(getWorld(), getX(), getY()-getHeight()+4));
    	}
    }

    //move back and forth - no difference
    private void wander(){}
    
    private void seek(){
    	//horizontal seeking
    	float speed = 0.1f;
    	if (getX() < getWorld().mario.getX()) setXa(getXa()+speed); 
    	else setXa(getXa()-speed);

    	//vert seeking
    	if (getY() < getWorld().mario.getY()) setYa(getYa()+speed); 
    	else setYa(getYa()-speed);
    	
    	//speed limits
    	int limit = 2;
    	if (getXa() > limit) setXa(limit);
    	else if (getXa() < -limit) setXa(-limit);
    	if (getYa() > limit) setYa(limit);
    	else if (getYa() < -limit) setYa(-limit);
    }
}
