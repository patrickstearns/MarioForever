package com.mojang.mario.sprites;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.*;
import com.mojang.mario.*;
import com.mojang.mario.level.*;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.enemies.BulletBill;
import com.mojang.mario.sprites.projectiles.Fireball;
import com.mojang.sonar.*;

public class Mario extends Sprite {

	public static Mario instance;

    public static boolean large = false;
    public static boolean fire = false;
    public static int coins = 0;
    public static int lives = 3;
    public static String levelString = "none";
    public static Marker lastMarker = null;
    
    public static void resetStatic(){
        large = false;
        fire = false;
        coins = 0;
        lives = 2;
        levelString = "none";
        lastMarker = null;
    }

    public static final int KEY_LEFT = 0;
    public static final int KEY_RIGHT = 1;
    public static final int KEY_DOWN = 2;
    public static final int KEY_UP = 3;
    public static final int KEY_JUMP = 4;
    public static final int KEY_SPEED = 5;

    private static float SLIPPERY_GROUND_INERTIA = 0.99f;
    private static float GROUND_INERTIA = 0.89f;
    private static float AIR_INERTIA = 0.89f;

    public boolean[] keys;
    public boolean wasOnGround = false, onGround = false, inWater = false;
    boolean onSlipperyGround = false, onBouncyGround = false, canClimb = false;
    private boolean mayJump = false, ducking = false, wallSliding = false;
    private boolean buttStomping = false, swimming = false;

	public boolean climbing = false;
    private boolean canClimbUp = false, canClimbDown = false, canShoot = false;
    public boolean enteringPipeUp = false, enteringPipeDown = false, enteringPipeLeft = false, enteringPipeRight = false;
    public boolean exitingPipeUp = false, exitingPipeDown = false, exitingPipeLeft = false, exitingPipeRight = false;
    public boolean enteringDoor = false, exitingDoor = false, enteringBeanstalkUp = false, exitingBeanstalkUp = false,
    	enteringBeanstalkDown = false, exitingBeanstalkDown = false;
    private boolean lastLarge, lastFire, newLarge, newFire;
    private int[] winRewards = null;
    public int facing, xDeathPos, yDeathPos;
    public int deathTime = 0, winTime = 0, winningTime = 0;
    public int powerUpTime = 0, invulnerableTime = 0, enteringPipeTime = 0, exitingPipeTime = 0;
    private int jumpTime = 0, climbTime = 0, starTime = 0, swimTime = 0;
    private float xJumpSpeed, yJumpSpeed, runTime;
    
    public Sprite carried = null;
    public Marker enteringMarker = null, exitingMarker = null;
    
    public Mario(LevelScene world){
    	super(world);
    	Mario.instance = this;
        keys = Scene.keys;
        setX(32);
        setY(0);
        setWidth(4);

        facing = 1;
        setLarge(Mario.large, Mario.fire);
    }
    
    private void blink(boolean on){
        Mario.large = on?newLarge:lastLarge;
        Mario.fire = on?newFire:lastFire;
        
        if (large){
            setSheet(Art.mario);
            if (fire) setSheet(Art.fireMario);

            setXPicO(16);
            setYPicO(31);
            setWPic(32);
            setHPic(32);
        }
        else {
            setSheet(Art.smallMario);

            setXPicO(8);
            setYPicO(15);
            setWPic(16);
            setHPic(16);
        }

        calcPic();
    }

    void setLarge(boolean large, boolean fire) {
        if (fire) large = true;
        if (!large) fire = false;
        
        lastLarge = Mario.large;
        lastFire = Mario.fire;
        
        Mario.large = large;
        Mario.fire = fire;

        newLarge = Mario.large;
        newFire = Mario.fire;
        
        blink(true);
    }
    
    public boolean isStarInvulnerable(){ return starTime > 0; }
    void setStarInvulnerable(){ starTime = 300; }

    public void move(){
    	if (winningTime > 0){
    		//check for win reward
    		int rewardIndex = winningTime-1;
    		if (winRewards == null || rewardIndex >= winRewards.length)
    			rewardIndex = -1;

    		//if they get something...
    		if (rewardIndex != -1){
    			if (winRewards[rewardIndex] == 0){ //coin
	                Mario.getCoin();
	                getWorld().sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(getX(), getY()-getHeight()), 1, 1, 1);
	                spriteContext.addSprite(new CoinAnim((int)getX(), (int)(getY()-getHeight())));
    			}
    			else if (winRewards[rewardIndex] == 1){ //one-up
    				get1Up();
    			}
    		}

    		if (winningTime > 70){
    			winningTime = 0;
    			finishWin();
    			return;
    		}
    		
    		winningTime++;
            setXa(0);
            setYa(0);
            return;
        }

    	if (winTime > 0){
    		winTime++;
    		return;
    	}
    	
        if (deathTime > 0){
            deathTime++;
            if (deathTime < 11){
	            setXa(0);
	            setYa(0);
            }
            else if (deathTime == 11) setYa(-15);
            else setYa(getYa()+2);

            setX(getX()+getXa());
            setY(getY()+getYa());

            calcPic();
            return;
        }

        if (powerUpTime != 0){
            if (powerUpTime > 0){
                powerUpTime--;
                blink(((powerUpTime / 3) & 1) == 0);
            }
            else{
                powerUpTime++;
                blink(((-powerUpTime / 3) & 1) == 0);
            }

            if (powerUpTime == 0) getWorld().actionPaused = false;

            calcPic();
            return;
        }
        
        //NOTHING BEYOND THIS POINT RUNS WHEN WORLD IS PAUSED
        if (getWorld().paused) return;
        
        if (enteringPipeTime != 0){
        	//just entered pipe
        	if (enteringPipeTime == 1){
        		getWorld().actionPaused = true;

           		if (enteringMarker.type == Marker.Type.WIN_DOOR){
        			win(false);
        			return;
        		}
        		
        		getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_PIPE], this, 1, 1, 1);

