package com.mojang.mario.sprites;

import com.mojang.mario.Art;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.level.Level;

public class Beanstalk extends Sprite {

    private int life;
    private boolean dead, sproutUp;

    public Beanstalk(LevelScene world, int x, int y, boolean sproutUp){
    	super(world);
    	setSheet(Art.items);

        setX(x);
        setY(y);
        if (!sproutUp) setY(getY()+16);
        
        setXPicO(8);
        setYPicO(15);

        setXPic(0);
        setYPic(7);

        setWidth(4);
        setHeight(12);
        
        setWPic(16);
        setHPic(16);
        
       	setYFlipPic(!sproutUp);
       	setLayer(-1);

       	life = 0;
        this.sproutUp = sproutUp;
    	setPersistent(true);
    }

    public void move(){
    	//if sprouting, sprout in whatever direction with puff of smoke
    	if (life<12){
            if (sproutUp) setY(getY()-1);
            else setY(getY()+1);

            if (life == 0){
                for (int i = 0; i < 24; i++)
                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int)getY(), (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
            }
        }
        else{
            setLayer(2);

            //turn tile behind to vine
        	int ix = (int)(getX()/16);
        	int iy = (int)(getY()/16);
        	if (!sproutUp) iy--;
        	if (getWorld().level.getBlock(ix, iy).blockId != Level.VINE_BLOCK &&
        			!Level.isDeadBlockId(getWorld().level.getBlock(ix, iy).blockId)){
        		getWorld().level.setBlock(ix, iy, Level.VINE_BLOCK);
        	}

        	//move whichever direction
        	int ya = sproutUp ? -2 : 2;
        	if (!move(ya)) dead = true;
        	
        	//if dead go up in puff of smoke
        	if (dead){
        		if (!sproutUp){
        			iy++;
	        		if (getWorld().level.getBlock(ix, iy).blockId != Level.VINE_BLOCK &&
		        			!Level.isDeadBlockId(getWorld().level.getBlock(ix, iy).blockId)){
		        		getWorld().level.setBlock(ix, iy, Level.VINE_BLOCK);
		        	}
        		}	        		
        		
                for (int i = 0; i < 24; i++)
                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int)getY(), (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));

                spriteContext.removeSprite(this);
        	}
        	
        }

        //increment counter and next animation frame
        life++;
       	setXPic((life/2)%4);
    }
    
    protected boolean move(float ya){
        while (ya > 8){
            if (!move(0, 8)) return false;
            ya -= 8;
        }
        while (ya < -8){
            if (!move(0, -8)) return false;
            ya += 8;
        }

        boolean collide = false;
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        if (ya > 0){
            if (isBlocking(x - width, y + ya, 0, 0)) collide = true;
            else if (isBlocking(x + width, y + ya, 0, 0)) collide = true;
            else if (isBlocking(x - width, y + ya + 1, 0, ya)) collide = true;
            else if (isBlocking(x + width, y + ya + 1, 0, ya)) collide = true;
        }
        if (ya < 0){
            if (isBlocking(x, y + ya - height, 0, ya)) collide = true;
            else if (collide || isBlocking(x - width, y + ya - height, 0, ya)) collide = true;
            else if (collide || isBlocking(x + width, y + ya - height, 0, ya)) collide = true;
        }

		if (collide){
            if (ya < 0){
                setY((int) ((y - height) / 16) * 16 + height);
                setYa(0);
            }
            if (ya > 0){
                setY((int) (y / 16 + 1) * 16 - 1);
            }
            return false;
        }
        else{
            setY(getY() + ya);
            return true;
        }
    }

    protected boolean isBlocking(float _x, float _y, float xa, float ya){
        int x = (int) (_x / 16);
        int y = (int) (_y / 16);
        if (x == (int) (getX()/16) && y == (int) (getY()/16)) return false;
        boolean blocking = getWorld().level.isBlocking(x, y, xa, ya, _x, _y);
        return blocking;
    }    
}