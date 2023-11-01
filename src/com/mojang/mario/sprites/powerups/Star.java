package com.mojang.mario.sprites.powerups;

import com.mojang.mario.*;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Sprite;

public class Star extends Sprite {

	protected static float GROUND_INERTIA = 0.89f;
    protected static float AIR_INERTIA = 0.89f;

    protected float runTime;
    protected boolean onGround = false;
    protected boolean sproutUp = true;
    
    public int facing;
    protected int life, jumpTime;
    protected float yJumpSpeed;

    public Star(LevelScene world, int x, int y, boolean sproutUp, boolean moveRight){
    	super(world);
    	setSheet(Art.items);

        setX(x);
        setY(y);

        this.sproutUp = sproutUp;
        facing = moveRight ? 1 : -1;

        if (sproutUp) setY(getY()+16);
        
        life = 0;
        jumpTime = 0;
        
        setXPic(0);
        setYPic(1);

        setWPic(16);
        setHPic(16);

        setXPicO(8);
        setYPicO(15);

        setWidth(8);
        setHeight(16);
    }

    public void collideCheck(){
        float xMarioD = getWorld().mario.getX() - getX();
        float yMarioD = getWorld().mario.getY() - getY();
        if (xMarioD > -16 && xMarioD < 16){
            if (yMarioD > -getHeight() && yMarioD < getWorld().mario.getHeight()){
                getWorld().mario.getStar();
                spriteContext.removeSprite(this);
            }
        }
    }

    public boolean knockCheck(int xTile, int yTile, boolean up, boolean down, boolean left, boolean right){
    	boolean ret = super.knockCheck(xTile, yTile, up, down, left, right);
    	if (ret) facing = -1*facing;
    	return ret;
    }

    public void move(){
        if (life<9){
            setLayer(0);
            if (sproutUp) setY(getY()-1);
            else setY(getY()+1);
            life++;
            return;
        }
        float sideWaysSpeed = 2f;
        setLayer(1);

        if (getXa() > 2) facing = 1;
        if (getXa() < -2) facing = -1;
        
        setXa(facing * sideWaysSpeed);
        setXFlipPic(facing == -1);

        setXPic(getXPic()+1);
        if (getXPic() > 4) setXPic(0);

        //in the air and going down
    	if (jumpTime < 0){
            setYa(-jumpTime * yJumpSpeed);
            jumpTime++;
    	}
        //jump
        else if (onGround){
            yJumpSpeed = -0.7f;
            jumpTime = 12;
            onGround = false;
        }
    	//if on the way up
        else if (jumpTime > 0){
            setYa(jumpTime * yJumpSpeed);
            jumpTime--;
        }
        else jumpTime = 0;

        runTime += (Math.abs(getXa())) + 5;
        if (!move(getXa(), 0)) facing = -facing;
        onGround = false;
        move(0, getYa());
        if (!onGround) setYa(getYa()+3);
        else setYa(0);
    }

    protected boolean move(float xa, float ya){
    	float step = 4f;
    	while (xa > step){
            if (!move(step, 0)) return false;
            xa -= step;
        }
        while (xa < -step){
            if (!move(-step, 0)) return false;
            xa += step;
        }
        while (ya > step){
            if (!move(0, step)) return false;
            ya -= step;
        }
        while (ya < -step){
            if (!move(0, -step)) return false;
            ya += step;
        }

        boolean collide = false;
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        if (ya > 0){
	        if (collide || isBlocking(x + xa - width, y + ya, xa, 0)) collide = true;
            else if (collide || isBlocking(x + xa + width, y + ya, xa, 0)) collide = true;
            else if (collide || isBlocking(x + xa - width, y + ya + 1, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa + width, y + ya + 1, xa, ya)) collide = true;
        }
        if (ya < 0){
            if (collide || isBlocking(x + xa, y + ya - height, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
        }
        if (xa > 0){
            if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
            if (collide || isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) collide = true;
            if (collide || isBlocking(x + xa + width, y + ya, xa, ya)) collide = true;
        }
        if (xa < 0){
            if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
            if (collide || isBlocking(x + xa - width, y + ya - height / 2, xa, ya)) collide = true;
            if (collide || isBlocking(x + xa - width, y + ya, xa, ya)) collide = true;
        }
        
        if (collide){
        	float newX = x, newY = y;
        	
        	if (xa < 0){
                newX = (int) ((newX - width) / 16) * 16 + width;
                setXa(0);
            }
            if (xa > 0){
	        	newX = (int) ((newX + width) / 16 + 1) * 16 - width - 1;
                setXa(0);
            }
            if (ya < 0){
				newY = (int) ((newY - height) / 16) * 16 + height;
                setYa(0);
            }
            if (ya > 0){
				newY = (int) ((newY - 1) / 16 + 1) * 16 - 1 ;//+ getWorld().level.yOffsets[ix][iy];
      			onGround = true;
            }

        	if (newX != x || newY != y){
	        	setX(newX);
	        	setY(newY);
        	}
        	
            return false;
        }
        else{
			setX(x+xa);
            setY(y+ya);
            return true;
        }
    }

    private boolean isBlocking(float _x, float _y, float xa, float ya){
        int x = (int) (_x / 16);
        int y = (int) (_y / 16);
        if (x == (int)(getX() / 16) && y == (int)(getY()/16)) return false;

        boolean blocking = getWorld().level.isBlocking(x, y, xa, ya, _x, _y);

        return blocking;
    }

    public void bumpCheck(int xTile, int yTile){
        if (getX() + getWidth() > xTile * 16 && getX() - getWidth() < xTile * 16 + 16 && yTile==(int)((getY()-1)/16)){
        	if (getX() < xTile*16+8) //go left
        		facing = -1;
        	else // go right
        		facing = 1;
            setYa(-10);
        }
    }
}