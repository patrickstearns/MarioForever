package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;

public class BobOmb extends Enemy {

	public BobOmb(LevelScene world, int x, int y, int facing){
		super(world, x, y, facing, false);
		doubleWinged = false;

		setXPicBase(3);
        setXPic(getXPicBase());
		setYPic(5);
		setHeight(16);

		squishable = false;
        immuneToFireballs = true;
        shellDropType = 4;
	}

}

