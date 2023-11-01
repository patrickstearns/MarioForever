package com.mojang.mario.scene;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;

import com.mojang.mario.Art;
import com.mojang.mario.BgRenderer;
import com.mojang.mario.MarioComponent;
import com.mojang.mario.Scene;
import com.mojang.mario.level.BgLevelGenerator;
import com.mojang.mario.level.LevelGenerator;
import com.mojang.mario.sprites.Mario;

public class TalkScene extends Scene {

	private MarioComponent component;
    private int tick;
    private BgRenderer bgLayer0;
    private BgRenderer bgLayer1;
    private int charToShow; 	//0 = toad, 1 = princess
    private char[] message;
    
    public TalkScene(MarioComponent component, GraphicsConfiguration gc, int charToShow, String message){
        this.component = component;
        bgLayer0 = new BgRenderer(BgLevelGenerator.createLevel((short)2048, (short)15, false, LevelGenerator.Background.CASTLE), gc, 320, 240, 1, LevelGenerator.Background.HILLS);        
        bgLayer1 = new BgRenderer(BgLevelGenerator.createLevel((short)2048, (short)15, true, LevelGenerator.Background.CASTLE), gc, 320, 240, 2, LevelGenerator.Background.HILLS);
        this.charToShow = charToShow;
        this.message = message.toCharArray();
    }

    public void init(){
        Art.startMusic(Art.SONG_RELAXING);
    }

    public void render(Graphics g, float alpha){
        bgLayer0.setCam(160, 0);
        bgLayer1.setCam(160, 0);
        bgLayer1.render(g, tick, alpha);
        bgLayer0.render(g, tick, alpha);

        //draw checkered floor - rows 12 and 13, cols 12 and 13
        for (int y = 208; y < 240; y += 16){
	        for (int x = 0; x < 320; x += 16){
	        	int tx = 12, ty = 13;
	        	if ((x/16)%2 == 1) tx++;
	        	if ((y/16)%2 == 0) ty++;
	        	Image tile = Art.level[tx][ty];
	        	g.drawImage(tile, x, y, null);
	        }
        }
        
        //draw toad/princess - enemy sheet at 0,8 and 1,8 respectively
        Image character = Art.enemies[0][8];				//Toad
        if (charToShow == 1) character = Art.enemies[1][8]; //Peach
        g.drawImage(character, 160, 176, null);
        
        //draw mario walking in from side of screen, then stop facing toad/peach
        int marioFrame = (tick/4)%4;
        if (marioFrame == 2) marioFrame = 0;
        if (marioFrame == 3) marioFrame = 2;
       
        int marioX = tick-16 + tick*2;
        if (marioX > 130){
        	marioX = 130;
        	marioFrame = 0;
        }
        g.drawImage(Art.mario[marioFrame][0], marioX, 176, null);
        
        if (tick > 50){
        	//draw speech balloon or text box or whatever
        	g.setColor(Color.WHITE);
        	int height = tick;
        	if (height > 22) height = 22;
        	g.drawRoundRect(40, 40, 240, height, 10, 10);
        }
        
        if (tick > 70){
        	int x = 44, y = 44;
        	for (int i = 0; i < message.length && i < tick-70; i++){
	            g.drawImage(Art.font[message[i] - 32][0], x + 1, y + 1, null);
	            g.drawImage(Art.font[message[i] - 32][7], x, y, null);
	            
	            x += 8;
	            if (x > 44+8*28){
	            	x = 44;
	            	y += 8;
	            }
        	}
        }
    }

    private boolean wasDown = true;
    public void tick(){
    	tick++;
        if (tick > 120 && !wasDown && keys[Mario.KEY_JUMP]){
        	component.levelWon();
        }
        if (keys[Mario.KEY_JUMP]){
            wasDown = false;
        }
    }

    public float getX(float alpha){ return 0; }
    public float getY(float alpha){ return 0; }

}
