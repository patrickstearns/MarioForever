package com.mojang.mario.scene;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;

import com.mojang.mario.Art;
import com.mojang.mario.BgRenderer;
import com.mojang.mario.MarioComponent;
import com.mojang.mario.Scene;
import com.mojang.mario.level.BgLevelGenerator;
import com.mojang.mario.level.LevelGenerator;
import com.mojang.mario.sprites.Mario;

public class TitleScene extends Scene {
	
    private MarioComponent component;
    private int tick;
    private BgRenderer bgLayer0;
    private BgRenderer bgLayer1;
    
    public TitleScene(MarioComponent component, GraphicsConfiguration gc){
        this.component = component;
        bgLayer0 = new BgRenderer(BgLevelGenerator.createLevel((short)2048, (short)15, false, LevelGenerator.Background.HILLS), gc, 320, 240, 1, LevelGenerator.Background.HILLS);        
        bgLayer1 = new BgRenderer(BgLevelGenerator.createLevel((short)2048, (short)15, true, LevelGenerator.Background.HILLS), gc, 320, 240, 2, LevelGenerator.Background.HILLS);
    }

    public void init(){
        Art.startMusic(Art.SONG_TITLE);
    }

    public void render(Graphics g, float alpha){
        bgLayer0.setCam(tick+160, 0);
        bgLayer1.setCam(tick+160, 0);
        bgLayer1.render(g, tick, alpha);
        bgLayer0.render(g, tick, alpha);
        int yo = 16-Math.abs((int)(Math.sin((tick+alpha)/6.0)*8));
        g.drawImage(Art.logo, 0, yo, null);
        g.drawImage(Art.titleScreen, 0, 120, null);
        
        int y = 300-tick;
        if (y < 180) y = 180;
        drawString(g, "Hold [A] to run/carry", y);
        drawString(g, "Press [S] to jump", y+8);
        drawString(g, "Press [Q] to quit", y+16);

        if ((tick/10)%2 == 0)
        	drawString(g, "Press Jump to start!", y+32);
    }

    private void drawString(Graphics g, String text, int y){
        drawStringInternal(g, text, 160 - text.length() * 4 + 1, y + 1, 0);
        drawStringInternal(g, text, 160 - text.length() * 4, y, 7);
    }
    
    private void drawStringInternal(Graphics g, String text, int x, int y, int c){
        char[] ch = text.toCharArray();
        for (int i = 0; i < ch.length; i++){
            g.drawImage(Art.font[ch[i] - 32][c], x + i * 8, y, null);
        }
    }

    private boolean wasDown = true;
    public void tick(){
    	tick++;
        if (!wasDown && keys[Mario.KEY_JUMP]){
            component.startGame();
        }
        if (keys[Mario.KEY_JUMP]){
            wasDown = false;
        }
        
        if (tick > 1000){
        	component.toCredits();
        }
    }

    public float getX(float alpha){ return 0; }
    public float getY(float alpha){ return 0; }

}
