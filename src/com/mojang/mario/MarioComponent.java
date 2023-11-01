package com.mojang.mario;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.*;
import java.io.*;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;

import com.mojang.mario.level.*;
import com.mojang.mario.scene.*;
import com.mojang.mario.sprites.*;
import com.mojang.sonar.FakeSoundEngine;
import com.mojang.sonar.SonarSoundEngine;

public class MarioComponent extends JComponent implements Runnable, KeyListener {

    public static String BASE_DIR = "";
    public static String GAME_DATA_DIR = BASE_DIR+"gamedata/";
    public static String MAIN_GAME_DIR = GAME_DATA_DIR+"main/";
    public static String TILE_BEHAVIOR_FILENAME = GAME_DATA_DIR+"tiles.dat";
    public static String DEFAULT_LEVEL_ORDER_FILENAME = MAIN_GAME_DIR+"levels.txt";
    
    public static boolean FULLSCREEN = false;
    
	private static final long serialVersionUID = 739318775993206607L;
    public static final int TICKS_PER_SECOND = 24;

    private boolean running = false, useScale2x = false;
    private int width, height;
    private GraphicsConfiguration graphicsConfiguration;
    private Scene scene;
    private SonarSoundEngine sound;
    private Scale2x scale2x = new Scale2x(320, 240);
    private String loadLevelFilename;
    private LevelOrder levelOrder;
    private int currentLevel = 0;
//    private MapScene mapScene;
    
    public MarioComponent(int width, int height){
        try { Level.loadBehaviors(new DataInputStream(MarioComponent.class.getResourceAsStream("/"+TILE_BEHAVIOR_FILENAME))); }
        catch(IOException e){
        	e.printStackTrace();
        }
        
        try{ setLevelOrder(LevelOrder.loadLevelOrder("/"+DEFAULT_LEVEL_ORDER_FILENAME)); }
        catch(IOException e){
        	e.printStackTrace();
        }
        
        this.setFocusable(true);
        this.setEnabled(true);
        this.width = width;
        this.height = height;

        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        try{ sound = new SonarSoundEngine(64); }
        catch (LineUnavailableException e){
            e.printStackTrace();
            sound = new FakeSoundEngine();
        }

        setFocusable(true);
    }

    public void setLoadLevelFilename(String filename){ this.loadLevelFilename = filename; }

    public void setLevelOrder(LevelOrder order){ this.levelOrder = order; }
    public LevelOrder getLevelOrder(){ return levelOrder; }
    
    public int getCurrentWorld(){ return levelOrder.getWorldIndex(currentLevel); }
    public int getCurrentLevel(){ return levelOrder.getLevelIndex(currentLevel); }
    
    private void toggleKey(int keyCode, boolean isPressed){
        if (keyCode == KeyEvent.VK_LEFT) scene.toggleKey(Mario.KEY_LEFT, isPressed);
        if (keyCode == KeyEvent.VK_RIGHT) scene.toggleKey(Mario.KEY_RIGHT, isPressed);
        if (keyCode == KeyEvent.VK_DOWN) scene.toggleKey(Mario.KEY_DOWN, isPressed);
        if (keyCode == KeyEvent.VK_UP) scene.toggleKey(Mario.KEY_UP, isPressed);
        if (keyCode == KeyEvent.VK_A) scene.toggleKey(Mario.KEY_SPEED, isPressed);
        if (keyCode == KeyEvent.VK_S) scene.toggleKey(Mario.KEY_JUMP, isPressed);
        if (isPressed && keyCode == KeyEvent.VK_1) useScale2x = !useScale2x;
        if (isPressed && keyCode == KeyEvent.VK_2) LevelRenderer.renderBehaviors = !LevelRenderer.renderBehaviors;
        if (isPressed && keyCode == KeyEvent.VK_Q) System.exit(1);
    }

    public void paint(Graphics g){}
    public void update(Graphics g){}

    public void start(){
        if (!running){
            running = true;
            new Thread(this, "Game Thread").start();
        }
    }

    public void stop(){
        Art.stopMusic();
        running = false;
    }

