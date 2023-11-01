package com.mojang.mario.level;

import java.awt.*;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.*;

public class SpriteTemplate {

	public Enemy.Kind kind;
    public int lastVisibleTick = -1;
    public Sprite sprite;
    public boolean isDead = false;

    public SpriteTemplate(Enemy.Kind kind){
        this.kind = kind;
    }
    
    public void spawn(LevelScene world, int x, int y, int dir){
    	if (isDead) return;
        //if (kind == Enemy.Kind.JumpPlant) sprite = Enemy.create(kind, world, x*16+15, y*16+24, 0);
        //else
        sprite = Enemy.create(kind, world, x*16+8, y*16+15, dir);
        sprite.setSpriteTemplate(this);
        if (world != null) world.addSprite(sprite); //world is null if this is the editor; not if it's the game
    }
    
    public Image getSpriteIcon(Component c, int dir){
    	Sprite sprite = Enemy.create(kind, null, 16, 31, dir);
        sprite.setSpriteTemplate(this);
        Image ret = c.getGraphicsConfiguration().createCompatibleImage(32, 32);
        sprite.render(ret.getGraphics(), 1f);
        return ret;
    }
}