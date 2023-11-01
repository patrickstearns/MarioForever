package com.mojang.mario.sprites;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import com.mojang.mario.Art;
import com.mojang.mario.Block;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.level.Level;

public class Bubble extends Sprite{

	public int life, initialX;
    
    public Bubble(LevelScene world, float x, float y){ 
    	super(world);
    	setSheet(Art.particles);
        setX(x);
        setY(y);
        setXa(0);
        setYa(-1);
    }

    public void move(){
    	setXa((float)(((Math.random()*10000)%5)-2)); //-2 to 2
    	setYa(-(float)(((Math.random()*10000)%3)+1)); //-1 to -3
    	
        setX(getX()+getXa());
        setY(getY()+getYa());

		int ix = (int)(getX()/16);
		int iy = (int)(getY()/16);
		Block block = getWorld().level.getBlock(ix, iy);
		ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
		if (!traits.contains(Block.Trait.Swimmable)){
        	Sprite.spriteContext.removeSprite(this);        	
        }
    }
    
    public void render(Graphics og, float alpha){
        //image location plus sprite offset
        int xPixel = (int)(getXOld()+(getX()-getXOld())*alpha);
        int yPixel = (int)(getYOld()+(getY()-getYOld())*alpha);
        
        //draw small circle
        og.setColor(Color.WHITE);
        og.drawOval((int)(xPixel-2), (int)(yPixel-2), 4, 4);
    }
}