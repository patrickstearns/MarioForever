package com.mojang.mario.sprites.enemies;

import com.mojang.mario.*;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.*;
import com.mojang.sonar.FixedSoundSource;

public class Thwomp extends Enemy {

	private boolean ready = true;
	private float originalY;
	private float wait = 0;
	
	public Thwomp(LevelScene world, int x, int y, int dir){
		super(world, x, y, dir, false);

		sheet = Art.bigEnemies;
		setXPicBase(0);
        setXPic(getXPicBase());
        setYPic(0);
		
        setWidth(14);
		setHeight(32);
		setWPic(32);
		setHPic(32);
		setXPicO(16);
		setYPicO(31);
		
		hurtsWhenStomped = true;
		squishable = false;
        immuneToFireballs = true;
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

        int xDist = Math.abs((int)((getX()+getWidth()/2)-(getWorld().mario.getX()+getWorld().mario.getWidth()/2)));
        if (ready){
	        if (xDist < 24) drop();
	        else if (xDist < 64) shudder();
	        else rest();
        }
        else {
        	if (onGround){
        		wait++;
        		if (wait == 1){
        			getWorld().sound.play(Art.samples[Art.SAMPLE_THUD], new FixedSoundSource(getX(), getY()), 1, 1, 1);
	                for (int i = 0; i < 24; i++)
	                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int)getY(), (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
        		}
        		if (wait > 10){
        			rise();
        			wait = 0;
        		}
        	}
        	else rest(); //falling or rising
        }
        
        move(getXa(), 0);
        onGround = false;
        bumpedCeiling = false;
        move(0, getYa());

        if ((!ready && getY() <= originalY) || bumpedCeiling){
        	//setY(originalY);
        	ready = true;
        	setYa(0);
        }
    }

    private void shudder(){
    	setXPicO(16+(int)(Math.signum((Math.random()*10)-5)));
    }
    
    private void drop(){
    	setXPicO(16);
    	setYa(10);
    	originalY = getY();
    	ready = false;
    }

    private void rise(){
    	setXPicO(16);
    	setYa(-2);
    }
    
    private void rest(){
    	setXPicO(16);
    }
}
