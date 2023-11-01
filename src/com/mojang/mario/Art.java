package com.mojang.mario;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.sound.midi.Sequence;

import com.mojang.util.MidiPlayer;
import com.mojang.sonar.SonarSoundEngine;
import com.mojang.sonar.sample.SonarSample;


public class Art {

	public static final int SAMPLE_BREAK_BLOCK = 0;
    public static final int SAMPLE_GET_COIN = 1;
    public static final int SAMPLE_MARIO_JUMP = 2;
    public static final int SAMPLE_MARIO_STOMP = 3;
    public static final int SAMPLE_MARIO_KICK = 4;
    public static final int SAMPLE_MARIO_POWER_UP = 5;
    public static final int SAMPLE_MARIO_POWER_DOWN = 6;
    public static final int SAMPLE_MARIO_DEATH = 7;
    public static final int SAMPLE_ITEM_SPROUT = 8;
    public static final int SAMPLE_CANNON_FIRE = 9;
    public static final int SAMPLE_SHELL_BUMP = 10;
    public static final int SAMPLE_LEVEL_EXIT = 11;
    public static final int SAMPLE_MARIO_1UP = 12;
    public static final int SAMPLE_MARIO_FIREBALL = 13;
    public static final int SAMPLE_MARIO_SWIM = 14;
    public static final int SAMPLE_MARIO_PIPE = 15;
    public static final int SAMPLE_THUD = 16;
    public static final int SAMPLE_BOBOMB_EXPLODE = 17;
    public static final int SAMPLE_BOO_LAUGH = 18;
    public static final int SAMPLE_PEACH_CRY = 19;
    public static final int SAMPLE_STAGE_WIN = 20;
    public static final int SAMPLE_GAME_OVER = 21;
    public static final int SAMPLE_WIN_DOOR = 22;

	public static final int SONG_AIRSHIP = 0;
	public static final int SONG_BOWSER = 1;
	public static final int SONG_FORTRESS = 2;
	public static final int SONG_MAP = 3;
	public static final int SONG_OVERWORLD_1 = 4;
	public static final int SONG_OVERWORLD_2 = 5;
	public static final int SONG_OVERWORLD_3 = 6;
	public static final int SONG_PARADE = 7;
	public static final int SONG_RELAXING = 8;
	public static final int SONG_SKY = 9;
	public static final int SONG_TITLE = 10;
	public static final int SONG_UNDERGROUND_1 = 11;
	public static final int SONG_UNDERGROUND_2 = 12;
	public static final int SONG_UNDERWATER = 13;
	public static final int SONG_INVINCIBLE = 14;
	public static final int SONG_POW_SWITCH = 15;

	public static Image[][] mario;
    public static Image[][] smallMario;
    public static Image[][] fireMario;
    public static Image[][] enemies;
    public static Image[][] bigEnemies;
    public static Image[][] items;
    public static Image[][] level;
    public static Image[][] particles;
    public static Image[][] bigParticles;
    public static Image[][] font;
    public static Image[][] bg;
    public static Image[][] bg2;
    public static Image[][] bg3;
    public static Image[][] map;
    public static Image[][] endScene;
    public static Image[][] gameOver;
    public static Image[][] editorIcons;
    public static Image[][] platforms;
    public static Image[][] console;
    public static Image[][] bowser;
    public static Image[][] boomer;
    public static Image[][] effects;
    public static Image logo;
    public static Image titleScreen;

    public static SonarSample[] samples = new SonarSample[100];

    private static Sequence[] songs = new Sequence[16];
    
    private static MidiPlayer midiPlayer = new MidiPlayer();

    public static Image intToConsoleImage(int i){
    	int x = 0, y = 0;
    	
    	if (i == 0 || i == 9) y = 1;
    	else y = 2;

    	x = i-1;
    	if (i == 0) x = 6;
    	if (i == 9) x = 7;
    	
    	return console[x][y];
    }
    
    public static void init(GraphicsConfiguration gc, SonarSoundEngine sound){
    	initGraphics(gc);
    	initSound(sound);
    }
    	
