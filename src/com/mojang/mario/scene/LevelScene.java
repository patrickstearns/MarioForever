package com.mojang.mario.scene;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.io.*;
import java.util.*;

import com.mojang.mario.*;
import com.mojang.mario.sprites.*;
import com.mojang.mario.sprites.enemies.BulletBill;
import com.mojang.mario.sprites.powerups.FireFlower;
import com.mojang.mario.sprites.powerups.LifeMushroom;
import com.mojang.mario.sprites.powerups.Mushroom;
import com.mojang.mario.sprites.powerups.Star;
import com.mojang.mario.sprites.powerups.Switch;
import com.mojang.mario.sprites.projectiles.CircularFireball;
import com.mojang.mario.sprites.projectiles.Fireball;
import com.mojang.sonar.FixedSoundSource;
import com.mojang.mario.level.*;

public class LevelScene extends Scene implements SpriteContext {

	private List<Sprite> sprites = new ArrayList<Sprite>();
    private List<Sprite> spritesToAdd = new ArrayList<Sprite>();
    private List<Sprite> spritesToRemove = new ArrayList<Sprite>();

    public Level level;
    public Mario mario;
    public float xCam, yCam, xCamO, yCamO;
    public static Image tmpImage;
    private int tick;

    private LevelRenderer layer;
    private BgRenderer[] bgLayer = new BgRenderer[2];

    private GraphicsConfiguration graphicsConfiguration;

    public boolean paused = false, actionPaused = false;
    public int startTime = 0, introTime = 0;

    private long levelSeed;
    private MarioComponent renderer;
    private LevelGenerator.Background levelType;
    private int levelDifficulty;
    private String loadedFilename;
    
    public LevelScene(GraphicsConfiguration graphicsConfiguration, MarioComponent renderer, long seed, int levelDifficulty, 
    		LevelGenerator.Background type){
        this.graphicsConfiguration = graphicsConfiguration;
        this.levelSeed = seed;
        this.renderer = renderer;
        this.levelDifficulty = levelDifficulty;
        this.levelType = type;
    }

    public LevelScene(GraphicsConfiguration graphicsConfiguration, MarioComponent renderer, String loadedFilename){
        this.graphicsConfiguration = graphicsConfiguration;
        this.renderer = renderer;
        this.loadedFilename = loadedFilename;
    }

