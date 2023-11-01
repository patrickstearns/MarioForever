package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;

public class Buzzy extends Enemy {

	public Buzzy(LevelScene world, int x, int y, int facing){
		super(world, x, y, facing, false);

		setXPicBase(6);
        setXPic(getXPicBase());
		setYPic(3);
		setHeight(16);

        doubleWinged = false;
        hurtsWhenStomped = false;
		squishable = false;
        immuneToFireballs = true;
        shellDropType = 2;
	}

}

