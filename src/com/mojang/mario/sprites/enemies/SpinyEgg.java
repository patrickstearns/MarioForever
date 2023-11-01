package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;
import com.mojang.mario.sprites.Sparkle;

public class SpinyEgg extends Enemy {

	public SpinyEgg(LevelScene world, int x, int y, int dir){
		super(world, x, y, dir, false);

		setXPicBase(8);
        setXPic(getXPicBase());
		setYPic(4);
		setHeight(16);

		hurtsWhenStomped = true;
		squishable = false;
        immuneToFireballs = false;
	}

    public void move(){
        wingTime++;
        if (deadTime > 0){
            deadTime--;

            //if a shelled enemy, just go poof (and leave a shell) instead of showing squished
            if (!flyDeath && shellDropType > -1){
                deadTime = 0;
            }

            if (deadTime == 0){
                deadTime = 1;
                for (int i = 0; i < 8; i++)
                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int) (getY() - Math.random() * 8) + 4, (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
                spriteContext.removeSprite(this);
            }

            if (flyDeath){
                setX(getX()+getXa());
                setY(getY()+getYa());
                setYa((getYa() * 0.95f)+1);
            }

            setLayer(1);
            return;
        }


        float sideWaysSpeed = 1.2f;
        //float sideWaysSpeed = onGround ? 2.5f : 1.2f;

        if (getXa() > 2) facing = 1;
        if (getXa() < -2) facing = -1;

        setXa(facing * sideWaysSpeed);

        setXFlipPic(facing == -1);

        runTime += (Math.abs(getXa())) + 5;

        int runFrame = ((int) (runTime / 20)) % 2;

        if (!onGround) runFrame = 1;
        boolean wasOnGround = onGround;

        if (!move(getXa(), 0)) facing = -facing;
        onGround = false;
        bumpedCeiling = false;
        move(0, getYa());

        setYa(getYa() * (winged ? 0.95f : 0.85f));
        if (onGround) setXa(getXa() * GROUND_INERTIA);
        else setXa(getXa() * AIR_INERTIA);

        if (onGround && !wasOnGround){
            if (Math.abs(getYa()) < 0.5f) setYa(0);
            else setYa(-getYa()/1.5f);
        }

        if (!onGround){
            if (winged) setYa(getYa() + 0.6f);
            else setYa(getYa() + 2);
        }
        else if (winged) setYa(-10);

        if (winged) runFrame = wingTime / 4 % 2;

        setXPic(getXPicBase()+runFrame);

        if (inWater){
			getWorld().removeSprite(this);
			Enemy spiny = Enemy.create(Kind.Spiny, getWorld(), (int)getX(), (int)getY(), facing);
			getWorld().addSprite(spiny);
		}

    }
}

