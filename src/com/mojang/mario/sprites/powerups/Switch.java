package com.mojang.mario.sprites.powerups;

import com.mojang.mario.Art;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Sparkle;
import com.mojang.mario.sprites.Sprite;

public class Switch extends Sprite {

    private int life, flavor, deadTime;
    private boolean dead;

    public Switch(LevelScene world, int x, int y, int flavor){
    	super(world);
    	setSheet(Art.items);

        setX(x);
        setY(y);
        
        setXPicO(8);
        setYPicO(15);

        setXPic(0);
        setYPic(3+flavor);

        setWidth(8);
        setHeight(16);
        
        setWPic(16);
        setHPic(16);
        
        life = 0;
        this.flavor = flavor;
       	setPersistent(true);
    }

    public int getFlavor(){ return flavor; }
    
    public void collideCheck(){
        float xMarioD = getWorld().mario.getX() - getX();
        float yMarioD = getWorld().mario.getY() - getY();
        int width = getWidth(), height = getHeight();
        if (xMarioD > -width*2-4 && xMarioD < width*2+4){
            if (yMarioD > -height && yMarioD < getWorld().mario.getHeight()){
            	if (getWorld().mario.getYa() > 0 && yMarioD <= 0){
            		if (!dead){
	            		//bounce mario
	            		getWorld().mario.stomp(this);
	
	            		//change layer so not hittable again
	            		setLayer(0);
	
	            		//dead time
	            		dead = true;
	            		
	                    //squish
	                    setYPicO(7);
	                    setHPic(8);
	
	                    //smoke puff
		                for (int i = 0; i < 24; i++)
		                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int)getY(), (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));

		                //flip switch on level
		                getWorld().level.setSwitch(flavor);
            		}
            	}
            }
        }
    }

    public void move(){
        if (life<12){
        	setLayer(0);
        	setY(getY()-1);
            
            if (life == 0){
                for (int i = 0; i < 24; i++)
                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int)getY(), (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
            }
        }
        else{
        	setLayer(1);
        	if (!getWorld().level.isBlocking((int)getX()/16, (int)getY()/16, 0f, 1f, getX(), getY())) setYa((getYa()*0.95f)+1);
        	else setYa(0);

        	for (int i = 0; i < getYa(); i++){
        		if (!getWorld().level.isBlocking((int)getX()/16, (int)getY()/16, 0f, 1f, getX(), getY()))
        			setY(getY()+1);
        	}
        	
        	if (dead) deadTime++;
        	if (deadTime > 400){
        		getWorld().level.unsetSwitch(flavor);

                for (int i = 0; i < 24; i++)
                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int)getY(), (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));

                if (flavor == 1) spriteContext.removeSprite(this);
                else if (flavor == 0){
			        setYPicO(15);
			        setHeight(16);
			        setHPic(16);
                	dead = false;
                	deadTime = 0;
                	setLayer(1);
                }
        	}
        	
        }
       	life++;
    }
}