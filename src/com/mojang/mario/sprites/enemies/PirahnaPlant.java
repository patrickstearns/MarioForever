package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;
import com.mojang.mario.sprites.Sparkle;

public class PirahnaPlant extends Enemy {

	private int tick, yStart, moveTime;
    private int color; //0=green, 1=red
	private boolean rising, falling, out;

	public PirahnaPlant(LevelScene world, int x, int y){
        super(world, x+8, y, 1, false);

		hurtsWhenStomped = true;

        color = (int)((Math.random()*10000)%2);

		int xPicBase = 0;
		if (color == 0) xPicBase = 4;
		else if (color == 1) xPicBase = 10;
		setXPicBase(xPicBase);
        setXPic(getXPicBase());

		setYPic(6);
		setHeight(26);

        yStart = y;
        setLayer(0);
        moveTime = 0;
        rising = false;
        falling = false;
        out = false;

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

            //plants always have a fly death
            setX(getX()+getXa());
            setY(getY()+getYa());
            setYa((getYa()*0.95f)+1);

            setLayer(1);
            return;
        }

        tick++;

        int xd = (int)(Math.abs(getWorld().mario.getX()-getX()));
        moveTime++;
        setYa(0);
        if (!out && moveTime > 40 && xd > 24){
        	rising = true;
        	out = true;
        	setYa(-4);
        }
        else if (rising && getY() > yStart-32) setYa(-4);
        else if (rising && getY() <= yStart-32){
        	rising = false;
        	setY(yStart-32);
        	setYa(0);
        	moveTime = 0;
        }

        if (out && !rising && !falling && moveTime > 80){
        	falling = true;
        	setYa(4);
        }
        else if (falling && getY() < yStart) setYa(4);
        else if (falling && getY() >= yStart){
        	falling = false;
        	out = false;
        	setYa(0);
        	moveTime = 0;
        }

        setY(getY()+getYa());

        facing = (int)Math.signum(getWorld().mario.getX()-getX());
        setXFlipPic(facing == -1);

        //calc picture
        int xPic = getXPicBase();
        if (tick%10 > 5) xPic++;
        setXPic(xPic);
    }
}
