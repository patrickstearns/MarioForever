package com.mojang.mario.sprites.enemies;

import java.awt.*;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.*;

public class Lakitu extends Enemy {

	private int cloudXPic = 2, tick = 0, throwTime = 0;
	
	public Lakitu(LevelScene world, int x, int y, int dir){
		super(world, x, y, dir, false);
		setXPicBase(3);
        setXPic(3);
		setYPic(4);
		setHeight(24);
		squishable = false;
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

            setXa(getXa()/2);
            
            setX(getX()+getXa());
            setY(getY()+getYa());
            setYa((getYa()*0.95f)+1);
            return;
        }

        tick++;
        throwTime++;

    	float xSpeed = 0.15f;
    	int xd = (int)(getWorld().mario.getX()-getX());
    	int maxXD = 2;
    	if (xd > maxXD) xd = maxXD;
    	else if (xd < -maxXD) xd = -maxXD;
    	
    	float xa = getXa()+xd*xSpeed;
    	if (xa > 8) xa = 8;
    	if (xa < -8) xa = -8;
    	setXa(xa);
    	
    	if (throwTime > 60){
    		throwSpiny((int)Math.signum(xd), -getXa());
    		throwTime = 0;
    	}
    	
    	setX(getX()+getXa());

    	int xPic = 0;
    	if (throwTime < 2) xPic = 4;
    	else if (throwTime < 4) xPic = 3;
    	else if (throwTime < 6) xPic = 2;
    	else if (throwTime < 8) xPic = 1;
    	else if (throwTime < 52) xPic = 0;
    	else if (throwTime < 54) xPic = 1;
    	else if (throwTime < 56) xPic = 2;
    	else if (throwTime < 58) xPic = 3;
    	else xPic = 4;
    	setXPic(getXPicBase()+xPic);
    }

    private void throwSpiny(int dir, float xSpeed){
    	Enemy egg = Enemy.create(Kind.SpinyEgg, getWorld(), (int)getX(), (int)(getY()-getHeight()), dir);
    	egg.setXa(xSpeed);
    	egg.setYa(-2f);
    	getWorld().addSprite(egg);
    }
    
    public void render(Graphics og, float alpha){
    	//following duped from Sprite
    	if (!isVisible()) return;
        int xPixel = (int)(getXOld()+(getX()-getXOld())*alpha)-getXPicO();
        int yPixel = (int)(getYOld()+(getY()-getYOld())*alpha)-getYPicO();
        if (Mario.instance != null){
        	int ix = (int)(getX()/16), iy = (int)(getY()/16)+1;

        	if (ix >= 0 && ix < Mario.instance.getWorld().level.getWidth() &&
        			iy >= 0 && iy < Mario.instance.getWorld().level.getHeight()){
	        	int xo = Mario.instance.getWorld().level.getXOffsets()[ix][iy];
	        	int yo = Mario.instance.getWorld().level.getYOffsets()[ix][iy];
	
	        	if (xo != 0){
		        	if (xo > 0) xo = (int) (Math.cos((xo - alpha) / 4.0f * Math.PI) * 8);
		        	else if (xo < 0) xo = -(int)(Math.cos((-xo - alpha) / 4.0f * Math.PI) * 8);
		        	xPixel -= xo;
	        	}
	        	if (yo != 0){
		        	if (yo > 0) yo = (int) (Math.sin((yo - alpha) / 4.0f * Math.PI) * 8);
		        	else if (yo < 0) yo = -(int)(Math.sin((-yo - alpha) / 4.0f * Math.PI) * 8);
		        	yPixel -= yo;
	        	}
        	}
        }

        og.drawImage(getSheet()[flyDeath ? getXPic() : cloudXPic][getYPic()], xPixel+(isXFlipPic()?getWPic():0), yPixel+(isYFlipPic()?getHPic():0), 
        		isXFlipPic()?-getWPic():getWPic(), isYFlipPic()?-getHPic():getHPic(), null);
        og.drawImage(getSheet()[flyDeath ? cloudXPic : getXPic()][getYPic()], xPixel+(isXFlipPic()?getWPic():0), yPixel+(isYFlipPic()?getHPic():0)-16, 
        		isXFlipPic()?-getWPic():getWPic(), isYFlipPic()?-getHPic():getHPic(), null);
    }
}
