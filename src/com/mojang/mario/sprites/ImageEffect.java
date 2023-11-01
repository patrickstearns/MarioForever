package com.mojang.mario.sprites;

import com.mojang.mario.Art;

public class ImageEffect extends Sprite{

	public int life;
    public int xPicStart;
    public int type;
    
    public ImageEffect(int x, int y, int type){
    	super(null);
    	setSheet(Art.effects);
        
    	setX(x);
        setY(y);
        
        setXa(0);
        setYa(0);

        setXPicO(16);
        setYPicO(31);
        
        setWPic(32);
        setHPic(32);

        if (type == 0){ //BOMB!
        	setXPic(0);
	        life = 10;
        }
        else if (type == 1){ //1UP
        	setY(getY()-16);
        	setYa(-1);

        	setXPic(2);
	        life = 20;
        }
        else if (type == 2){ //3UP
        	setY(getY()-16);
        	setYa(-1);

        	setXPic(3);
	        life = 20;
        }
        setYPic(0);
        xPicStart = getXPic();
        
        this.type = type;
    }

    public void move(){
    	if (type == 0){
    		setXPic((life/2)%2);
    	}
        
        if (life-- < 0) Sprite.spriteContext.removeSprite(this);

        setX(getX()+getXa());
        setY(getY()+getYa());
    }
}