    public void init(){
        if (loadedFilename == null) level = LevelGenerator.createLevel((short)320, (short)15, levelSeed, levelDifficulty, levelType);
        else {
			try{ level = Level.load(new DataInputStream(LevelScene.class.getClassLoader().getResourceAsStream(loadedFilename))); }
			catch(Exception e){ e.printStackTrace(); System.exit(1); }
			levelType = level.getBackgroundType();
        }
        mario = new Mario(this);
 
        Marker lastMarker = Mario.lastMarker;
        if (lastMarker != null){
        	level.setCurrentArea(level.findAreaForPortal(Mario.lastMarker.id, false));

    		mario.setLayer(-1); //if coming in via a marker, needs to be on back layer to prevent collisions
    		mario.exitingMarker = lastMarker;
	        mario.exitingPipeLeft = (lastMarker.type == Marker.Type.ENTRANCE_BIGPIPE_LEFT 
        		|| lastMarker.type == Marker.Type.ENTRANCE_SMALLPIPE_LEFT);
	        mario.exitingPipeRight = (lastMarker.type == Marker.Type.ENTRANCE_BIGPIPE_RIGHT
        		|| lastMarker.type == Marker.Type.ENTRANCE_SMALLPIPE_RIGHT);
	        mario.exitingPipeUp = (lastMarker.type == Marker.Type.ENTRANCE_BIGPIPE_UP ||
        		lastMarker.type == Marker.Type.ENTRANCE_SMALLPIPE_UP);
	        mario.exitingPipeDown = (lastMarker.type == Marker.Type.ENTRANCE_BIGPIPE_DOWN ||
        		lastMarker.type == Marker.Type.ENTRANCE_SMALLPIPE_DOWN);
	        mario.exitingDoor = (lastMarker.type == Marker.Type.ENTER_DOOR);
	        mario.exitingBeanstalkDown = (lastMarker.type == Marker.Type.ENTER_BEANSTALK_DOWNWARD);
	        mario.exitingBeanstalkUp = (lastMarker.type == Marker.Type.ENTER_BEANSTALK_UPWARD);
	        
	        if (lastMarker.type == Marker.Type.ENTRANCE_BIGPIPE_LEFT 
        		|| lastMarker.type == Marker.Type.ENTRANCE_SMALLPIPE_LEFT) mario.facing = -1;
	        if (lastMarker.type == Marker.Type.ENTRANCE_BIGPIPE_RIGHT
        		|| lastMarker.type == Marker.Type.ENTRANCE_SMALLPIPE_RIGHT) mario.facing = 1;
	        mario.exitingPipeTime = 1;

			mario.enteringPipeLeft = false;
			mario.enteringPipeRight = false;
    		mario.enteringPipeUp = false;
    		mario.enteringPipeDown = false;
    		mario.enteringDoor = false;
    		mario.enteringPipeTime = 0;
    		mario.enteringMarker = null;

    		//if on a beanstalk, set climbing, else will die when restarting on an up beanstalk
    		if (mario.exitingBeanstalkDown || mario.exitingBeanstalkUp)
    			mario.climbing = true;

        
        }
        else{
        	level.setCurrentArea(level.getAreas().get(level.getStartAreaIndex()));
        }

        paused = true;
        actionPaused = true;
        Sprite.spriteContext = this;
        introTime = 1;
        startTime = 0;
        tick = 0;
        areaChanged();

        if (lastMarker == null){
	        mario.setX(level.getCurrentArea().findMarker(Marker.Type.START_POS).x*16+8);
	        mario.setY(level.getCurrentArea().findMarker(Marker.Type.START_POS).y*16+8);
        }
        else{
	        if (mario.exitingPipeLeft){
	        	mario.facing = -1;
	        	mario.setX(lastMarker.x*16+2);
	        	mario.setY(lastMarker.y*16+lastMarker.height-2);
	        }
	        else if (mario.exitingPipeRight){
	        	mario.facing = 1;
	        	mario.setX((lastMarker.x)*16+lastMarker.width-mario.getWidth()+1);
	        	mario.setY(lastMarker.y*16+lastMarker.height-2);
	        }
	        else if (mario.exitingPipeUp || mario.exitingBeanstalkUp){
	        	mario.setX(lastMarker.x*16+lastMarker.width/2-mario.getWidth()/2);
	        	mario.setY(lastMarker.y*16+mario.getHeight());
	        }
	        else if (mario.exitingPipeDown || mario.exitingBeanstalkDown){
	        	mario.setX(lastMarker.x*16+lastMarker.width/2-mario.getWidth()/2);
	        	mario.setY((lastMarker.y+1)*16+lastMarker.height-mario.getHeight());
	        }
	        else if (mario.exitingDoor){
	        	mario.setX(1+lastMarker.x*16+lastMarker.width/2-mario.getWidth()/2);
	        	mario.setY(lastMarker.y*16+lastMarker.height-1);
	        	mario.alphaTransparency = 0;
	        }
        }
        
    }
    
    public void areaChanged(){
    	if (mario.exitingMarker != null)
    		Mario.lastMarker = mario.exitingMarker;

    	this.levelType = level.getBackgroundType();

    	if (!mario.isStarInvulnerable()){ //if invulnerable, don't change the music
	    	int newSong = Level.songForLevelType(levelType);
	    	if (startTime == 0 && introTime == 0){ //don't start right at first when coming into level so display has time to show
	    		if (Art.getCurrentSong() != newSong)
	    			Art.startMusic(newSong);
	    	}
    	}
    	
        sprites.clear();
        layer = new LevelRenderer(level, graphicsConfiguration, 320, 240);
        for (int i = 0; i < 2; i++){
            int scrollSpeed = 4 >> i;
            int w = ((level.getWidth() * 16) - 320) / scrollSpeed + 320;
            int h = ((level.getHeight() * 16) - 240) / scrollSpeed + 240;
            Level bgLevel = BgLevelGenerator.createLevel((short)(w / 32 + 1), (short)(h / 32 + 1), i == 0, levelType);
            bgLayer[i] = new BgRenderer(bgLevel, graphicsConfiguration, 320, 240, scrollSpeed, levelType);
        }
        sprites.add(mario);
        
        for (int i = 0; i < level.getWidth(); i++){
        	for (int j = 0; j < level.getHeight(); j++){
        		if (Level.FIRE_BAR_BLOCK == level.getBlock(i, j).blockId){
        			sprites.add(new CircularFireball(this, i*16, j*16, null, 40));
        			sprites.add(new CircularFireball(this, i*16, j*16, null, 32));
        			sprites.add(new CircularFireball(this, i*16, j*16, null, 24));
        			sprites.add(new CircularFireball(this, i*16, j*16, null, 16));
        			sprites.add(new CircularFireball(this, i*16, j*16, null, 8));
        		}
        	}
        }
        
    }

    public LevelRenderer getLayer(){ return layer; }
    
    public int fireballsOnScreen = 0;
    List<Shell> shellsToCheck = new ArrayList<Shell>();
    public void checkShellCollide(Shell shell){ shellsToCheck.add(shell); }

