package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;

public class Spiny extends Enemy {

	public Spiny(LevelScene world, int x, int y, int facing){
		super(world, x, y, facing, false);
		doubleWinged = false;

		setXPicBase(0);
        setXPic(getXPicBase());
		setYPic(3);
		setHeight(12);

        hurtsWhenStomped = true;
		squishable = false;
        shellDropType = 3;
	}

}
