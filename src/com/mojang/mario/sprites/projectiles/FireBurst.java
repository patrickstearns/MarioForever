package com.mojang.mario.sprites.projectiles;

import com.mojang.mario.Art;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Projectile;
import com.mojang.mario.sprites.Sparkle;
import com.mojang.mario.sprites.Sprite;

public class FireBurst extends Projectile {

    public boolean dead = false;
    public int anim;
    public int facing, vfacing;
    private int deadTime = 0;
    private Sprite source;
    
    public FireBurst(LevelScene world, float x, float y, int facing, Sprite source, int ya){
    	super(world);
    	setSheet(Art.bigParticles);

        setX(x);
        setY(y);
        setXPicO(4);
        setYPicO(4);
        setXPic(0);
        setYPic(3);
        setWPic(16);
        setHPic(16);
        setXa(facing * 1.5f);
        setYa(ya/2f);
        if (ya == 0) setYa(-0.1f);
        setWidth(8);
        setHeight(16);
        this.facing = facing;
        this.source = source;
    }

    public boolean isDead(){ return dead; }
    public Sprite getSource(){ return source; }
    
    public void move(){
        if (deadTime > 0){
            for (int i = 0; i < 8; i++)
                getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 8 - 4)+4, (int) (getY() + Math.random() * 8-4)+2, (float) Math.random() * 2 - 1-facing, (float) Math.random() *2 -1, 0, 1, 5));
            spriteContext.removeSprite(this);
            return;
        }

        getWorld().checkProjectileCollide(this);
        
        anim++;
        int animFrame = (anim/4)%3;
        setXPic(animFrame);
        setXFlipPic(facing < 0);
        
        if (!move(getXa(), 0)) die();
        else setX(getX()+getXa());
        if (!move(0, getYa())) die();
        else setY(getY()+getYa());
    }

    public void die(){
        dead = true;
        deadTime = 1;
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
        }
        if (xa < 0){
            if (isBlocking(x + xa - getWidth(), y + ya - getHeight(), xa, ya)) collide = true;
            if (isBlocking(x + xa - getWidth(), y + ya - getHeight() / 2, xa, ya)) collide = true;
            if (isBlocking(x + xa - getWidth(), y + ya, xa, ya)) collide = true;
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
}