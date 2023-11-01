package com.mojang.mario.sprites.projectiles;

import com.mojang.mario.Art;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Projectile;
import com.mojang.mario.sprites.Sparkle;
import com.mojang.mario.sprites.Sprite;

public class Boomerang extends Projectile {

    private boolean dead = false;
    public int anim;
    public int facing;
    private int deadTime = 0;
    private float runTime;
    private Sprite source;
    
    public Boomerang(LevelScene world, float x, float y, int facing, Sprite source, boolean thrownInAir){
    	super(world);
    	setSheet(Art.bigParticles);

        setX(x);
        setY(y);
        setXPicO(4);
        setYPicO(4);
        setXPic(0);
        setYPic(2);
        setWPic(16);
        setHPic(16);
        setXa(facing * 6f);

        if (thrownInAir) setYa(1f);
        else setYa(-1f);
        
        setWidth(8);
        setHeight(16);
        this.facing = facing;
        this.source = source;
    }

    public Sprite getSource(){ return source; }
    public boolean isDead(){ return dead; }
    
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
        
        int animFrame = (anim%8)/2;
    	setXPic(3-animFrame); //4- cuz boomerang sprites are in reverse order

        move(getXa(), 0);
        move(0, getYa());

        setXa((float)(getXa() + -facing * 0.15f));
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