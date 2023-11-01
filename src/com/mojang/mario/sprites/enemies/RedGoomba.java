package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;

public class RedGoomba extends Enemy {

	public RedGoomba(LevelScene world, int x, int y, int dir){
		super(world, x, y, dir, true);

		setXPicBase(2);
        setXPic(getXPicBase());
		setYPic(2);
		setHeight(16);
		squishable = true;
	}

}