    List<Fireball> fireballsToCheck = new ArrayList<Fireball>();
    public void checkFireballCollide(Fireball fireball){ fireballsToCheck.add(fireball); }

    List<Projectile> projectilesToCheck = new ArrayList<Projectile>();
    public void checkProjectileCollide(Projectile projectile){ projectilesToCheck.add(projectile); }

    public int countProjectilesFrom(Sprite source){
    	int count = 0;
    	for (Sprite sprite: getSprites()){
    		if (sprite instanceof Projectile){
    			Projectile p = (Projectile)sprite;
    			if (p.getSource() == source) count++;
    		}
    	}
    	return count;
    }

    public void tick(){
        xCamO = xCam;
        yCamO = yCam;

        if (introTime > 0) introTime++;
        if (introTime > 60){ 
        	startTime = 1; 
        	introTime = 0; 
        	Art.startMusic(Level.songForLevelType(levelType));
        	paused = false;
        }
        if (startTime > 0) startTime++;
        if (startTime > 15){
        	actionPaused = false;
        	startTime = 0;
        }

        float targetXCam = mario.getX() - 160;
        float targetYCam = mario.getY()-120;
        xCam = targetXCam;
        yCam = targetYCam;
        if (xCam < 0) xCam = 0;
        if (xCam > level.getWidth() * 16 - 320) xCam = level.getWidth() * 16 - 320;
		if (yCam < 0) yCam = 0;
		if (yCam > level.getHeight() * 16 - 240) yCam = level.getHeight() * 16 - 240;

        //if (recorder != null) recorder.addTick(mario.getKeyMask());
        //if (replayer!=null) mario.setKeys(replayer.nextTick());
        
        fireballsOnScreen = 0;

        for (Sprite sprite : sprites){
            if (sprite != mario){
                float xd = sprite.getX() - xCam;
                float yd = sprite.getY() - yCam;
                if ((xd < -64 || xd > 320 + 64 || yd < -64 || yd > 240 + 64) &&
                		!sprite.isPersistent()) removeSprite(sprite);
                else if (sprite instanceof Fireball && !(sprite instanceof CircularFireball) && ((Fireball)sprite).getSource() == mario) 
                	fireballsOnScreen++;
            }
        }

        if (paused) {}
        else if (actionPaused) {
            for (Sprite sprite : new ArrayList<Sprite>(sprites)) {
                if (sprite == mario) 
                	sprite.tick();
                else sprite.tickNoMove();
            }
        }
        else {
            tick++;
            level.tick();

            boolean hasShotCannon = false;
            int xCannon = 0;
            
			int sx = (int) xCam / 16 - 1, ex = (int) (xCam + layer.getWidth()) / 16 + 1;
			int sy = (int) yCam / 16 - 1, ey = (int) (yCam + layer.getHeight()) / 16 + 1;
            for (int x = sx; x <= ex; x++)
                for (int y = sy; y <= ey; y++){
                    int dir = 0;

                    if (x * 16 + 8 > mario.getX() + 16) dir = -1;
                    if (x * 16 + 8 < mario.getX() - 16) dir = 1;

                    SpriteTemplate st = level.getSpriteTemplate(x, y);
                    if (st != null){
                        if (st.lastVisibleTick != tick - 1)
                            if (st.sprite == null || !sprites.contains(st.sprite))
                                st.spawn(this, x, y, dir);

                        st.lastVisibleTick = tick;
                    }

                    if (dir != 0){
                        Block b = level.getBlock(x, y);
                    	if (b.blockId == Level.BULLET_BILL_CANNON){
                            if ((tick - x * 2) % 100 == 0){
                                xCannon = x;
                                for (int i = 0; i < 8; i++)
                                    addSprite(new Sparkle(x * 16 + 8, y * 16 + (int) (Math.random() * 16), (float) Math.random() * dir, 0, 0, 1, 5));

                                addSprite(new BulletBill(this, x * 16 + 8 + dir * 8, y * 16 + 15, dir));
                                hasShotCannon = true;
                            }
                        }
                    }
                }

            if (hasShotCannon) sound.play(Art.samples[Art.SAMPLE_CANNON_FIRE], new FixedSoundSource(xCannon * 16, yCam + 120), 1, 1, 1);

            for (Sprite sprite : sprites) sprite.tick();

            for (Sprite sprite : sprites) sprite.collideCheck();

            for (Shell shell : shellsToCheck)
                for (Sprite sprite : sprites)
                    if (sprite != shell && !shell.dead)
                        if (sprite.shellCollideCheck(shell))
                            if (mario.carried == shell && !shell.dead){
                                mario.carried = null;
                                shell.die();
                            }
            shellsToCheck.clear();

            for (Fireball fireball : fireballsToCheck)
                for (Sprite sprite : sprites)
                    if (sprite != fireball && sprite != fireball.getSource() && !fireball.dead)
                        if (sprite.fireballCollideCheck(fireball))
                            fireball.die();
            fireballsToCheck.clear();

            for (Projectile check: projectilesToCheck)
                for (Sprite sprite : sprites)
                    if (sprite != check && sprite != check.getSource() && !check.isDead())
                        if (sprite.spriteCollideCheck(check))
                            check.die();
            projectilesToCheck.clear();
        }

        sprites.addAll(0, spritesToAdd);
        sprites.removeAll(spritesToRemove);
        spritesToAdd.clear();
        spritesToRemove.clear();
    }
    
