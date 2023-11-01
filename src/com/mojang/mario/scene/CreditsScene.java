package com.mojang.mario.scene;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.mojang.mario.Art;
import com.mojang.mario.BgRenderer;
import com.mojang.mario.MarioComponent;
import com.mojang.mario.Scene;
import com.mojang.mario.level.BgLevelGenerator;
import com.mojang.mario.level.LevelGenerator;
import com.mojang.mario.sprites.Mario;

public class CreditsScene extends Scene {

	private MarioComponent component;
    private int tick;
    private BgRenderer bgLayer0;
    private BgRenderer bgLayer1;
    private Image textImage = null;
    private String[] credits = new String[]{
    		"Almost Everything by Patrick Stearns",
    		"",
    		"Code based on \"Infinite Mario\"",
    		"by Markus Persson",
    		"http://www.mojang.com/notch/mario/",
    		"",
    		"Terrain and Objects Ripped by Jouw",
    		"found at www.spriters-resource.com",
    		"",
    		"Some Music and SFX taken from",
    		"www.themushroomkingdom.net",
    };
    
    public CreditsScene(MarioComponent component, GraphicsConfiguration gc){
        this.component = component;
        bgLayer0 = new BgRenderer(BgLevelGenerator.createLevel((short)20, (short)15, false, LevelGenerator.Background.HILLS), gc, 320, 240, 1, LevelGenerator.Background.HILLS);        
        bgLayer1 = new BgRenderer(BgLevelGenerator.createLevel((short)20, (short)15, true, LevelGenerator.Background.HILLS), gc, 320, 240, 2, LevelGenerator.Background.HILLS);
    }

    public void init(){
        Art.startMusic(Art.SONG_RELAXING);
    }

    public void render(Graphics g, float alpha){
    	//set camera, draw background layers
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
        
        //if this if the first time we're drawing...
        if (textImage == null){
        	//create text image
        	int width =320;
        	int height = credits.length*8+4;
        	textImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        	
        	//draw on credits
        	for (int i = 0; i < credits.length; i++){
        		char[] credit = credits[i].toCharArray();
        		int x = 160-credit.length*4;
        		int y = 2 + i * 8;
        		for (int j = 0; j < credit.length; j++){
		            textImage.getGraphics().drawImage(Art.font[credit[j] - 32][0], x + 1, y + 1, null);
		            textImage.getGraphics().drawImage(Art.font[credit[j] - 32][7], x, y, null);
		            x += 8;
        		}
        	}
        }
        
        //draw the credits image
        int textX = 0;
        int textY = 240-tick;
        g.drawImage(textImage, textX, textY, null);
    }

    private boolean wasDown = true;
    public void tick(){
    	tick++;
        if (tick > 120 && !wasDown && keys[Mario.KEY_JUMP]){
        	component.toTitle();
        }
        if (keys[Mario.KEY_JUMP]){
            wasDown = false;
        }
        
        if (textImage != null && tick > textImage.getHeight(null)+240+20){
        	component.toTitle();
        }
    }

    public float getX(float alpha){ return 0; }
    public float getY(float alpha){ return 0; }

}
