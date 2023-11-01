package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;

public class Goomba extends Enemy {

	public Goomba(LevelScene world, int x, int y, int dir){
		super(world, x, y, dir, false);

		setXPicBase(0);
        setXPic(getXPicBase());
		setYPic(2);
		setHeight(16);

		squishable = true;
	}
}

