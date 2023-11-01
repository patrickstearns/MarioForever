package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;

public class Koopa extends Enemy {

	public Koopa(LevelScene world, int x, int y, int facing){
		super(world, x, y, facing, false);

		setXPicBase(0);
        setXPic(getXPicBase());
		setYPic(1);
		setHeight(24);

        doubleWinged = false;
		squishable = false;
        shellDropType = 1;
	}

}