    public void render(Graphics g, float alpha){
    	//if doing intro, just do that - no need for the rest
    	if (introTime > 0 && startTime == 0){
    		//black background
    		g.setColor(new Color(0, 0, 100));
    		g.fillRect(0, 0, 320, 240);
    		
    		//"World X-Y" is 128x16 pixels, so located at 109, 104
    		g.drawImage(Art.console[0][1], 96, 100, null);
    		g.drawImage(Art.console[1][1], 112, 100, null);
    		g.drawImage(Art.console[2][1], 128, 100, null);
    		g.drawImage(Art.console[3][1], 144, 100, null);
    		//space
    		g.drawImage(Art.intToConsoleImage(renderer.getCurrentWorld()), 176, 100, null);
    		g.drawImage(Art.console[4][1], 192, 100, null);
    		if (renderer.getCurrentLevel() != 4)
    			g.drawImage(Art.intToConsoleImage(renderer.getCurrentLevel()), 208, 100, null);
    		else //draw castle if this is X-4
    			g.drawImage(Art.console[3][0], 208, 100, null);
    			
    		//"[mario head] x ZZ" is 84x16 pixels, so at 120, 122 
   			g.drawImage(Art.console[0][0], 120, 122, null);
   			//space
   			g.drawImage(Art.console[5][1], 152, 122, null);
   			if (Mario.lives > 9){
   				int tensDigit = Mario.lives/10;
    			g.drawImage(Art.intToConsoleImage(tensDigit), 168, 122, null);
   			}
   			g.drawImage(Art.intToConsoleImage(Mario.lives%10), 184, 122, null);
   			
   			return;
    	}
    	
    	//figure camera position
    	int xCam = (int) (mario.getXOld() + (mario.getX() - mario.getXOld()) * alpha) - 160;
        int yCam = (int) (mario.getYOld() + (mario.getY() - mario.getYOld()) * alpha) - 120;

        //if dead, lock camera on death position instead
        if (mario.deathTime > 0){
    		xCam = (int) mario.xDeathPos - 160;
        	yCam = (int) mario.yDeathPos - 120;
        }
        
        if (xCam < 0) xCam = 0;
        if (yCam < 0) yCam = 0;
        if (xCam > level.getWidth() * 16 - 320) xCam = level.getWidth() * 16 - 320;
        if (yCam > level.getHeight() * 16 - 240) yCam = level.getHeight() * 16 - 240;

        //paint background
        for (int i = 0; i < 2; i++){
            bgLayer[i].setCam(xCam, yCam);
            bgLayer[i].render(g, tick, alpha);
        }

        //paint tiles with 'behind' attributes
        layer.render(g, tick, paused?0:alpha, true, false);

        //paint any sprites on level -1 (moving thru pipes, etc.)
        g.translate(-xCam, -yCam);
        for (Sprite sprite : sprites)
            if (sprite.getLayer() == -1) sprite.render(g, alpha);
        g.translate(xCam, yCam);
        
        //paint layer 0
        g.translate(-xCam, -yCam);
        for (Sprite sprite : sprites)
            if (sprite.getLayer() == 0) sprite.render(g, alpha);
        g.translate(xCam, yCam);

        //paint tiles
        layer.setCam(xCam, yCam);
        layer.render(g, tick, paused?0:alpha, false, false);

        //paint back part of exit
        layer.renderExit0(g, tick, paused?0:alpha, mario.winningTime==0 && mario.winTime == 0);

        //paint layer 1
        g.translate(-xCam, -yCam);
        for (Sprite sprite : sprites)
            if (sprite.getLayer() == 1) sprite.render(g, alpha);
        g.translate(xCam, yCam);

        //paint sprites on layer 2
        g.translate(-xCam, -yCam);
        for (Sprite sprite : sprites)
            if (sprite.getLayer() == 2) sprite.render(g, alpha);
        g.translate(xCam, yCam);

        //paint front part of exit
        g.setColor(Color.BLACK);
        layer.renderExit1(g, tick, paused?0:alpha);
        
        //paint swimmable stuff
        layer.setCam(xCam, yCam);
        layer.render(g, tick, paused?0:alpha, false, true);        
        
        //paint console stuff
        g.drawImage(Art.console[0][0], 4, 4, null); //mario icon
        g.drawImage(Art.console[5][1], 20, 4, null); // x
        drawNumber(g, Mario.lives, 36, 4);
        
        g.drawImage(Art.console[2][0], 4, 20, null); //coin icon
        g.drawImage(Art.console[5][1], 20, 20, null); // x
        drawNumber(g, Mario.coins, 36, 20);
        
        //spotlight in
        if (startTime > 0){
            float t = startTime + alpha - 2;
            t = t * t * 0.6f;
            renderBlackout(g, 160, 120, (int) (t));
        }

        //spotlight out if won
        if (mario.winTime > 0){
            float t = mario.winTime + alpha;
            t = t * t * 0.2f;

            if (t > 900){
                renderer.levelWon();
                //replayer = new Replayer(recorder.getBytes());
                //init();
            }

            renderBlackout(g, (int) (mario.xDeathPos - xCam), (int) (mario.yDeathPos - yCam), (int) (320 - t));
        }

        //spotlight out if dead
        if (mario.deathTime > 40){
        	int dt = mario.deathTime-40;
            float t = dt + alpha;
            t = t * t * 0.4f;

            if (t > 1800){
                renderer.levelFailed();
                //replayer = new Replayer(recorder.getBytes());
                //init();
            }

            renderBlackout(g, (int) (mario.xDeathPos - xCam), (int) (mario.yDeathPos - yCam), (int) (320 - t));
        }
    }

