package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;
import com.mojang.mario.sprites.Sparkle;

public class Pobodo extends Enemy {

	private int tick, yStart, jumpTime;
    private float yJumpSpeed;

    public Pobodo(LevelScene world, int x, int y){
        super(world, x, y, 1, false);

        setWidth(7);
        setHeight(16);
        setYPicO(31);
        setXPicBase(13);
        setXPic(13);
        setYPic(0);

        yJumpSpeed = -20f;
        hurtsWhenStomped = true;
        jumpTime = 0;
        yStart = y;
        setYa(-8);
        y -= 1;

        if (world != null && world.mario != null)
        	for (int i=0; i<4; i++) move();

        squishable = false;

        setLayer(-1);
        immuneToFireballs = true;
    }

    public void move(){
        if (deadTime > 0){
            deadTime--;
            if (deadTime == 0){
                deadTime = 1;
                for (int i = 0; i < 8; i++)
                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int) (getY() - Math.random() * 8) + 4, (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
                spriteContext.removeSprite(this);
            }

            setX(getX()+getXa());
            setY(getY()+getYa());
            setYa((getYa()*0.95f)+1);
            return;
        }

        tick++;

        if (getY() >= yStart){
            setY(yStart);

            jumpTime++;
            if (jumpTime > 40) setYa(yJumpSpeed);
            else setYa(0);
        }
        else jumpTime = 0;

        setY(getY()+getYa());
        setYa(getYa()+2);

        if (getYa() > 0){
            setYFlipPic(true);
            setYPicO(15);
        }
        else{
            setYFlipPic(false);
            setYPicO(31);
        }

        if (getYa() != 0) setLayer(0);
        else setLayer(-1);

        setXPic(getXPicBase()+(tick%3));
    }
}