    public static void initGraphics(GraphicsConfiguration gc){
        try{
            mario = cutImage(gc, "/res/mariosheet.png", 32, 32);
            smallMario = cutImage(gc, "/res/smallmariosheet.png", 16, 16);
            fireMario = cutImage(gc, "/res/firemariosheet.png", 32, 32);
            enemies = cutImage(gc, "/res/enemysheet.png", 16, 32);
            bigEnemies = cutImage(gc, "/res/bigenemysheet.png", 32, 32);
            items = cutImage(gc, "/res/itemsheet.png", 16, 16);
            level = cutImage(gc, "/res/mapsheet.png", 16, 16);
            map = cutImage(gc, "/res/worldmap.png", 16, 16);
            platforms = cutImage(gc, "/res/platforms.png", 16, 16);
            particles = cutImage(gc, "/res/particlesheet.png", 8, 8);
            bigParticles = cutImage(gc, "/res/bigParticlesheet.png", 16, 16);
            bg = cutImage(gc, "/res/bgsheet.png", 32, 32);
            bg2 = cutImage(gc, "/res/bgsheet2.png", 32, 32);
            bg3 = cutImage(gc, "/res/bgsheet3.png", 32, 32);
            logo = getImage(gc, "/res/logo.gif");
            titleScreen = getImage(gc, "/res/title.gif");
            font = cutImage(gc, "/res/font.gif", 8, 8);
            endScene = cutImage(gc, "/res/endscene.gif", 96, 96);
            gameOver = cutImage(gc, "/res/gameovergost.gif", 96, 64);
            editorIcons = cutImage(gc, "/res/editorIconSheet.png", 16, 16);
            console = cutImage(gc, "/res/console.png", 16, 16);
            bowser = cutImage(gc, "/res/bowser.png", 32, 48);
            boomer = cutImage(gc, "/res/boomer.png", 32, 32);
            effects = cutImage(gc, "/res/effects.png", 32, 32);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }        

    public static void initSound(SonarSoundEngine sound){
        if (sound != null){
            samples[SAMPLE_BREAK_BLOCK] = sound.loadSample("/res/snd/breakblock.wav");
            samples[SAMPLE_GET_COIN] = sound.loadSample("/res/snd/coin.wav");
            samples[SAMPLE_MARIO_JUMP] = sound.loadSample("/res/snd/jump.wav");
            samples[SAMPLE_MARIO_STOMP] = sound.loadSample("/res/snd/stomp.wav");
            samples[SAMPLE_MARIO_KICK] = sound.loadSample("/res/snd/kick.wav");
            samples[SAMPLE_MARIO_POWER_UP] = sound.loadSample("/res/snd/powerup.wav");
            samples[SAMPLE_MARIO_POWER_DOWN] = sound.loadSample("/res/snd/powerdown.wav");
            samples[SAMPLE_MARIO_DEATH] = sound.loadSample("/res/snd/death.wav");
            samples[SAMPLE_ITEM_SPROUT] = sound.loadSample("/res/snd/sprout.wav");
            samples[SAMPLE_CANNON_FIRE] = sound.loadSample("/res/snd/cannon.wav");
            samples[SAMPLE_SHELL_BUMP] = sound.loadSample("/res/snd/bump.wav");
            samples[SAMPLE_LEVEL_EXIT] = sound.loadSample("/res/snd/exit.wav");
            samples[SAMPLE_MARIO_1UP] = sound.loadSample("/res/snd/1-up.wav");
            samples[SAMPLE_MARIO_FIREBALL] = sound.loadSample("/res/snd/fireball.wav");
            samples[SAMPLE_THUD] = sound.loadSample("/res/snd/thud.wav");
            samples[SAMPLE_MARIO_SWIM] = sound.loadSample("/res/snd/swim.wav");
            samples[SAMPLE_MARIO_PIPE] = sound.loadSample("/res/snd/pipe.wav");
            samples[SAMPLE_BOBOMB_EXPLODE] = sound.loadSample("/res/snd/bob-omb.wav");
            samples[SAMPLE_BOO_LAUGH] = sound.loadSample("/res/snd/boo.wav");
            samples[SAMPLE_PEACH_CRY] = sound.loadSample("/res/snd/peach-cry.wav");
            samples[SAMPLE_STAGE_WIN] = sound.loadSample("/res/snd/stagewin.wav");
            samples[SAMPLE_GAME_OVER] = sound.loadSample("/res/snd/gameover.wav");
            samples[SAMPLE_WIN_DOOR] = sound.loadSample("/res/snd/windoor.wav");
        }

        try{
            songs[0] = loadMusic("/res/mus/airship.mid");
            songs[1] = loadMusic("/res/mus/bowser.mid");
            songs[2] = loadMusic("/res/mus/fortress.mid");
            songs[3] = loadMusic("/res/mus/map.mid");
            songs[4] = loadMusic("/res/mus/overworld1.mid");
            songs[5] = loadMusic("/res/mus/overworld2.mid");
            songs[6] = loadMusic("/res/mus/overworld3.mid");
            songs[7] = loadMusic("/res/mus/parade.mid");
            songs[8] = loadMusic("/res/mus/relaxing.mid");
            songs[9] = loadMusic("/res/mus/sky.mid");
            songs[10] = loadMusic("/res/mus/title.mid");
            songs[11] = loadMusic("/res/mus/underground1.mid");
            songs[12] = loadMusic("/res/mus/underground2.mid");
            songs[13] = loadMusic("/res/mus/underwater.mid");
            songs[14] = loadMusic("/res/mus/invincible.mid");
            songs[15] = loadMusic("/res/mus/powswitch.mid");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private static Image getImage(GraphicsConfiguration gc, String imageName) throws IOException {
		BufferedImage source = ImageIO.read(Art.class.getResourceAsStream(imageName));
        Image image = gc.createCompatibleImage(source.getWidth(), source.getHeight(), Transparency.BITMASK);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return image;
    }

    private static Image[][] cutImage(GraphicsConfiguration gc, String imageName, int xSize, int ySize) throws IOException {
        Image source = getImage(gc, imageName);
        Image[][] images = new Image[source.getWidth(null) / xSize][source.getHeight(null) / ySize];
        for (int x = 0; x < source.getWidth(null) / xSize; x++){
            for (int y = 0; y < source.getHeight(null) / ySize; y++){
                Image image = gc.createCompatibleImage(xSize, ySize, Transparency.BITMASK);
                Graphics2D g = (Graphics2D) image.getGraphics();
                g.setComposite(AlphaComposite.Src);
                g.drawImage(source, -x * xSize, -y * ySize, null);
                g.dispose();
                images[x][y] = image;
            }
        }
        return images;
    }

    private static Integer currentSong;
    public static Integer getCurrentSong(){ return currentSong; }
    
    public static void startMusic(int song){
    	stopMusic();
    	midiPlayer.play(songs[song], true);
    	currentSong = song;
    }

    public static void stopMusic(){
    	midiPlayer.stop();
    	currentSong = null;
    }

    private static Sequence loadMusic(String location){
        return midiPlayer.loadSequence(Art.class.getResourceAsStream(location));
    }

    static int complete = 0;
    public static void main(String[] args){
        try{
            songs[0] = loadMusic("bin/res/mus/airship.mid");
            songs[1] = loadMusic("bin/res/mus/bowser.mid");
            songs[2] = loadMusic("bin/res/mus/fortress.mid");
            songs[3] = loadMusic("bin/res/mus/map.mid");
            songs[4] = loadMusic("bin/res/mus/overworld1.mid");
            songs[5] = loadMusic("bin/res/mus/overworld2.mid");
            songs[6] = loadMusic("bin/res/mus/overworld3.mid");
            songs[7] = loadMusic("bin/res/mus/parade.mid");
            songs[8] = loadMusic("bin/res/mus/relaxing.mid");
            songs[9] = loadMusic("bin/res/mus/sky.mid");
            songs[10] = loadMusic("bin/res/mus/title.mid");
            songs[11] = loadMusic("bin/res/mus/underground1.mid");
            songs[12] = loadMusic("bin/res/mus/underground2.mid");
            songs[13] = loadMusic("bin/res/mus/underwater.mid");
            songs[14] = loadMusic("bin/res/mus/invincible.mid");
            songs[15] = loadMusic("bin/res/mus/powswitch.mid");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    	System.out.println("sound initialized.");
    	
    	complete = 0;
    	for (int i = 0; i < 100; i++){					//100 iterations for test
    		final int song = i % 16; 					//songs 0 thru 15
    		final int wait = i * 1000 * 10; 			//ten seconds per song
    		final int iter = i;
    		
    		Runnable timer = new Runnable(){
	    		public void run(){
	    			try{ Thread.sleep(wait); }
	    			catch(InterruptedException e){}
	    			System.out.println("starting music; i = "+iter);
	    			Art.startMusic(song);
	    			System.out.println("  music started.");
	    			Art.complete++;
	    		}
	    	};
	    	new Thread(timer).start();
    	}
    	
    	while (complete < 100){
    		; //do nothing
    	}
    	
    	System.out.println("test complete.");
    	System.exit(1);
    }
}