    boolean checkForPause = false;
    public void run(){
        graphicsConfiguration = getGraphicsConfiguration();

        //scene = new LevelScene(graphicsConfiguration);
//        mapScene = new MapScene(graphicsConfiguration, this, new Random().nextLong());
//        scene = mapScene;

        Art.init(graphicsConfiguration, sound);

        VolatileImage image = createVolatileImage(320, 240);
        Graphics g = getGraphics();
        Graphics og = image.getGraphics();

        long lastTick = -1;
        //        double lastNow = 0;
        int renderedFrames = 0;
        //int fps = 0;

        //double now = 0;
        //double startTime = System.nanoTime() / 1000000000.0; 
        //double timePerFrame = 0; 
        double time = System.nanoTime() / 1000000000.0;
        double now = time;
        double averagePassedTime = 0;

        addKeyListener(this);

        boolean naiveTiming = true;

        if (loadLevelFilename != null) startLoadLevel(loadLevelFilename);
        else toTitle();

        while (running){
            double lastTime = time;
            time = System.nanoTime() / 1000000000.0;
            double passedTime = time - lastTime;

            if (passedTime < 0) naiveTiming = false; // Stop relying on nanotime if it starts skipping around in time (ie running backwards at least once). This sometimes happens on dual core amds.
            averagePassedTime = averagePassedTime * 0.9 + passedTime * 0.1;

            if (naiveTiming)
            {
                now = time;
            }
            else
            {
                now += averagePassedTime;
            }

            long tick = (long) (now * TICKS_PER_SECOND);
            if (lastTick == -1) lastTick = tick;
            while (lastTick < tick)
            {
            	scene.tick();
                lastTick++;

                if (lastTick % TICKS_PER_SECOND == 0)
                {
                    //fps = renderedFrames;
                    renderedFrames = 0;
                }
            }

            float alpha = (float) (now * TICKS_PER_SECOND - tick);
            sound.clientTick(alpha);

            og.setColor(Color.WHITE);
            og.fillRect(0, 0, 320, 240);

            scene.render(og, alpha);

            if (!this.hasFocus()){
            	if (scene instanceof LevelScene){
            		((LevelScene)scene).paused = true;
            	}

            	og.setColor(new Color(0f, 0f, 0f, 0.75f));
            	og.fillRect(0, 0, 320, 240);
            	
            	for (int x = 0; x < 4; x++){
            		og.drawImage(Art.console[x+4][0], 128+16*x, 104, null);
            	}
            	
            	checkForPause = true;
            	
//                String msg = "CLICK TO PLAY";
//                drawString(og, msg, 160 - msg.length() * 4 + 1, 110 + 1, 0);
//                drawString(og, msg, 160 - msg.length() * 4, 110, 7);
            }
            else if (checkForPause){
            	if (scene instanceof LevelScene){
            		((LevelScene)scene).paused = false;
            	}
            	checkForPause = false;
            }
            og.setColor(Color.BLACK);
            /*          drawString(og, "FPS: " + fps, 5, 5, 0);
             drawString(og, "FPS: " + fps, 4, 4, 7);*/

            if (width != 320 || height != 240){
                if (useScale2x) g.drawImage(scale2x.scale(image), 0, 0, null);
                else g.drawImage(image, 0, 0, 640, 480, null);
            }
            else g.drawImage(image, 0, 0, null);

            renderedFrames++;

            try{ Thread.sleep(5); }
            catch (InterruptedException e){}
        }

        Art.stopMusic();
    }

    public void keyPressed(KeyEvent arg0){
    	toggleKey(arg0.getKeyCode(), true);
    }

    public void keyReleased(KeyEvent arg0){
        toggleKey(arg0.getKeyCode(), false);
    }

    public void startRandomLevel(long seed, int difficulty, LevelGenerator.Background type){
        scene = new LevelScene(graphicsConfiguration, this, seed, difficulty, type);
        scene.setSound(sound);
        scene.init();
    }

    public void startLoadLevel(String loadedFilename){
        Mario.resetStatic();
        scene = new LevelScene(graphicsConfiguration, this, loadedFilename);
        scene.setSound(sound);
        scene.init();
    }
    
    public void startNextLevel(){
    	Mario.lastMarker = null;
    	currentLevel++;
    	if (currentLevel < levelOrder.getLevelCount()){
    		if (levelOrder.isLevelTalk(currentLevel)){
    			int charToShow = 0; //toad
    			if (levelOrder.getLevelMessage(currentLevel).startsWith("H")) charToShow = 1; //TODO: a dirty, dirty hack
    			//TODO: if on last level, switch that to peach (1)
    			scene = new TalkScene(this, graphicsConfiguration, charToShow, levelOrder.getLevelMessage(currentLevel));
    			scene.setSound(sound);
    			scene.init();
    		}
    		else{ //is game level
    			//running via editor
//		        String filename = loadLevelFilename;
//		        if (filename == null) filename = levelOrder.getLevelFilename(currentLevel);
//		        else scene = new LevelScene(graphicsConfiguration, this, filename);

    			//running game
    			scene = new LevelScene(graphicsConfiguration, this, levelOrder.getLevelFilename(currentLevel));
		    	scene.setSound(sound);
		    	scene.init();
    		}
    	}
    	else win();
    }

    public void levelFailed(){
        Mario.lives--;
        if (Mario.lives < 0) lose();

        String filename = loadLevelFilename;
        if (filename == null) filename = levelOrder.getLevelFilename(currentLevel);
        else scene = new LevelScene(graphicsConfiguration, this, filename);
    	scene.setSound(sound);
    	scene.init();
    }

    public void keyTyped(KeyEvent arg0){}

    public void levelWon(){
        String filename = loadLevelFilename;
        if (filename == null) startNextLevel();
        else scene = new LevelScene(graphicsConfiguration, this, filename);
    }
    
    public void win(){
        scene = new WinScene(this);
        scene.setSound(sound);
        scene.init();
    }
    
    public void toTitle(){
        Mario.resetStatic();
        scene = new TitleScene(this, graphicsConfiguration);
        scene.setSound(sound);
        scene.init();
    }
    
    public void toCredits(){
        scene = new CreditsScene(this, graphicsConfiguration);
        scene.setSound(sound);
        scene.init();
    }
    
    public void lose(){
        scene = new LoseScene(this);
        scene.setSound(sound);
        scene.init();
    }

    public void startGame(){
    	Mario.resetStatic();
    	currentLevel = -1; //so it starts at zero
    	startNextLevel();
   }
}