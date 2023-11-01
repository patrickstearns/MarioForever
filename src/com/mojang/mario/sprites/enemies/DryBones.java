package com.mojang.mario.sprites.enemies;

import java.awt.*;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.*;

public class DryBones extends Enemy {

	private int crumbledTime;
	
	public DryBones(LevelScene world, int x, int y, int dir){
		super(world, x, y, dir, false);
		setXPicBase(7);
        setXPic(getXPicBase());
		setYPic(0);
		setHeight(24);
		crumbledTime = 0;
		
		squishable = false;
        immuneToFireballs = true;
        avoidsCliffs = true;
	}

    public void move(){
    	if (crumbledTime > 0){ //is crumbly
    		setXPicO(16);
    		crumbledTime--;
    		if (crumbledTime <= 0){
    			setXPicO(8);
    			setLayer(0);
    			crumbledTime = 0;
    			tangible = true;
    		}
    		if (crumbledTime < 25 && ((crumbledTime/2)%2) == 0) setXPic(getXPicBase()+2);
    		else if (crumbledTime >= 97) setXPic(getXPicBase()+2);
    		else setXPic(getXPicBase()+4);
    		
	        if (!onGround){ //so it still falls when crumbled
	            setYa(getYa() + 2);
	            move(0, getYa());
	        }
    		
    	}
    	else super.move();
    }
	
	public boolean stomped(){ 
		crumbledTime = 100;
		setLayer(-1);
		tangible = false;
		return false;
	}
	
    public void render(Graphics og, float alpha){
    	if (crumbledTime == 0){
    		super.render(og, alpha);
    		return;
    	}

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

        if (isXFlipPic()){
	        og.drawImage(getSheet()[getXPic()+1][getYPic()], xPixel+getWPic(), 
	        		yPixel+(isYFlipPic() ? getHPic() : 0), -getWPic(), 
	        		isYFlipPic() ? -getHPic() : getHPic(), null);
	        og.drawImage(getSheet()[getXPic()][getYPic()], xPixel+getWPic()+getWPic(), 
	        		yPixel+(isYFlipPic() ? getHPic() : 0), -getWPic(), 
	        		isYFlipPic() ? -getHPic() : getHPic(), null);
        }
        else{
	        og.drawImage(getSheet()[getXPic()][getYPic()], xPixel, 
	        		yPixel+(isYFlipPic() ? getHPic() : 0), getWPic(), 
	        		isYFlipPic() ? -getHPic() : getHPic(), null);
	        og.drawImage(getSheet()[getXPic()+1][getYPic()], xPixel+getWPic(), 
	        		yPixel+(isYFlipPic() ? getHPic() : 0), getWPic(), 
	        		isYFlipPic() ? -getHPic() : getHPic(), null);
        }
    }
}