    //formats one or two digit numbers
    private void drawNumber(Graphics g, int number, int x, int y){
    	Image[] images = new Image[2];
    	int tensDigit = number/10;
    	int onesDigit = number%10;
    	
    	if (tensDigit == 0) images[0] = Art.console[6][1];
    	else if (tensDigit == 9) images[0] = Art.console[7][1];
    	else images[0] = Art.console[tensDigit-1][2];
    	
    	if (onesDigit == 0) images[1] = Art.console[6][1];
    	else if (onesDigit == 9) images[1] = Art.console[7][1];
    	else images[1] = Art.console[onesDigit-1][2];

    	for (int i = 0; i < images.length; i++){
    		g.drawImage(images[i], x, y, null);
    		x += 16;
    	}
    }
    
    private void renderBlackout(Graphics g, int x, int y, int radius){
        if (radius > 320) return;

        g.setColor(new Color(0, 0, 100));

        int[] xp = new int[20];
        int[] yp = new int[20];
        for (int i = 0; i < 16; i++){
            xp[i] = x + (int) (Math.cos(i * Math.PI / 15) * radius);
            yp[i] = y + (int) (Math.sin(i * Math.PI / 15) * radius);
        }
        xp[16] = 320;
        yp[16] = y;
        xp[17] = 320;
        yp[17] = 240;
        xp[18] = 0;
        yp[18] = 240;
        xp[19] = 0;
        yp[19] = y;
        g.fillPolygon(xp, yp, xp.length);

        for (int i = 0; i < 16; i++){
            xp[i] = x - (int) (Math.cos(i * Math.PI / 15) * radius);
            yp[i] = y - (int) (Math.sin(i * Math.PI / 15) * radius);
        }
        xp[16] = 320;
        yp[16] = y;
        xp[17] = 320;
        yp[17] = 0;
        xp[18] = 0;
        yp[18] = 0;
        xp[19] = 0;
        yp[19] = y;

        g.fillPolygon(xp, yp, xp.length);
        
        g.setColor(Color.BLACK);
    }

    public void addSprite(Sprite sprite){
        spritesToAdd.add(sprite);
        sprite.tick();
    }
    public void removeSprite(Sprite sprite){ spritesToRemove.add(sprite); }
    public List<Sprite> getSprites(){ return sprites; }
    
    public float getX(float alpha){
        int xCam = (int) (mario.getXOld() + (mario.getX() - mario.getXOld()) * alpha) - 160;
        if (xCam < 0) xCam = 0;
        return xCam + 160;
    }
    public float getY(float alpha){ return 0; }

