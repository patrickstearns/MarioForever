package com.mojang.mario.sprites.enemies;

import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.Enemy;

public class Spike extends Enemy {

	public Spike(LevelScene world, int x, int y, int dir){
		super(world, x, y, dir, false);

		setXPicBase(12);
        setXPic(getXPicBase());
		setYPic(3);
		setHeight(12);

		hurtsWhenStomped = true;
		squishable = false;
        immuneToFireballs = true;
	}

}

