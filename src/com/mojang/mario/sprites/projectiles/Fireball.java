package com.mojang.mario.sprites.projectiles;

import com.mojang.mario.Art;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Sparkle;
import com.mojang.mario.sprites.Sprite;

public class Fireball extends Sprite {
    private static float GROUND_INERTIA = 0.89f;

    private boolean onGround = false;
    public boolean avoidCliffs = false;
    public boolean dead = false;
    public int anim;
    public int facing, vfacing;
    private int deadTime = 0;
    private float runTime;
    private Sprite source;
    private boolean targeted;
    private boolean bouncedOnce;
    
    public Fireball(LevelScene world, float x, float y, int facing, Sprite source, boolean targeted, int vfacing){
    	super(world);
    	setSheet(Art.particles);

        setX(x);
        setY(y);
        setXPicO(4);
        setYPicO(4);
        setXPic(4);
        setYPic(3);
        setWPic(8);
        setHPic(8);
        setYa(4);
        setWidth(3);
        setHeight(4);
        this.facing = facing;
        this.vfacing = vfacing;
        this.source = source;
        this.targeted = targeted;
        this.bouncedOnce = false;
    }

    public Sprite getSource(){ return source; }
    
    public void move(){
        if (deadTime > 0){
            for (int i = 0; i < 8; i++)
                getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 8 - 4)+4, (int) (getY() + Math.random() * 8-4)+2, (float) Math.random() * 2 - 1-facing, (float) Math.random() *2 -1, 0, 1, 5));
            spriteContext.removeSprite(this);
            return;
        }

        if (facing != 0) anim++;
        if (getXa() > 0) facing = 1;
        if (getXa() < -0) facing = -1;

        if (targeted){
        	setXa(facing * 1.5f);
        }
        else if (bouncedOnce){
	        setXa(facing * 6f);
        }
        else{ //just thrown
	        setXa(facing * 6f);
	        setYa(6);
        }
        
        getWorld().checkFireballCollide(this);
        
        setXFlipPic(facing == -1);
        runTime += (Math.abs(getXa())) + 5;
        setXPic((anim) % 4);
        if (!move(getXa(), 0)) die();

        onGround = false;
        boolean ymove = move(0, getYa());
        if (targeted && !ymove) die();
        
        if (targeted){
      		setYa(vfacing);
        	if (anim%3 == 0) setYa(0);
        }
        else{
	        if (onGround){
	        	setXa(getXa() * GROUND_INERTIA);
	        	setYa(-10);
	        	bouncedOnce = true;
	        }
	        else {
	        	setXa(getXa() * GROUND_INERTIA);
	        	setYa(getYa() * 0.90f + 3); //was *0.95f and +1.2f
	        }
	        
        }
    }

    protected boolean move(float xa, float ya){
    	int speed = 8;
        while (xa > speed){
            if (!move(speed, 0)) return false;
            xa -= speed;
        }
        while (xa < -speed){
            if (!move(-speed, 0)) return false;
            xa += speed;
        }
        while (ya > speed){
            if (!move(0, speed)) return false;
            ya -= speed;
        }
        while (ya < -speed){
            if (!move(0, -speed)) return false;
            ya += speed;
        }

        boolean collide = false;
        float x = getX(), y = getY();
        if (ya > 0){
            if (isBlocking(x + xa - getWidth(), y + ya, xa, 0)) collide = true;
            else if (isBlocking(x + xa + getWidth(), y + ya, xa, 0)) collide = true;
            else if (isBlocking(x + xa - getWidth(), y + ya + 1, xa, ya)) collide = true;
            else if (isBlocking(x + xa + getWidth(), y + ya + 1, xa, ya)) collide = true;
        }
        if (ya < 0){
            if (isBlocking(x + xa, y + ya - getHeight(), xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa - getWidth(), y + ya - getHeight(), xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa + getWidth(), y + ya - getHeight(), xa, ya)) collide = true;
        }
        if (xa > 0){
            if (isBlocking(x + xa + getWidth(), y + ya - getHeight(), xa, ya)) collide = true;
            if (isBlocking(x + xa + getWidth(), y + ya - getHeight() / 2, xa, ya)) collide = true;
            if (isBlocking(x + xa + getWidth(), y + ya, xa, ya)) collide = true;

            if (avoidCliffs && onGround && !getWorld().level.isBlocking((int) ((x + xa + getWidth()) / 16), (int) ((y) / 16 + 1), xa, 1, x + xa + getWidth(), y+1)) collide = true;
        }
        if (xa < 0){
            if (isBlocking(x + xa - getWidth(), y + ya - getHeight(), xa, ya)) collide = true;
            if (isBlocking(x + xa - getWidth(), y + ya - getHeight() / 2, xa, ya)) collide = true;
            if (isBlocking(x + xa - getWidth(), y + ya, xa, ya)) collide = true;

            if (avoidCliffs && onGround && !getWorld().level.isBlocking((int) ((x + xa - getWidth()) / 16), (int) ((y) / 16 + 1), xa, 1, x + xa - getWidth(), y+1)) collide = true;
        }

        if (collide){
            if (xa < 0){
                setX((int) ((x - getWidth()) / 16) * 16 + getWidth());
                setXa(0);
            }
            if (xa > 0){
                setX((int) ((x + getWidth()) / 16 + 1) * 16 - getWidth() - 1);
                setXa(0);
            }
            if (ya < 0){
                setY((int) ((y - getHeight()) / 16) * 16 + getHeight());
                setYa(0);
            }
            if (ya > 0){
                setY((int) (y / 16 + 1) * 16 - 1);
                onGround = true;
            }
            return false;
        }
        else{
            setX(getX()+xa);
            setY(getY()+ya);
            return true;
        }
    }

    private boolean isBlocking(float _x, float _y, float xa, float ya){
        int x = (int) (_x / 16);
        int y = (int) (_y / 16);
        if (x == (int)(getX() / 16) && y == (int) (getY() / 16)) return false;

        boolean blocking = getWorld().level.isBlocking(x, y, xa, ya, _x, _y);

        //short block = getWorld().level.getBlock(x, y);

        return blocking;
    }

    public void die(){
        dead = true;
        setXa(-facing * 2);
        setYa(-5);
        deadTime = 100;
    }
}