    public void bumpUp(int x, int y, boolean canBreakBricks){
    	Block block = level.getBlock(x, y);
        ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
        if (traits.contains(Block.Trait.VertBumpable) && level.getYOffsets()[x][y] == 0){
            bumpInto(x, y - 1);
            boolean shroomsRight = (mario.getX() <= x*16+8);
            boolean bouncy = traits.contains(Block.Trait.Bouncy);
            Block.Powerup powerup = Level.getPowerup(block.blockId);
            level.getYOffsets()[x][y] = 4;
            if (bouncy){
            	sound.play(Art.samples[Art.SAMPLE_SHELL_BUMP], this, 1, 1, 1);
            }
            else{
	            if (powerup == Block.Powerup.FireFlower){
	                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
	                if (!Mario.large) addSprite(new Mushroom(this, x * 16 + 8, y * 16 + 8, true, shroomsRight));
	                else addSprite(new FireFlower(this, x * 16 + 8, y * 16 + 8, true));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            else if (powerup == Block.Powerup.OneUpMushroom){
	                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
	                addSprite(new LifeMushroom(this, x * 16 + 8, y * 16 + 8, true, shroomsRight));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            else if (powerup == Block.Powerup.Coin){
	                Mario.getCoin();
	                sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
	                addSprite(new CoinAnim(x, y));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            else if (powerup == Block.Powerup.Multicoin){
	                Mario.getCoin();
	                sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 - 8, y * 16 - 8), 1, 1, 1);
	                addSprite(new CoinAnim(x, y));
	                if (Math.random() < 0.2f)
	                	level.setBlock(x, y, level.getBlock(x, y).blockId-1);
	            }
	            else if (powerup == Block.Powerup.Star){
	                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 - 8), 1, 1, 1);
	                addSprite(new Star(this, x * 16 + 8, y * 16 - 8, true, shroomsRight));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            else if (powerup == Block.Powerup.BlueSwitch){
	                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
	                addSprite(new Switch(this, x * 16 + 8, y * 16 + 8, 0));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            else if (powerup == Block.Powerup.RedSwitch){
	                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
	                addSprite(new Switch(this, x * 16 + 8, y * 16 + 8, 1));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            else if (powerup == Block.Powerup.YellowSwitch){
	                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
	                addSprite(new Switch(this, x * 16 + 8, y * 16 + 8, 2));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            else if (powerup == Block.Powerup.GreenSwitch){
	                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
	                addSprite(new Switch(this, x * 16 + 8, y * 16 + 8, 3));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            else if (powerup == Block.Powerup.Beanstalk){
	                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
	                addSprite(new Beanstalk(this, x * 16 + 8, y * 16 + 8, true));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            
	            if (traits.contains(Block.Trait.Invisible)){
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
            }
        }
        else{
       		sound.play(Art.samples[Art.SAMPLE_SHELL_BUMP], this, 1, 1, 1);
        }

        if (traits.contains(Block.Trait.Breakable)){
            bumpInto(x, y - 1);
            if (canBreakBricks){
                sound.play(Art.samples[Art.SAMPLE_BREAK_BLOCK], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);

                //figure out which variety of particle this should create
                int blockId = level.getBlock(x, y).blockId;
                int type = 0;
                if (blockId >= Level.ORANGE_BRICK && blockId < Level.ORANGE_BRICK+16) type = 0;
                else if (blockId >= Level.RED_BRICK && blockId < Level.RED_BRICK+16) type = 1;
                else if (blockId >= Level.BLUE_BRICK && blockId < Level.BLUE_BRICK+16) type = 2;
                
                level.setBlock(x, y, Level.DEFAULT_BLOCK);
                for (int xx = 0; xx < 2; xx++)
                    for (int yy = 0; yy < 2; yy++)
                        addSprite(new Particle(x * 16 + xx * 8 + 4, y * 16 + yy * 8 + 4, (xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8, type));
            }
            else level.getYOffsets()[x][y] = 4;
        }
        
        layer.repaint(x-1, y-1, 3, 3);
    }

    public void bumpDown(int x, int y, boolean canBreakBricks){
        Block block = level.getBlock(x, y);
        ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
        if (traits.contains(Block.Trait.VertBumpable) && level.getYOffsets()[x][y] == 0){
            bumpInto(x, y+1);
            boolean shroomsRight = (mario.getX() <= x+8);
            boolean bouncy = traits.contains(Block.Trait.Bouncy);
            Block.Powerup powerup = Level.getPowerup(block.blockId);
            level.getYOffsets()[x][y] = -4;
            if (bouncy){
            	level.getYOffsets()[x][y] = -6;
            	sound.play(Art.samples[Art.SAMPLE_SHELL_BUMP], this, 1, 1, 1);
            }
            else{
	            if (powerup == Block.Powerup.FireFlower){
	                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, (y+2) * 16 - 8), 1, 1, 1);
	                if (!Mario.large) addSprite(new Mushroom(this, x * 16 + 8, (y+2) * 16 - 8, false, shroomsRight));
	                else addSprite(new FireFlower(this, x * 16 + 8, (y+2) * 16 - 8, false));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            else if (powerup == Block.Powerup.OneUpMushroom){
	                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, (y+2) * 16 - 8), 1, 1, 1);
	                addSprite(new LifeMushroom(this, x * 16 + 8, (y+2) * 16 - 8, false, shroomsRight));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            else if (powerup == Block.Powerup.Coin){
	                Mario.getCoin();
	                sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 - 8, y * 16 - 8), 1, 1, 1);
	                addSprite(new CoinAnim(x, y));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            else if (powerup == Block.Powerup.Multicoin){
	                Mario.getCoin();
	                sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 - 8, y * 16 - 8), 1, 1, 1);
	                addSprite(new CoinAnim(x, y));
	                if (Math.random() < 0.2f)
	                	level.setBlock(x, y, level.getBlock(x, y).blockId-1);
	            }
	            else if (powerup == Block.Powerup.Star){
	                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, (y+2) * 16 - 8), 1, 1, 1);
	                addSprite(new Star(this, x * 16 + 8, (y+2) * 16 - 8, false, shroomsRight));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
	            else if (powerup == Block.Powerup.Beanstalk){
	                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
	                addSprite(new Beanstalk(this, x * 16 + 8, y * 16 + 8, false));
	                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	            }
            }
        }

        if (traits.contains(Block.Trait.Breakable)){
            bumpInto(x, y+1);
            if (canBreakBricks){
                sound.play(Art.samples[Art.SAMPLE_BREAK_BLOCK], new FixedSoundSource(x * 16 - 8, y * 16 - 8), 1, 1, 1);

                //figure out which variety of particle this should create
                int blockId = level.getBlock(x, y).blockId;
                int type = 0;
                if (blockId >= Level.ORANGE_BRICK && blockId < Level.ORANGE_BRICK+16) type = 0;
                else if (blockId >= Level.RED_BRICK && blockId < Level.RED_BRICK+16) type = 1;
                else if (blockId >= Level.BLUE_BRICK && blockId < Level.BLUE_BRICK+16) type = 2;
                
                level.setBlock(x, y, Level.DEFAULT_BLOCK);
                for (int xx = 0; xx < 2; xx++)
                    for (int yy = 0; yy < 2; yy++)
                        addSprite(new Particle(x * 16 + xx * 8 + 4, y * 16 + yy * 8 + 4, (xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8, type));
            }
            else level.getYOffsets()[x][y] = -4;
        }

        layer.repaint(x-1, y-1, 3, 3);
    }

    public void bumpLeft(int x, int y, boolean canBreakBricks){
        Block block = level.getBlock(x, y);
        ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
        if (traits.contains(Block.Trait.HorizBumpable) && level.getXOffsets()[x][y] == 0){
            bumpInto(x-1, y);
            level.getXOffsets()[x][y] = -6;
            block.wasBouncingHorizontally = true;
       		sound.play(Art.samples[Art.SAMPLE_SHELL_BUMP], this, 1, 1, 1);
        }
        
        layer.repaint(x-1, y-1, 3, 3);
    }

    public void bumpRight(int x, int y, boolean canBreakBricks){
        Block block = level.getBlock(x, y);
        ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
        if (traits.contains(Block.Trait.HorizBumpable) && level.getXOffsets()[x][y] == 0){
            bumpInto(x+1, y);
            level.getXOffsets()[x][y] = 6;
            block.wasBouncingHorizontally = true;
       		sound.play(Art.samples[Art.SAMPLE_SHELL_BUMP], this, 1, 1, 1);
        }
        
        layer.repaint(x-1, y-1, 3, 3);
    }

    public void bumpInto(int x, int y){
        Block block = level.getBlock(x, y);
        ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
        if (traits.contains(Block.Trait.Pickupable)){
            Mario.getCoin();
            sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);

            if (traits.contains(Block.Trait.Swimmable)) level.setBlock(x, y, Level.WATER);
            else level.setBlock(x, y, Level.DEFAULT_BLOCK);
            addSprite(new CoinAnim(x, y + 1));
        }

        for (Sprite sprite : sprites) sprite.bumpCheck(x, y);
    }
    
    public void knockInto(int x, int y, boolean up, boolean down, boolean left, boolean right){
        //get knocking block
    	Block block = level.getBlock(x, y);
        ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);

    	//add any powerups
        boolean shroomsRight = !right;
        Block.Powerup powerup = Level.getPowerup(block.blockId);
        if (down){
            if (powerup == Block.Powerup.FireFlower){
                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, (y+2) * 16 - 8), 1, 1, 1);
                if (!Mario.large) addSprite(new Mushroom(this, x * 16 + 8, (y+2) * 16 - 8, false, shroomsRight));
                else addSprite(new FireFlower(this, x * 16 + 8, (y+2) * 16 - 8, false));
                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
            }
            else if (powerup == Block.Powerup.OneUpMushroom){
                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, (y+2) * 16 - 8), 1, 1, 1);
                addSprite(new LifeMushroom(this, x * 16 + 8, (y+2) * 16 - 8, false, shroomsRight));
                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
            }
            else if (powerup == Block.Powerup.Coin){
                Mario.getCoin();
                sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 - 8, y * 16 - 8), 1, 1, 1);
                addSprite(new CoinAnim(x, y));
                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
            }
            else if (powerup == Block.Powerup.Multicoin){
                Mario.getCoin();
                sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 - 8, y * 16 - 8), 1, 1, 1);
                addSprite(new CoinAnim(x, y));
                if (Math.random() < 0.2f)
                	level.setBlock(x, y, level.getBlock(x, y).blockId-1);
            }
            else if (powerup == Block.Powerup.Star){
                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, (y+2) * 16 - 8), 1, 1, 1);
                addSprite(new Star(this, x * 16 + 8, (y+2) * 16 - 8, false, shroomsRight));
                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
            }
            else if (powerup == Block.Powerup.BlueSwitch){
                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
                addSprite(new Switch(this, x * 16 + 8, y * 16 + 8, 0));
                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
            }
            else if (powerup == Block.Powerup.RedSwitch){
                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
                addSprite(new Switch(this, x * 16 + 8, y * 16 + 8, 1));
                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
            }
            else if (powerup == Block.Powerup.YellowSwitch){
                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
                addSprite(new Switch(this, x * 16 + 8, y * 16 + 8, 2));
                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
            }
            else if (powerup == Block.Powerup.GreenSwitch){
                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
                addSprite(new Switch(this, x * 16 + 8, y * 16 + 8, 3));
                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
            }
            else if (powerup == Block.Powerup.Beanstalk){
                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
                addSprite(new Beanstalk(this, x * 16 + 8, y * 16 + 8, true));
                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
            }

            if (traits.contains(Block.Trait.Invisible)){
                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
            }
        }
        else{
	        if (powerup == Block.Powerup.FireFlower){
	            sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
	            if (!Mario.large) addSprite(new Mushroom(this, x * 16 + 8, y * 16 + 8, true, shroomsRight));
	            else addSprite(new FireFlower(this, x * 16 + 8, y * 16 + 8, true));
	            level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	        }
	        else if (powerup == Block.Powerup.OneUpMushroom){
	            sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
	            addSprite(new LifeMushroom(this, x * 16 + 8, y * 16 + 8, true, shroomsRight));
	            level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	        }
	        else if (powerup == Block.Powerup.Coin){
	            Mario.getCoin();
	            sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
	            addSprite(new CoinAnim(x, y));
	            level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
	        }
            else if (powerup == Block.Powerup.Multicoin){
                Mario.getCoin();
                sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 - 8, y * 16 - 8), 1, 1, 1);
                addSprite(new CoinAnim(x, y));
                if (Math.random() < 0.2f)
                	level.setBlock(x, y, level.getBlock(x, y).blockId-1);
            }
            else if (powerup == Block.Powerup.Star){
                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 - 8), 1, 1, 1);
                addSprite(new Star(this, x * 16 + 8, (y+2) * 16 - 8, false, shroomsRight));
                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
            }
            else if (powerup == Block.Powerup.Beanstalk){
                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
                addSprite(new Beanstalk(this, x * 16 + 8, y * 16 + 8, true));
                level.setBlock(x, y, Level.getDeadBlockId(block.blockId));
            }
        }
        
        //figure knocked block
    	int xt = x, yt = y;
    	if (left) xt--;
    	else if (right) xt++;
    	else if (up) yt++;
    	else if (down) yt--;
    	Block kblock = level.getBlock(xt, yt);
    	ArrayList<Block.Trait> ktraits = Level.getBehavior(kblock.blockId);

    	//pickup if pickupable (coin)
        if (ktraits.contains(Block.Trait.Pickupable)){
            Mario.getCoin();
            sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(xt * 16 + 8, yt * 16 + 8), 1, 1, 1);
            if (ktraits.contains(Block.Trait.Swimmable)) level.setBlock(x, y, Level.WATER);
            else level.setBlock(x, y, Level.DEFAULT_BLOCK);
            addSprite(new CoinAnim(xt, yt + 1));
        }

        //knock anything that might be there
        for (Sprite sprite : sprites) sprite.knockCheck(xt, yt, up, down, left, right);
    }
    
    public void createWinDoor(int ix, int iy){
        Marker winDoor = new Marker("WIN_DOOR", ix, iy+1, 16, 32, Marker.Type.WIN_DOOR);
        level.setMarker(ix, iy, winDoor);
    	level.setBlock(ix, iy, Level.DOOR_BOTTOM);
    	level.setBlock(ix, iy-1, Level.DOOR_TOP);
    	level.setBlock(ix+1, iy, Level.DOOR_SHADOW_BOTTOM);
    	level.setBlock(ix+1, iy-1, Level.DOOR_SHADOW_TOP);
    }
}