		        if (enteringPipeLeft){
		        	facing = -1;
		        	setX((enteringMarker.x+1)*16+2);
		        	setY(enteringMarker.y*16+enteringMarker.height-2);
		        }
		        else if (enteringPipeRight){
		        	facing = 1;
		        	setX((enteringMarker.x*16-1)+1);
		        	setY(enteringMarker.y*16+enteringMarker.height-2);
		        }
		        else if (enteringPipeUp || enteringBeanstalkUp){
		        	setX(enteringMarker.x*16+enteringMarker.width/2-getWidth()/2);
		        	setY((enteringMarker.y+1)*16+getHeight());
		        }
		        else if (enteringPipeDown || enteringBeanstalkDown){
		        	setX(enteringMarker.x*16+enteringMarker.width/2-getWidth()/2);
		        	setY(enteringMarker.y*16+enteringMarker.height-getHeight());
		        }
		        else if (enteringDoor){
		        	setX(2+enteringMarker.x*16+enteringMarker.width/2-getWidth()/2);
		        	setY(enteringMarker.y*16+enteringMarker.height-1);
		        }
        		
		        setLayer(-1);
        	}
        	
        	//while entering pipe
        	enteringPipeTime++;
        	int maxEnteringPipeTime = getHeight();
        	if (enteringPipeLeft || enteringPipeRight) maxEnteringPipeTime = getWidth()*2;
        	if (enteringDoor) maxEnteringPipeTime = 16;
        	
       		//done, go to new area
        	if (enteringPipeTime > maxEnteringPipeTime){
        		LevelArea newArea = getWorld().level.findAreaForPortal(enteringMarker.id, false); //find entrance
        		exitingMarker = getWorld().level.findEntranceForExit(enteringMarker.id);
        		if (getWorld().level.getCurrentArea() != newArea){
			    	//unset any switches
			    	getWorld().level.unsetSwitch(0);
			    	getWorld().level.unsetSwitch(1);
    	
        			getWorld().level.setCurrentArea(newArea);
			        getWorld().areaChanged();
        		}

		        exitingPipeLeft = (exitingMarker.type == Marker.Type.ENTRANCE_BIGPIPE_LEFT 
	        		|| exitingMarker.type == Marker.Type.ENTRANCE_SMALLPIPE_LEFT);
		        exitingPipeRight = (exitingMarker.type == Marker.Type.ENTRANCE_BIGPIPE_RIGHT
	        		|| exitingMarker.type == Marker.Type.ENTRANCE_SMALLPIPE_RIGHT);
		        exitingPipeUp = (exitingMarker.type == Marker.Type.ENTRANCE_BIGPIPE_UP ||
	        		exitingMarker.type == Marker.Type.ENTRANCE_SMALLPIPE_UP);
		        exitingPipeDown = (exitingMarker.type == Marker.Type.ENTRANCE_BIGPIPE_DOWN ||
	        		exitingMarker.type == Marker.Type.ENTRANCE_SMALLPIPE_DOWN);
		        exitingDoor = (exitingMarker.type == Marker.Type.ENTER_DOOR);
		        exitingBeanstalkDown = (exitingMarker.type == Marker.Type.ENTER_BEANSTALK_DOWNWARD);
		        exitingBeanstalkUp = (exitingMarker.type == Marker.Type.ENTER_BEANSTALK_UPWARD);
		        
		        if (exitingMarker.type == Marker.Type.ENTRANCE_BIGPIPE_LEFT 
	        		|| exitingMarker.type == Marker.Type.ENTRANCE_SMALLPIPE_LEFT) facing = -1;
		        if (exitingMarker.type == Marker.Type.ENTRANCE_BIGPIPE_RIGHT
	        		|| exitingMarker.type == Marker.Type.ENTRANCE_SMALLPIPE_RIGHT) facing = 1;
		        exitingPipeTime = 1;

		        if (exitingPipeLeft){
		        	facing = -1;
		        	setX(exitingMarker.x*16+2);
		        	setY(exitingMarker.y*16+exitingMarker.height-2);
		        }
		        else if (exitingPipeRight){
		        	facing = 1;
		        	setX((exitingMarker.x)*16+exitingMarker.width-getWidth()+1);
		        	setY(exitingMarker.y*16+exitingMarker.height-2);
		        }
		        else if (exitingPipeUp || exitingBeanstalkUp){
		        	setX(exitingMarker.x*16+exitingMarker.width/2-getWidth()/2);
		        	setY(exitingMarker.y*16+getHeight());
		        }
		        else if (exitingPipeDown || exitingBeanstalkDown){
		        	setX(exitingMarker.x*16+exitingMarker.width/2-getWidth()/2);
		        	setY((exitingMarker.y+1)*16+exitingMarker.height-getHeight());
		        }
		        else if (exitingDoor){
		        	setX(1+exitingMarker.x*16+exitingMarker.width/2-getWidth()/2);
		        	setY(exitingMarker.y*16+exitingMarker.height-1);
		        	alphaTransparency = 0;
		        }

				enteringPipeLeft = false;
				enteringPipeRight = false;
        		enteringPipeUp = false;
        		enteringPipeDown = false;
        		enteringDoor = false;
        		enteringPipeTime = 0;
        		enteringMarker = null;

                getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_PIPE], this, 1, 1, 1);
        	}
        	else{
	        	setXa(0);
	        	setYa(0);
	        	if (enteringPipeLeft || enteringPipeRight) setXa(facing/2f);
	        	else if (enteringPipeUp) setYa(-1f);
	        	else if (enteringPipeDown) setYa(1f);
	        	
	            setX(getX()+getXa());
	            setY(getY()+getYa());
	
	        	if (enteringDoor) alphaTransparency = (16f-(float)enteringPipeTime)/16f;
        	}
        	
        	calcPic();
        	return;
        }

        //leaving a pipe/door
        if (exitingPipeTime != 0){
        	//next two lines prevent problems when coming down out of a pipe
        	ducking = false;
        	calcPic();
        	
        	exitingPipeTime++;
        	int maxExitingPipeTime = getHeight()+2;
        	if (exitingPipeLeft || exitingPipeRight) maxExitingPipeTime = 8*2+1;
        	if (exitingDoor) maxExitingPipeTime = 16;

        	//done exiting pipe/door
        	if (exitingPipeTime > maxExitingPipeTime){
        		getWorld().actionPaused = false;
        		exitingPipeLeft = false;
        		exitingPipeRight = false;
        		exitingPipeUp = false;
        		exitingPipeDown = false;
        		exitingDoor = false;
        		exitingMarker = null;
        		exitingPipeTime = 0;
        		alphaTransparency = 1;
        		setLayer(1);
	        	setXa(0);
	        	setYa(0);
        	}
        	else{
	        	setXa(0);
	        	setYa(0);
	        	if (exitingPipeLeft || exitingPipeRight) setXa(facing/2f);
	        	else if (exitingPipeUp) setYa(-1f);
	        	else if (exitingPipeDown) setYa(1f);
	
	            setX(getX()+getXa());
	            setY(getY()+getYa());

	        	if (exitingDoor) alphaTransparency = (float)exitingPipeTime/16f;
        	}
        	
        	calcPic();
        	return;
        }

        //NOTHING BEYOND THIS POINT WHEN WORLD IS PAUSED
        if (getWorld().actionPaused) return;
        
        //star countdown
        int oldStarTime = starTime;
        if (starTime > 0){
        	starTime--;
        	calcPic();
        }

        //invulnerability has worn off
        if (oldStarTime > 0 && starTime == 0){
    		Art.startMusic(Level.songForLevelType(getWorld().level.getBackgroundType()));
			calcPic();
        }
        
        //if still inside block-all stuff, be pushed to the right
        if (Level.getBehavior(getWorld().level.getBlock((int)(getX()/16), (int)(getY()/16)).blockId).contains(Block.Trait.BlockAll)){
        	setX(getX()+2);
        	return;
        }
        else if (getHeight() > 16 && Level.getBehavior(getWorld().level.getBlock((int)(getX()/16), (int)(getY()/16)-1).blockId).contains(Block.Trait.BlockAll)){
        	setX(getX()+2);
        	return;
        }
        if (Level.getBehavior(getWorld().level.getBlock((int)((getX()-getWidth())/16), (int)(getY()/16)).blockId).contains(Block.Trait.BlockAll)){
        	setX(getX()+2);
        	return;
        }
        else if (getHeight() > 16 && Level.getBehavior(getWorld().level.getBlock((int)((getX()-getWidth())/16), (int)(getY()/16)-1).blockId).contains(Block.Trait.BlockAll)){
        	setX(getX()+2);
        	return;
        }

        if (invulnerableTime > 0) invulnerableTime--;
        setVisible(((invulnerableTime / 2) & 1) == 0);

        swimming = (!onGround && inWater);

        //set speed
        wasOnGround = onGround;
        float sideWaysSpeed = (wallSliding || (keys[KEY_SPEED] && !climbing)) ? 0.8f : 0.4f;
        if (inWater) sideWaysSpeed = 0.4f;
        if (inWater && onGround) sideWaysSpeed = 0.3f;

        //set if ducking
        if (onGround && !climbing){
        	if (keys[KEY_DOWN] && large) ducking = true;
            else ducking = false;
        }
        
        //set facing
        if (getXa() > 2) facing = 1;
        if (getXa() < -2) facing = -1;

        swimTime--;
        
        //if in the air or hit jump
        if (keys[KEY_JUMP] || (jumpTime < 0 && !onGround && !wallSliding)){
        	//in the air and going down
        	if (jumpTime < 0){
                setXa(xJumpSpeed);
                setYa(-jumpTime * yJumpSpeed);
                if (inWater) setYa(getYa()/4f);
                jumpTime++;
            }
            //jump
            else if ((onGround && mayJump) || swimming || climbing){
            	if ((swimming && swimTime <= 0) || !swimming){
	            	int sample = Art.SAMPLE_MARIO_JUMP;
	            	if (swimming) {
	            		sample = Art.SAMPLE_MARIO_SWIM;
	            		swimTime = 10;
	            	}
	                getWorld().sound.play(Art.samples[sample], this, 1, 1, 1);
	
	                //if running, jump higher
	                xJumpSpeed = 0;
	                yJumpSpeed = -1.6f;
	                jumpTime = 7;
	                if (keys[KEY_SPEED]) jumpTime = 9;
	                if (inWater){
	                	jumpTime = 20; //slower but stronger
	                	yJumpSpeed = -0.9f;
	                }
	                
	                //bouncy terrain way high
	                if (!swimming && onBouncyGround) jumpTime = 10;
	                
	                setYa(jumpTime * yJumpSpeed);
	                onGround = false;
	                wallSliding = false;
	                climbing = false;
            	}
            }
            //if wallsliding and jumped
            else if (wallSliding && mayJump){
                getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_JUMP], this, 1, 1, 1);
                xJumpSpeed = -facing * 6.0f;
                yJumpSpeed = -2.0f;
                jumpTime = -6;
                setXa(xJumpSpeed);
                setYa(-jumpTime * yJumpSpeed);
                onGround = false;
                wallSliding = false;
                climbing = false;
                facing = -facing;
            }
        	//if on the way up
            else if (jumpTime > 0){
                setXa(getXa()+xJumpSpeed);
                setYa(jumpTime * yJumpSpeed);
                if (inWater) setYa(getYa()/2f);
                jumpTime--;
            }
        }
        else{
        	jumpTime = 0;
        	swimTime = 0;
        }
        
        //climbing
        if (keys[KEY_UP] && canClimb && !buttStomping){
        	climbing = true;
        	setXa(0);
        	if (canClimbUp) setYa(-2);
        	jumpTime = 0;
        	wallSliding = false;

        	climbTime++;
        	if (climbTime > 4){
        		facing *= -1;
        		climbTime = 0;
        	}
        }
        else if (keys[KEY_DOWN] && canClimb && !buttStomping){
        	climbing = true;
        	setXa(0);
        	if (canClimbDown) setYa(4);
        	jumpTime = 0;
        	wallSliding = false;
        }
        if (!climbing) climbTime = 0;
        
        //moving left
        if (keys[KEY_LEFT] && (swimming || (!onGround || !ducking))){
            if (facing == 1) wallSliding = false;
            setXa(getXa()-sideWaysSpeed);
            if (jumpTime >= 0 || swimming) facing = -1;
        }

        //moving right
        if (keys[KEY_RIGHT] && (swimming || (!onGround || !ducking))){
            if (facing == -1) wallSliding = false;
            setXa(getXa()+sideWaysSpeed);
            if (jumpTime >= 0 || swimming) facing = 1;
        }

        if ((!keys[KEY_LEFT] && !keys[KEY_RIGHT]) || ducking || getYa() < 0 || onGround) wallSliding = false;
        
        if (keys[KEY_DOWN] && !buttStomping && !onGround && carried == null && large && !swimming && !climbing){
        	setXa(getXa()*GROUND_INERTIA);
        	buttStomping = true;
        }
        
        if (!keys[KEY_DOWN] && buttStomping) buttStomping = false;

        if (keys[KEY_SPEED] && canShoot && Mario.fire && getWorld().fireballsOnScreen<2){
            getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_FIREBALL], this, 1, 1, 1);
            getWorld().addSprite(new Fireball(getWorld(), getX()+facing*6, getY()-20, facing, this, false, 0));
        }

        //entering a door
        if (keys[KEY_UP] && onGround){
			Marker marker = getWorld().level.getMarker((int)(getX()/16), (int)(getY()/16), false);
            if (marker != null){
            	if (marker.type == Marker.Type.EXIT_DOOR){
            		enteringPipeLeft = false;
            		enteringPipeRight = false;
            		enteringPipeTime = 1;
            		enteringMarker = marker;
            		enteringDoor = true;
            		calcPic();
            	}
            	else if (marker.type == Marker.Type.WIN_DOOR){
            		enteringPipeLeft = false;
            		enteringPipeRight = false;
            		enteringPipeTime = 1;
            		enteringMarker = marker;
            		enteringDoor = true;
            		calcPic();
            	}
            }
        }
        
        //beanstalk exit upward
        if (keys[KEY_UP] && climbing){
			Marker marker = getWorld().level.getMarker((int)(getX()/16), (int)(getY()/16), false);
            if (marker != null && marker.type == Marker.Type.EXIT_BEANSTALK_UPWARD){
        		enteringPipeLeft = false;
        		enteringPipeRight = false;
        		enteringPipeTime = 1;
        		enteringMarker = marker;
        		enteringDoor = true;
        		calcPic();
            }
        }
        
        //beanstalk exit downward
        if (keys[KEY_DOWN] && climbing){
			Marker marker = getWorld().level.getMarker((int)(getX()/16), (int)(getY()/16), false);
            if (marker != null && marker.type == Marker.Type.EXIT_BEANSTALK_DOWNWARD){
        		enteringPipeLeft = false;
        		enteringPipeRight = false;
        		enteringPipeTime = 1;
        		enteringMarker = marker;
        		enteringDoor = true;
        		calcPic();
            }
        }
        
        canShoot = !keys[KEY_SPEED];
        mayJump = (onGround || wallSliding || climbing) && !keys[KEY_JUMP];
        setXFlipPic(facing == -1);

        runTime += (Math.abs(getXa())) + 5;
        if (Math.abs(getXa()) < 0.3f){
            runTime = 0;
            setXa(0);
        }

        calcPic();

        if (wallSliding){
            for (int i = 0; i < 1; i++){
                getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 4 - 2) + facing * 8, (int) (getY() + Math.random() * 4), (float) (Math.random() * 2 - 1), (float) Math.random() * 1, 0, 1, 5));
            }
            setYa(getYa()*0.3f);
        }

        if (buttStomping) setXa(getXa()/4f);
        
        onGround = false;
        move(getXa(), 0);
        move(0, getYa());
        
        //die if fall off bottom
        if (getY() > getWorld().level.getHeight() * 16 + 16) die();

        //left edge
        if (getX() < 0){
            setX(0);
            setXa(0);
        }

        //right edge (exit)
        Marker exit = getWorld().level.getCurrentArea().findMarker(Marker.Type.END_POS);
        int levelBandY = getWorld().getLayer().getLevelBandY();
        if (winningTime == 0 && winTime == 0){
	        if (exit != null && getX() >= (exit.x*16+1)-8 && getX() <= (exit.x*16+1)+16 &&
	        		(levelBandY <= getY() && levelBandY > getY()-getHeight())){
	        	//figure reward based on how high from the marker you are
	        	int height = (int)(exit.y*16 - getY());
	        	if (height > 80){
	        		//one up
	        		winRewards = new int[]{1};
	        	}
	        	else if (height > 64){
	        		//twenty coins
	        		winRewards = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	        	}
	        	else if (height > 48){
	        		//ten coins
	        		winRewards = new int[]{0,0,0,0,0,0,0,0,0,0};
	        	}
	        	else if (height > 32){
	        		//five coins
	        		winRewards = new int[]{0,0,0,0,0};
	        	}
	        	else if (height > 16){
	        		//two coins
	        		winRewards = new int[]{0,0};
	        	}
	        	else{
	        		//one coin
	        		winRewards = new int[]{0};
	        	}
	        	
	        	win(true);
	        }
        }
        
        //right edge (no exit)
        if (getX() > getWorld().level.getWidth() * 16){
            setX(getWorld().level.getWidth() * 16);
            setXa(0);
        }

        setYa(getYa()*0.85f);
        if (inWater){
        	setXa(getXa()/1.2f);
        	setYa(getYa()/1.1f);
//        	if (getYa() > 7) setYa(5);
//        	if (getYa() < -7) setYa(-5);
        	
        	//possibly create a bubble
        	if (Math.random() > 0.96){
        		spriteContext.addSprite(new Bubble(getWorld(), getX(), getY()-getHeight()+4));
        	}
        }
        else{
	        if (onGround){
	        	if (onSlipperyGround){
	        		setXa(getXa()*SLIPPERY_GROUND_INERTIA);
	        		if (getXa() > 10) setXa(10);
	        		else if (getXa() < -10) setXa(-10);
	        	}
	        	else setXa(getXa()*GROUND_INERTIA);
	        }
	        else{
	            setXa(getXa()*AIR_INERTIA);
	        }
        }

        //if climbing and stopped moving, stop dead
        if (climbing && !keys[KEY_UP] && !keys[KEY_DOWN]) setYa(0);
        
        //fall if in the air; drift if swimming
        if (swimming){
        	setYa(getYa()+1);
	        if (getYa() > 5) setYa(5);
	        if (getYa() < -5) setYa(-5);
        }
        else if (!onGround && !climbing) setYa(getYa()+3);

