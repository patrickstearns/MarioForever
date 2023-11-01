package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;
import com.mojang.mario.sprites.Sparkle;

public class JumpPlant extends Enemy {

	private int tick, yStart, jumpTime;
    private float yJumpSpeed;

    public JumpPlant(LevelScene world, int x, int y){
        super(world, x, y, 1, false);

        setWidth(7);
        setHeight(26);
//        setYPicO(28);
        setXPicBase(0);
        setXPic(0);
        setYPic(6);

        yJumpSpeed = -8f;
        hurtsWhenStomped = true;
        jumpTime = 0;
        yStart = y;
        setYa(-8);
        y -= 1;

        if (world != null && world.mario != null)
        	for (int i=0; i<4; i++) move();

        squishable = false;
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

            int xd = (int)(Math.abs(getWorld().mario.getX()-getX()));
            jumpTime++;

            if (jumpTime > 40 && xd > 24) setYa(yJumpSpeed);
            else setYa(0);
        }
        else jumpTime = 0;

        setY(getY()+getYa());
        setYa((getYa()*0.9f)+0.1f);

        if (getYa() != 0) setLayer(0);
        else setLayer(-1);

        setXPic(getXPicBase()+(((tick/2)&1)*2+((tick/6)&1)));
    }
}
