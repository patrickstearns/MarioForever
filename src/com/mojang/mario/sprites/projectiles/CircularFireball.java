package com.mojang.mario.sprites.projectiles;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Sprite;

public class CircularFireball extends Fireball {

    private int anim;
    private int radius;
    private double centerX, centerY;
    
    public CircularFireball(LevelScene world, float x, float y, Sprite source, int radius){
    	super(world, x, y, 1, source, false, 1);
    	centerX = x;
    	centerY = y;
    	this.radius = radius;
    	setPersistent(true);
    }

    public void move(){
        getWorld().checkFireballCollide(this);
        
        anim++;
        setXPic(3-((anim/2) % 4));

        //set x and y directly
        setX((float)(8+centerX+radius*Math.cos((double)anim/25d)));
        setY((float)(8+centerY+radius*Math.sin((double)anim/25d)));
    }
}