//        if (onGround && getYa() >= 0){ //makes running over 1-unit gaps mostly work (but seems to break jumping)
//        	setYa(0.5f);
//        }
        
        //if carrying a shell, update its position and maybe let go
        if (carried != null){
            carried.setX(getX()+facing*8); //was 8
            carried.setY(getY()-2);
            if (!keys[KEY_SPEED]){ //release a shell, if carrying one and not holding the button anymore
                carried.release(this);
                carried = null;
            }
        }
    }

    private void calcPic(){
        int runFrame = 0;

        if (large){
            runFrame = ((int) (runTime / 20)) % 4;
            if (runFrame == 3) runFrame = 1;
            int orf = runFrame;
            if (carried == null && Math.abs(getXa()) > 10) runFrame += 3;
            if (carried != null) runFrame += 10;
            if (!onGround){
                if (carried != null) runFrame = 12;
                else if (buttStomping) runFrame = 8;
                else if (wallSliding) runFrame = 15;
                else if (Math.abs(getXa()) > 10) runFrame = 7;
                else runFrame = 6;
            }
            if (inWater && !onGround) runFrame = orf+16;
        }
        else{
            runFrame = ((int) (runTime / 20)) % 2;
            int orf = runFrame;
            if (carried == null && Math.abs(getXa()) > 10) runFrame += 2;
            if (carried != null) runFrame += 8;
            if (!onGround){
                if (carried != null) runFrame = 9;
                else if (wallSliding) runFrame = 11;
                else if (Math.abs(getXa()) > 10) runFrame = 5;
                else runFrame = 4;
            }
            if (inWater && !onGround) runFrame = orf+13;
        }

        if (!inWater && onGround && ((facing == -1 && getXa() > 0) || (facing == 1 && getXa() < 0))){
            if (getXa() > 1 || getXa() < -1) runFrame = large ? 9 : 7;

            if (getXa() > 3 || getXa() < -3){
                for (int i = 0; i < 3; i++){
                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 8 - 4), (int) (getY() + Math.random() * 4), (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
                }
            }
        }

        if (climbing){
        	runFrame = large ? 19 : 16;
        }
        
        if (large){
            if (ducking) runFrame = 14;
            setHeight(ducking ? 12 : 24);
        }
        else{
            setHeight(12);
        }

        if (enteringPipeUp || enteringPipeDown || exitingPipeUp || exitingPipeDown){
        	if (large) runFrame = 21;
        	else runFrame = 16;
        }
        if (enteringPipeLeft || enteringPipeRight || exitingPipeLeft || exitingPipeRight){
        	runFrame = 0;
        }
        if (enteringDoor){
        	if (large) runFrame = 20;
        	else runFrame = 16;
        }
        if (exitingDoor){
        	if (large) runFrame = 21;
        	else runFrame = 0;
        }

        if (deathTime > 0) runFrame = 15;
        
        setXPic(runFrame);
    }

    protected boolean move(float xa, float ya){
    	float step = 0.5f;
    	while (xa > step){
            if (!move(step, 0)) return false;
            xa -= step;
        }
        while (xa < -step){
            if (!move(-step, 0)) return false;
            xa += step;
        }
        while (ya > step){
            if (!move(0, step)) return false;
            ya -= step;
        }
        while (ya < -step){
            if (!move(0, -step)) return false;
            ya += step;
        }

        boolean collide = false;
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();

//        Rectangle2D.Float r = new Rectangle2D.Float(x-width+xa, y-height+ya, width*2, height);
//        if (getWorld().level.getCurrentArea().getBlockingArea().intersects(r)) collide = true;

        if (ya > 0){
//            Rectangle2D.Float r = new Rectangle2D.Float(x+xa-width, y+ya, width*2, 1);
//            if (getWorld().level.getCurrentArea().getBlockingArea().intersects(r)) collide = true;

            if (collide || isBlocking(x + xa, y + ya, xa, 0)) collide = true;
	        else if (collide || isBlocking(x + xa - width, y + ya, xa, 0)) collide = true;
            else if (collide || isBlocking(x + xa + width, y + ya, xa, 0)) collide = true;
            else if (collide || isBlocking(x + xa - width, y + ya + 1, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa + width, y + ya + 1, xa, ya)) collide = true;
//if (collide) System.out.println("ya > 0 collide at "+getY());
        }
        if (ya < 0){
//            Rectangle2D.Float r = new Rectangle2D.Float(x+xa-width, y+ya-height, width*2, 1);
//            if (getWorld().level.getCurrentArea().getBlockingArea().intersects(r)) collide = true;

            if (collide || isBlocking(x + xa, y + ya - height, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
        }
        if (xa > 0){
        	if (carried == null && !swimming) wallSliding = true;

//            Rectangle2D.Float r = new Rectangle2D.Float(x+xa+width+1, y+ya-height, 1, height/2);
//            if (getWorld().level.getCurrentArea().getBlockingArea().intersects(r)) collide = true;
            if (collide || isBlocking(x + xa + width + 1, y + ya - height, xa, ya)) collide = true;
            if (collide || isBlocking(x + xa + width + 1, y + ya - height/2, xa, ya)) collide = true;
            else wallSliding = false;

//            r = new Rectangle2D.Float(x+xa+width+1, y+ya-height/2, 1, height/2);
//            if (getWorld().level.getCurrentArea().getBlockingArea().intersects(r)) collide = true;
            if (collide || isBlocking(x + xa + width + 1, y + ya, xa, ya)) collide = true;
            else wallSliding = false;
        }
        if (xa < 0){
            if (carried == null && !swimming) wallSliding = true;

//            Rectangle2D.Float r = new Rectangle2D.Float(x+xa-width, y+ya-height, 1, height/2);
//            if (getWorld().level.getCurrentArea().getBlockingArea().intersects(r)) collide = true;
            if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
            if (collide || isBlocking(x + xa - width, y + ya - height/2, xa, ya)) collide = true;
            else wallSliding = false;

//            r = new Rectangle2D.Float(x+xa-width, y+ya-height/2, 1, height/2);
//            if (getWorld().level.getCurrentArea().getBlockingArea().intersects(r)) collide = true;
            if (collide || isBlocking(x + xa - width, y + ya, xa, ya)) collide = true;
            else wallSliding = false;
        }

		int ix = (int)(x/16);
		int iy = (int)(y/16);
		Block block = getWorld().level.getBlock(ix, iy);
		ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);

		boolean wasInWater = inWater;
		inWater = traits.contains(Block.Trait.Swimmable);
		canClimb = traits.contains(Block.Trait.Climbable);
		if (inWater && !wasInWater){
			ya = 0;
			jumpTime = -1;
		}
		else if (!inWater && wasInWater){
			if (ya < 0) ya *= 2;
			jumpTime = 8;
		}

		//climbing stuff
		if (!canClimb) climbing = false;
		Block ublock = getWorld().level.getBlock(ix, iy-1);
		Block dblock = getWorld().level.getBlock(ix, iy+1);
		ArrayList<Block.Trait> utraits = Level.getBehavior(ublock.blockId);
		ArrayList<Block.Trait> dtraits = Level.getBehavior(dblock.blockId);
		canClimbUp = utraits.contains(Block.Trait.Climbable);
		canClimbDown = dtraits.contains(Block.Trait.Climbable);
		
    	if (xa < 0){
    		ix = (int)(x/16)-1;
    		iy = (int)(y/16);
			block = getWorld().level.getBlock(ix, iy);
			traits = Level.getBehavior(block.blockId);
			if (traits.contains(Block.Trait.Kills)) die();
        }
        if (xa > 0){
    		ix = (int)(x/16)+1;
    		iy = (int)(y/16);
			block = getWorld().level.getBlock(ix, iy);
			traits = Level.getBehavior(block.blockId);
			if (traits.contains(Block.Trait.Kills)) die();
        }
        if (ya < 0){
    		ix = (int)(x/16);
    		iy = (int)((y-getHeight())/16)-1;
			block = getWorld().level.getBlock(ix, iy);
			traits = Level.getBehavior(block.blockId);
			if (traits.contains(Block.Trait.Kills)) die();
        }
        if (ya > 0){
        	ix = (int)(x/16);
    		iy = (int)(y/16)+1;
			block = getWorld().level.getBlock(ix, iy);
			traits = Level.getBehavior(block.blockId);
			if (traits.contains(Block.Trait.Kills)) die();
        }
		
		if (collide){
        	float newX = x, newY = y;
        	
        	if (xa < 0){
        		ix = (int)(newX/16)-1;
        		iy = (int)(newY/16);
				block = getWorld().level.getBlock(ix, iy);
				traits = Level.getBehavior(block.blockId);
				
				if (traits.contains(Block.Trait.Hurts)) getHurt();
				else if (traits.contains(Block.Trait.Kills)) die();
				
                newX = (int) ((newX - width) / 16) * 16 + width;
                setXa(0);
                
				Marker marker = getWorld().level.getMarker(ix, iy, false);
				if (marker != null && Math.abs(xa) < 2){
                	if (marker.type == Marker.Type.EXIT_BIGPIPE_LEFT ||
                			(marker.type == Marker.Type.EXIT_SMALLPIPE_LEFT && !large)){
                		if (keys[KEY_LEFT]){
	                		enteringPipeLeft = true;
	                		enteringPipeRight = false;
	                		enteringPipeTime = 1;
	                		enteringMarker = marker;
	                		calcPic();
                		}
                	}
                		
                }
            }
            if (xa > 0){
        		ix = (int)(newX/16)+1;
        		iy = (int)(newY/16);
				block = getWorld().level.getBlock(ix, iy);
				traits = Level.getBehavior(block.blockId);

				if (traits.contains(Block.Trait.Hurts)) getHurt();
				else if (traits.contains(Block.Trait.Kills)) die();
				
	        	newX = (int) ((newX + width) / 16 + 1) * 16 - width - 1;
                setXa(0);

				Marker marker = getWorld().level.getMarker(ix, iy, false);
                if (marker != null && Math.abs(xa) < 2){
                	if (marker.type == Marker.Type.EXIT_BIGPIPE_RIGHT ||
                			(marker.type == Marker.Type.EXIT_SMALLPIPE_RIGHT && !large)){
                		if (keys[KEY_RIGHT]){
	                		enteringPipeLeft = false;
	                		enteringPipeRight = true;
	                		enteringPipeTime = 1;
	                		enteringMarker = marker;
	                		calcPic();
                		}
                	}
                		
                }
            }
            if (ya < 0){
        		ix = (int)(newX/16);
        		iy = (int)((newY-getHeight())/16)-1;
				block = getWorld().level.getBlock(ix, iy);
				traits = Level.getBehavior(block.blockId);

				if (traits.contains(Block.Trait.Hurts)) getHurt();
				else if (traits.contains(Block.Trait.Kills)) die();
				
				newY = (int) ((newY - height) / 16) * 16 + height;
                jumpTime = 0;
                setYa(0);

                Marker marker = getWorld().level.getMarker(ix, iy, false);
                if (marker != null && ya < 0){
                	if (marker.type == Marker.Type.EXIT_BIGPIPE_UP ||
                			(marker.type == Marker.Type.EXIT_SMALLPIPE_UP && !large)){
                		if (keys[KEY_UP]){
	                		enteringPipeUp = true;
	                		enteringPipeTime = 1;
	                		enteringMarker = marker;
	                		calcPic();
                		}
                	}
                		
                }
            }
            if (ya > 0){
				//test for slope
            	ix = (int)(newX/16);
        		iy = (int)(newY/16)+1;
				block = getWorld().level.getBlock(ix, iy);
				traits = Level.getBehavior(block.blockId);
	
				if (traits.contains(Block.Trait.Hurts)) getHurt();
				else if (traits.contains(Block.Trait.Kills)) die();

				newY = (int) ((newY - 1) / 16 + 1) * 16 - 1 ;//+ getWorld().level.yOffsets[ix][iy];
        		boolean bouncy = traits.contains(Block.Trait.Bouncy);
        		climbing = false;
        		
        		if (buttStomping){
		            for (int i = 0; i < 4; i++)
		                getWorld().addSprite(new Sparkle((int) (newX + Math.random() * 4 - 2) + facing * 8, (int) (newY + Math.random() * 4), (float) (Math.random() * 2 - 1), (float) Math.random() * 1, 0, 1, 5));
		            for (int i = 0; i < 4; i++)
		                getWorld().addSprite(new Sparkle((int) (newX + Math.random() * 4 - 2) + facing *-8, (int) (newY + Math.random() * 4), (float) (Math.random() * 2 - 1), (float) Math.random() * 1, 0, 1, 5));

	                jumpTime = -2;
	                ya = -3;
	                
	                int ilx = (int)((getX()-getWidth())/16f);
	                int irx = (int)((getX()+getWidth())/16f);
	                int ty = iy;
	                for (int tx = ilx; tx <= irx; tx++){
						Block b = getWorld().level.getBlock(tx, ty);
						ArrayList<Block.Trait> t = Level.getBehavior(b.blockId);
	                	boolean bumpable = (t.contains(Block.Trait.Breakable) || t.contains(Block.Trait.VertBumpable));
	                	boolean thud = false;
	                	if (bumpable){
	                		getWorld().bumpDown(tx, ty, large);
	                	}
		            	else{
				        	thud = true;
				            for (int i = 0; i < 4; i++)
				                getWorld().addSprite(new Sparkle((int) (newX + Math.random() * 8 - 4), (int) (newY + Math.random() * 4), (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
		            	}

	                	if (thud){
	                		getWorld().sound.play(Art.samples[Art.SAMPLE_THUD], new FixedSoundSource(newX*16, newY*16), 1, 1, 1);
		            		onGround = true;
			                buttStomping = false;
	                	}
	                }
            	}
        		else if (bouncy && getWorld().level.getYOffsets()[ix][iy] == 0){
        			getWorld().bumpDown(ix, iy, large);
        			onGround = true;
        			buttStomping = false;
        			climbing = false;
        			calcPic();
        		}
        		else{
	                onGround = true;
	                buttStomping = false;
	                climbing = false;
        			calcPic();
            	}

        		//mark if on special tiles
       			onSlipperyGround = onGround && traits.contains(Block.Trait.Slippery);
       			onBouncyGround = onGround && traits.contains(Block.Trait.Bouncy) && traits.contains(Block.Trait.VertBumpable);

                Marker marker = getWorld().level.getMarker(ix, iy, false);
                if (marker != null){
                	if (marker.type == Marker.Type.EXIT_BIGPIPE_DOWN ||
                			(marker.type == Marker.Type.EXIT_SMALLPIPE_DOWN && !large)){
                		if (keys[KEY_DOWN]){
	                		enteringPipeDown = true;
	                		enteringPipeTime = 1;
	                		enteringMarker = marker;
	                		calcPic();
                		}
                	}
                }
            }

        	if (newX != x || newY != y){
	        	setX(newX);
	        	setY(newY);
        	}
        	
            return false;
        }
        else{
			setX(x+xa);
            setY(y+ya);
            return true;
        }
    }

	private boolean isBlocking(float _x, float _y, float xa, float ya){
        int x = (int) (_x / 16);
        int y = (int) (_y / 16);
        //if (x == (int) (getX() / 16) && y == (int) (getY() / 16)) return false;

        boolean blocking = getWorld().level.isBlocking(x, y, xa, ya, _x, _y, false);
        Block block = getWorld().level.getBlock(x, y);
        ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
        if (traits.contains(Block.Trait.Pickupable)){
            Mario.getCoin();
            getWorld().sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
            if (traits.contains(Block.Trait.Swimmable)) getWorld().level.setBlock(x, y, Level.WATER);
            else getWorld().level.setBlock(x, y, Level.DEFAULT_BLOCK);
            for (int xx = 0; xx < 2; xx++)
                for (int yy = 0; yy < 2; yy++)
                    getWorld().addSprite(new Sparkle(x * 16 + xx * 8 + (int) (Math.random() * 8), y * 16 + yy * 8 + (int) (Math.random() * 8), 0, 0, 0, 2, 5));
        }

        if (blocking && ya < 0) getWorld().bumpUp(x, y, large);
        else if (blocking && xa < 0) getWorld().bumpLeft(x, y, false); //mario can only break if from below or above
        else if (blocking && xa > 0) getWorld().bumpRight(x, y, false); //ditto

        return blocking;
    }
    
    public void stomp(Sprite enemy){
        if (deathTime > 0 || getWorld().actionPaused) return;

        float targetY = enemy.getY() - enemy.getHeight() / 2;
        move(0, targetY - getY());

        getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);
        xJumpSpeed = 0;
        yJumpSpeed = -1.9f;
        if (buttStomping) yJumpSpeed = -1.0f;
        
        jumpTime = 8;
        setYa(jumpTime * yJumpSpeed);
        onGround = false;
        wallSliding = false;
        invulnerableTime = 1;
    }

    public void stomp(Shell shell){
        if (deathTime > 0 || getWorld().actionPaused) return;
        
        float targetY = shell.getY() - shell.getHeight() / 2;
        move(0, targetY - getY());

        getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);
        if (buttStomping){
            for (int i = 0; i < 4; i++)
                getWorld().addSprite(new Sparkle((int) (shell.getX() + Math.random() * 8 - 4), (int) (shell.getY() + Math.random() * 4), (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
            shell.die();
            spriteContext.removeSprite(shell);
        }
        xJumpSpeed = 0;
        yJumpSpeed = -1.9f;
        if (buttStomping) yJumpSpeed = -1.0f;
        
        jumpTime = 8;
        setYa(jumpTime * yJumpSpeed);
        onGround = false;
        wallSliding = false;
        invulnerableTime = 1;
    }

    public void getHurt(){
        if (deathTime > 0 || getWorld().actionPaused) return;
        if (invulnerableTime > 0) return;
        if (isStarInvulnerable()) return;

        if (large){
        	buttStomping = false;
            getWorld().actionPaused = true;
            powerUpTime = -3 * 6;
            getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_POWER_DOWN], this, 1, 1, 1);
            if (fire) getWorld().mario.setLarge(true, false);
            else getWorld().mario.setLarge(false, false);
            invulnerableTime = 32;
        }
        else die();
    }

    private boolean playWinSounds = false;
    private void win(boolean playSound){
    	playWinSounds = playSound;
    	getWorld().actionPaused = true;
    	winningTime = 1;
    	for (Sprite sprite: getWorld().getSprites()){
    		if (sprite instanceof Enemy){
    			((Enemy)sprite).deadTime = 1;
    		}
    		else if (sprite instanceof Shell){
    			((Shell)sprite).die();
    		}
    	}
    	Art.stopMusic();
        if (playSound)
        	getWorld().sound.play(Art.samples[Art.SAMPLE_STAGE_WIN], this, 3, 1, 1);
    }

    private void finishWin(){
        xDeathPos = (int)getX();
        yDeathPos = (int)getY();
        winningTime = 0;
        winTime = 1;
        Art.stopMusic();
        if (playWinSounds)
        	getWorld().sound.play(Art.samples[Art.SAMPLE_LEVEL_EXIT], this, 1, 1, 1);
    }
    
    public void die(){
        xDeathPos = (int)getX();
        yDeathPos = (int)getY();
        getWorld().actionPaused = true;
        deathTime = 1;
        Art.stopMusic();
        large = false;
        fire = false;
        getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_DEATH], this, 1, 1, 1);
    }


    public void getStar(){
        if (deathTime > 0 || getWorld().actionPaused) return;

        if (starTime <= 0){
        	getWorld().actionPaused = true;
        	lastLarge = large;
        	lastFire = fire;
        	powerUpTime = 3 * 6;
            getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_POWER_UP], this, 1, 1, 1);
            getWorld().mario.setStarInvulnerable();
            Art.startMusic(Art.SONG_INVINCIBLE);
        }
        else{
            getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_POWER_UP], this, 1, 1, 1);
            getWorld().mario.setStarInvulnerable();
        }
    }

    public void getFlower(){
        if (deathTime > 0 || getWorld().actionPaused) return;

        if (!fire){
            getWorld().actionPaused = true;
            powerUpTime = 3 * 6;
            getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_POWER_UP], this, 1, 1, 1);
            getWorld().mario.setLarge(true, true);
        }
        else{
            Mario.getCoin();
            getWorld().sound.play(Art.samples[Art.SAMPLE_GET_COIN], this, 1, 1, 1);
        }
    }

    public void getMushroom(){
        if (deathTime > 0 || getWorld().actionPaused) return;

        if (!large){
            getWorld().actionPaused = true;
            powerUpTime = 3 * 6;
            getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_POWER_UP], this, 1, 1, 1);
            setLarge(true, false);
        }
    }

    public void getLifeMushroom(){
        if (deathTime > 0 || getWorld().actionPaused) return;
        get1Up();
    }

    public void kick(Shell shell){
        if (deathTime > 0 || getWorld().actionPaused) return;

        if (keys[KEY_SPEED]){
            carried = shell;
            shell.carried = true;
        }
        else{
            getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);
            invulnerableTime = 1;
        }
    }

    public void stomp(BulletBill bill){
        if (deathTime > 0 || getWorld().actionPaused) return;

        float targetY = bill.getY() - bill.getHeight() / 2;
        move(0, targetY - getY());

        getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);
        xJumpSpeed = 0;
        yJumpSpeed = -1.9f;
        if (buttStomping) yJumpSpeed = -1.0f;

        jumpTime = 8;
        setYa(jumpTime * yJumpSpeed);
        onGround = false;
        wallSliding = false;
        invulnerableTime = 1;
    }

    public short getKeyMask(){
        int mask = 0;
        for (int i = 0; i < 7; i++)
            if (keys[i]) mask |= (1 << i);
        return (short) mask;
    }

    public void setKeys(short mask){
        for (int i = 0; i < 7; i++)
            keys[i] = (mask & (1 << i)) > 0;
    }

    public static void get1Up(){
        instance.getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_1UP], instance, 1, 1, 1);
        spriteContext.addSprite(new ImageEffect((int)instance.getX(), (int)instance.getY(), 1));
        lives++;
        if (lives==99) lives = 99;
    }
    
    public static void getCoin(){
        coins++;
        if (coins==100){
            coins = 0;
            get1Up();
        }
    }
    
    public boolean fireballCollideCheck(Fireball fireball){
        float xD = fireball.getX() - getX();
        float yD = fireball.getY() - getY();
        if (xD > -16 && xD < 16){
            if (yD > -getHeight() && yD < fireball.getHeight()){
            	if (starTime == 0)
            		getHurt();
                return true;
            }
        }
        return false;
    }
    
    public boolean spriteCollideCheck(Sprite sprite){
        float xD = sprite.getX() - getX();
        float yD = sprite.getY() - getY();
        if (xD > -16 && xD < 16){
            if (yD > -getHeight() && yD < sprite.getHeight()){
            	if (starTime == 0){
            		getHurt();
            	}
                return true;
            }
        }
        return false;
    }
}