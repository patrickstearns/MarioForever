package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;

public class WingedKoopa extends Enemy {

	public WingedKoopa(LevelScene world, int x, int y, int facing){
		super(world, x, y, facing, true);
		doubleWinged = false;

		setXPicBase(0);
        setXPic(getXPicBase());
		setYPic(1);
		setHeight(24);

        winged = true;
		squishable = false;
        shellDropType = 1;
	}

}
