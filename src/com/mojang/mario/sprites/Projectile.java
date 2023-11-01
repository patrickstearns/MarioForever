package com.mojang.mario.sprites;

import com.mojang.mario.scene.LevelScene;

public abstract class Projectile extends Sprite {

	protected Projectile(LevelScene world) {
		super(world);
	}

	public abstract Sprite getSource();
	public abstract void die();
	public abstract boolean isDead();
}
