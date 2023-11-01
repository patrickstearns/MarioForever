package com.mojang.mario.sprites.projectiles;

import com.mojang.mario.Art;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Projectile;
import com.mojang.mario.sprites.Sparkle;
import com.mojang.mario.sprites.Sprite;

public class Hammer extends Projectile {
    private static float GROUND_INERTIA = 0.97f;
    private static float AIR_INERTIA = GROUND_INERTIA;

    public boolean avoidCliffs = false;
    public boolean dead = false;
    public int anim;
    public int facing, vfacing;
    private int deadTime = 0;
    private float runTime;
    private Sprite source;
    
    public Hammer(LevelScene world, float x, float y, int facing, Sprite source, int vfacing){
    	super(world);
    	setSheet(Art.bigParticles);

        setX(x);
        setY(y);
        setXPicO(4);
        setYPicO(4);
        setXPic(0);
        setYPic(0);
        setWPic(16);
        setHPic(16);
        setXa(facing * 3f);
        setYa(-30);
        setWidth(8);
        setHeight(16);
        this.facing = facing;
        this.vfacing = vfacing;
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

        anim++;

        getWorld().checkProjectileCollide(this);
        
        runTime += (Math.abs(getXa())) + 5;
        
        int animFrame = anim%8;
        if (animFrame < 4){
        	setXPic(animFrame);
        	setYPic(0);
        }
        else{
        	setXPic(7-animFrame);
        	setYPic(1);
        }

        if (!move(getXa(), 0)) die();
        move(0, getYa());
        setXa(getXa() * AIR_INERTIA);
//        setYa(getYa() * 0.95f); //was 0.95f
        setYa(getYa()+3);
    }

    protected boolean move(float xa, float ya){
    	int speed = 8;
        while (xa > speed){
            xa -= speed;
        }
        while (xa < -speed){
            xa += speed;
        }
        while (ya > speed){
            ya -= speed;
        }
        while (ya < -speed){
            ya += speed;
        }

        setX(getX()+xa);
        setY(getY()+ya);
        return true;
    }

    public void die(){
        dead = true;
        setXa(-facing * 2);
        setYa(-5);
        deadTime = 100;
    }
}