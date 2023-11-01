package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;
import com.mojang.mario.sprites.Sparkle;

public class BulletBill extends Enemy {

    public BulletBill(LevelScene world, int x, int y, int dir){
    	super(world, x, y, dir, false);

    	setXPicO(8);
        setYPicO(31);

        setHeight(12);
        setWPic(16);
        setYPic(5);

        setXPic(0);
        setYa(-5);

        facing = dir;
        deadTime = 0;
        immuneToFireballs = true;
        canBlock = false;
    }

    public void move(){
        if (deadTime > 0){
            deadTime--;

            if (deadTime == 0){
                deadTime = 1;
                for (int i = 0; i < 8; i++)
                    getWorld().addSprite(new Sparkle((int) (getX()+Math.random()*16-8)+4, (int)(getY()-Math.random()*8)+4, (float)(Math.random()*2-1), (float)Math.random()*-1, 0, 1, 5));
                spriteContext.removeSprite(this);
            }

            setX(getX()+getXa());
            setY(getY()+getYa());
            setYa((getYa()*0.95f)+1);
            return;
        }

        float sideWaysSpeed = 4f;
        setXa(facing * sideWaysSpeed);
        setXFlipPic(facing == -1);
        move(getXa(), 0);
    }

    protected boolean move(float xa, float ya){
    	setX(getX()+getXa());
        return true;
    }
    
}