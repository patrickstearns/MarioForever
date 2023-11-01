package com.mojang.mario.sprites.enemies;

import com.mojang.mario.*;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.*;
import com.mojang.mario.sprites.projectiles.Boomerang;
import com.mojang.mario.sprites.projectiles.Fireball;
import com.mojang.mario.sprites.projectiles.Hammer;

public class Bro extends Enemy {

	private int direction = 0, fireTime = 0, firingTime = 0, jumpTime = 0;
    private int fireType; //0==hammer, 1==boomerang, 2==fire
	
	public Bro(LevelScene world, int x, int y, int dir, int fireType){
		super(world, x, y, dir, false);

        this.fireType = fireType;

		int xPicBase = 0;
		if (fireType == 0) xPicBase = 7;
		else if (fireType == 1) xPicBase = 10;
		else if (fireType == 2) xPicBase = 13;
		setXPicBase(xPicBase);
        setXPic(getXPicBase());
		
		setYPic(1);
		setHeight(24);

		fireTime = (int)(Math.random()*128);
		if (fireTime < 20) fireTime = 20;

		jumpTime = (int)(Math.random()*128);
		if (jumpTime < 20) jumpTime = 20;
		
		squishable = true;
	}
	
    public void move(){
        wingTime++;
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


        float sideWaysSpeed = 1.75f;
        //float sideWaysSpeed = onGround ? 2.5f : 1.2f;

        if (getX() > getWorld().mario.getX()) facing = -1;
        else facing = 1;
        setXFlipPic(facing == -1);
        
        if (direction == 0) direction = facing;
        
        setXa(direction * sideWaysSpeed);

        runTime += (Math.abs(getXa())) + 5;
        int runFrame = ((int) (runTime / 20)) % 2;
        if (!onGround) runFrame = 1;
        if (firingTime > 0){
        	runFrame = 2;
        	if (onGround) setXa(0);
        }
        
        if (!move(getXa(), 0)) direction = -direction;
        if (runTime > 256){
        	direction = -direction;
        	runTime = 0;
        }

        onGround = false;
        move(0, getYa());

        setYa(getYa() * (winged ? 0.95f : 0.85f));
        if (onGround) setXa(getXa() * GROUND_INERTIA);
        else setXa(getXa() * AIR_INERTIA);

        if (!onGround){
            setYa(getYa() + 1);
        }

        setXPic(getXPicBase()+runFrame);

        int myProjectiles = getWorld().countProjectilesFrom(this);
        
        fireTime--;
        firingTime--;
        if (fireTime <= 0 && Math.abs(getX()-getWorld().mario.getX()) < 320 && myProjectiles < 2){
        	fire();
        	fireTime = (int)(Math.random()*128);
        	if (fireTime < 20) fireTime = 20;
        	firingTime = 5;
        }
        
        jumpTime--;
        if (jumpTime <= 0 && Math.abs(getX()-getWorld().mario.getX()) < 320 && onGround){
        	jump();
        	jumpTime = (int)(Math.random()*128);
        	if (jumpTime < 20) jumpTime = 20;
        }
        
    }
    
    private void fire(){
    	if (fireType == 0){
            getWorld().addSprite(new Hammer(getWorld(), getX()+facing*6, getY()-20, facing, this, 0));
    	}
    	if (fireType == 1){
            getWorld().addSprite(new Boomerang(getWorld(), getX()+facing*6, getY()-20, facing, this, onGround));
    	}
    	else if (fireType == 2){
    		getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_FIREBALL], this, 1, 1, 1);
            getWorld().addSprite(new Fireball(getWorld(), getX()+facing*6, getY()-20, facing, this, false, 0));
    	}
    }
    
    private void jump(){
    	setYa(-16);
    }
}
