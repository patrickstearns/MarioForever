package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;

public class RedKoopa extends Enemy {

	public RedKoopa(LevelScene world, int x, int y, int facing){
		super(world, x, y, facing, false);
		doubleWinged = false;

		setXPicBase(0);
        setXPic(getXPicBase());
		setYPic(0);
		setHeight(24);

		squishable = false;
        avoidsCliffs = true;
        shellDropType = 0;
	}

}
