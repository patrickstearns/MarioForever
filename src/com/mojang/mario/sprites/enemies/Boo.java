package com.mojang.mario.sprites.enemies;

import com.mojang.mario.*;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.*;

public class Boo extends Enemy {

	private boolean hiding = false;
	
	public Boo(LevelScene world, int x, int y, int dir){
		super(world, x, y, dir, false);
		setXPicBase(3);
        setXPic(0);
		setYPic(7);
		setHeight(16);
		setXa(0);
		setYa(0);
		
		squishable = false;
		hurtsWhenStomped = true;
        immuneToFireballs = true;
        canBlock = false;           //passes thru things
        knockable = false;          //can't be knocked from the side
        bumpable = false;           //can't be bumped from beneath
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

            setX(getX()+getXa());
            setY(getY()+getYa());
            setYa((getYa()*0.95f)+1);
            return;
        }

        boolean oldHiding = hiding;
        if ((getX() < getWorld().mario.getX() && getWorld().mario.facing == 1) ||
        		(getX() > getWorld().mario.getX() && getWorld().mario.facing == -1))
        	seek();
        else hide();
        
        if (oldHiding && !hiding){
            getWorld().sound.play(Art.samples[Art.SAMPLE_BOO_LAUGH], this, 1, 1, 1);
        }

        if (getXa() > 0) facing = 1;
        else if (getXa() < 0) facing = -1;
        setXFlipPic(facing == -1);
        
        move(getXa(), 0);
        move(0, getYa());
   }

    private void seek(){
    	hiding = false;
    	
    	//horizontal seeking
    	float speed = 0.1f;
    	if (getX() < getWorld().mario.getX()) setXa(getXa()+speed); 
    	else setXa(getXa()-speed);

    	//vert seeking
    	if (getY() < getWorld().mario.getY()+getWorld().mario.getHeight()/2) setYa(getYa()+speed); 
    	else setYa(getYa()-speed);
    	
    	//speed limits
    	float limit = 1.5f;
    	if (getXa() > limit) setXa(limit);
    	else if (getXa() < -limit) setXa(-limit);
    	if (getYa() > limit) setYa(limit);
    	else if (getYa() < -limit) setYa(-limit);
    	
    	//image
    	setXPic(0);
    }

    private void hide(){
    	hiding = true;
    	setXa(0);
    	setYa(0);
    	setXPic(1);
    }
}
