package com.mojang.mario.sprites;

import java.util.ArrayList;

import com.mojang.mario.Art;
import com.mojang.mario.Block;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.level.Level;
import com.mojang.mario.sprites.projectiles.Fireball;
import com.mojang.sonar.FixedSoundSource;

public class Shell extends Sprite {

	private static float GROUND_INERTIA = 0.89f;
    private static float AIR_INERTIA = 0.95f;

    private boolean onGround = false;
    public boolean avoidCliffs = false;
    public boolean dead = false;
    public boolean carried;

    public int facing, type;
    public int anim;
    private int deadTime = 0, aliveTime = 0;
    private int xPicBase = 0;
    public int movedSinceRelease = Integer.MAX_VALUE;

    private float runTime;

    public Shell(LevelScene world, float x, float y, int type){
    	super(world);
        setSheet(Art.enemies);

        setX(x);
        setY(y);
        setXPicO(8);
        setYPicO(31);

        this.type = type;
        switch(type){
        	case 0: //red turtle shell
        		setYPic(0);
        		xPicBase = 3;
        		break;
        	case 1: //green turtle shell
        		setYPic(1);
        		xPicBase = 3;
        		break;
        	case 2: //beetle shell
        		setYPic(3);
        		xPicBase = 8;
        		break;
        	case 3: //spiny shell
        		setYPic(3);
        		xPicBase = 2;
        		break;
        	case 4: //bob-omb
        		setYPic(5);
        		xPicBase = 5;
        		break;
        }
        setHeight(12);
        facing = 0;
        setWPic(16);
        setYa(-5);
    }
    
    public boolean fireballCollideCheck(Fireball fireball){
        if (deadTime != 0) return false;

        float xD = fireball.getX() - getX();
        float yD = fireball.getY() - getY();

        if (xD > -16 && xD < 16){
            if (yD > -getHeight() && yD < fireball.getHeight()){
                if (type == 2 || type == 4) return true; //fireproof
                
                getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);

                setXa(fireball.facing * 2);
                setYa(-5);
                if (getSpriteTemplate() != null) getSpriteTemplate().isDead = true;
                deadTime = 100;
                setHPic(-getHPic());
                setYPicO(-getYPicO() + 16);
                return true;
            }
        }
        return false;
    }    

    public void collideCheck(){
        if (carried || dead || deadTime>0) return;

        float xMarioD = getWorld().mario.getX() - getX();
        float yMarioD = getWorld().mario.getY() - getY();
        //float w = 16;
        if (xMarioD > -16 && xMarioD < 16){
            if (yMarioD > -getHeight() && yMarioD < getWorld().mario.getHeight()){

            	//if mario's invulnerable, shell dies
    			if (getWorld().mario.isStarInvulnerable()){
	                getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);
	                setXa(getWorld().mario.facing * 2);
	                setYa(-5);
	                if (getSpriteTemplate() != null) getSpriteTemplate().isDead = true;
	                deadTime = 100;
	                setHPic(-getHPic());
	                setYPicO(-getYPicO() + 16);
    			}

            	//if mario is coming down on top of shell...
    			else if (getWorld().mario.getYa() > 0 && yMarioD <= 0 && (!getWorld().mario.onGround || !getWorld().mario.wasOnGround)){
                    getWorld().mario.stomp(this);
                   
                    if (facing != 0){ //if shell is moving already, jumping on it stops it
                        setXa(0);
                        facing = 0;
                    }
                    else{ //otherwise it starts it moving
			        	if (getWorld().mario.getX()+getWorld().mario.getWidth()/2 > getX()+getWidth()/2) facing = -1;
			        	else facing = 1;
                        movedSinceRelease = 0;
                        carried = false;
                    }
                }
                //else if he otherwise has run into it
                else{
                    if (facing != 0){ //if it's already moving...
                    	if (movedSinceRelease > 48){ //...and grace period before shell hits you runs out...
                    		if (getWorld().mario.isInvulnerableFlash()); //do nothing
                    		else if (type == 4) getWorld().mario.kick(this); //if a bob-omb
                    		else getWorld().mario.getHurt(); //get hurt
                    	}
                    }
                    else{
                        getWorld().mario.kick(this); //otherwise kick the shell in the direction mario faces
                        if (!carried){ //mario could have picked up in 'kick' above
	                        facing = getWorld().mario.facing;
	                        movedSinceRelease = 0;
	                        carried = false;
                        }
                    }
                }
            }
        }
    }

    public void move(){
    	aliveTime++;
    	
    	if (type == 4){ //if a bobomb
        	setXPic(xPicBase+(aliveTime%2));
	    	
        	if (aliveTime > 150){
	    		setXPic(7+aliveTime%4);
	    	}
        	
        	if (aliveTime > 200){
                if (getSpriteTemplate() != null) getSpriteTemplate().isDead = true;
                deadTime = 0;

                for (int i = 0; i < 32; i++)
                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 32 - 16) + 4, (int) (getY() - Math.random() * 32) + 4, (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
                
                for (Sprite sprite: getWorld().getSprites()){
                	if (Math.abs(sprite.getX()-getX()) < 32 && Math.abs(sprite.getY()-getY()) < 32){
                		if (sprite == getWorld().mario){
                			((Mario)sprite).getHurt();
                		}
                		else if (sprite instanceof Enemy){
                			Enemy enemy = (Enemy)sprite;
                			int facingAway = (int)Math.signum(getX()-enemy.getX());
                			enemy.facing = facingAway;
                			enemy.setXa(facingAway * 2);
			                enemy.setYa(-5);
			                enemy.flyDeath = true;
			                if (getSpriteTemplate() != null) enemy.getSpriteTemplate().isDead = true;
			                enemy.deadTime = 100;
			                enemy.winged = false;
			                enemy.setHPic(-getHPic());
			                enemy.setYPicO(-getYPicO() + 16);
                		}
                		else if (sprite instanceof Shell){
                			Shell shell = (Shell)sprite;
                			if (shell.type == 4) aliveTime = 100; //set off other bob-ombs
                			else shell.deadTime = 1; //so it dies next tick
                		}
                	}
                }
                
                getWorld().sound.play(Art.samples[Art.SAMPLE_BOBOMB_EXPLODE], this, 1, 1, 1);
                spriteContext.addSprite(new ImageEffect((int)getX(), (int)getY(), 0));
                spriteContext.removeSprite(this);
        	}
        }
    	
        if (carried){
            getWorld().checkShellCollide(this);
            return;
        }

        if (deadTime > 0){
            deadTime--;

            if (deadTime == 0){
                deadTime = 1;
                for (int i = 0; i < 8; i++)
                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int)(getY() - Math.random() * 8) + 4, (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
                spriteContext.removeSprite(this);
            }

            setX(getX()+getXa());
            setY(getY()+getYa());
            setYa((getYa()*0.95f)+1);

            return;
        }

        if (facing != 0) anim++;

        if (type != 4){
	        float sideWaysSpeed = 8f; //was 11f
	        //        float sideWaysSpeed = onGround ? 2.5f : 1.2f;
	
	        if (getXa() > 2) facing = 1;
	        if (getXa() < -2) facing = -1;
	
	        setXa(facing * sideWaysSpeed);
        }
        else if (type == 4 && movedSinceRelease == 0){
	        float sideWaysSpeed = 12f; //was 11f
	    	if (Math.abs(getWorld().mario.getXa()) < 2){
	    		sideWaysSpeed = 4;
	    	}
	
	        if (getXa() > 2) facing = 1;
	        if (getXa() < -2) facing = -1;
	
	        setXa(facing * sideWaysSpeed);
        }
        
        if (Math.abs(getXa()) < 2){
        	facing = 0;
        	movedSinceRelease = 0;
        }
        if (facing != 0) getWorld().checkShellCollide(this);

        setXFlipPic(facing == -1);
        runTime += (Math.abs(getXa())) + 5;

        if (type != 4){
        	setXPic(xPicBase+(anim / 2) % 4);
        }
        
        movedSinceRelease+=Math.abs(getXa());
        
        if (!move(getXa(), 0)){
            getWorld().sound.play(Art.samples[Art.SAMPLE_SHELL_BUMP], this, 1, 1, 1);
            facing = -facing;
            setXa(-getXa());
        }
        boolean wasOnGround = onGround;
        onGround = false;
        move(0, getYa());

        setYa(getYa()*0.85f);
        if (onGround) setXa(getXa()*GROUND_INERTIA);
        else setXa(getXa()*AIR_INERTIA);

        //bounce
        if (type == 4 && !wasOnGround && onGround){
        	if (Math.abs(getYa()) < 2f) setYa(0);
        	else setYa((int)(-getYa()*0.3));
        }
        
        if (!onGround) setYa(getYa()+3);
        
        if (type == 4) setXa((int)(getXa()*0.90));
        
    }

    protected boolean move(float xa, float ya){
    	float step = 4f;
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
        if (ya > 0){
	        if (collide || isBlocking(x + xa - width, y + ya, xa, 0)) collide = true;
            else if (collide || isBlocking(x + xa + width, y + ya, xa, 0)) collide = true;
            else if (collide || isBlocking(x + xa - width, y + ya + 1, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa + width, y + ya + 1, xa, ya)) collide = true;

    		int ix = (int)(x/16);
    		int iy = (int)(y/16)+1;
			Block block = getWorld().level.getBlock(ix, iy);
			ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
			if (traits.contains(Block.Trait.Kills)) deadTime = 1;
        }
        if (ya < 0){
            if (collide || isBlocking(x + xa, y + ya - height, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;

    		int ix = (int)(x/16);
    		int iy = (int)(y/16)-1;
			Block block = getWorld().level.getBlock(ix, iy);
			ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
			if (traits.contains(Block.Trait.Kills)) deadTime = 1;
        }
        if (xa > 0){
            if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
            if (collide || isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) collide = true;
            if (collide || isBlocking(x + xa + width, y + ya, xa, ya)) collide = true;

    		int ix = (int)(x/16)+1;
    		int iy = (int)(y/16);
			Block block = getWorld().level.getBlock(ix, iy);
			ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
			if (traits.contains(Block.Trait.Kills)) deadTime = 1;
        }
        if (xa < 0){
            if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
            if (collide || isBlocking(x + xa - width, y + ya - height / 2, xa, ya)) collide = true;
            if (collide || isBlocking(x + xa - width, y + ya, xa, ya)) collide = true;

    		int ix = (int)(x/16)-1;
    		int iy = (int)(y/16);
			Block block = getWorld().level.getBlock(ix, iy);
			ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
			if (traits.contains(Block.Trait.Kills)) deadTime = 1;
        }
		
        if (collide){
        	float newX = x, newY = y;
        	
        	if (xa < 0){
                newX = (int) ((newX - width) / 16) * 16 + width;
                setXa(0);
            }
            if (xa > 0){
	        	newX = (int) ((newX + width) / 16 + 1) * 16 - width - 1;
                setXa(0);
            }
            if (ya < 0){
				newY = (int) ((newY - height) / 16) * 16 + height;
                setYa(0);
            }
            if (ya > 0){
				newY = (int) ((newY - 1) / 16 + 1) * 16 - 1 ;//+ getWorld().level.yOffsets[ix][iy];
      			onGround = true;
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
        if (x == (int) (getX() / 16) && y == (int) (getY() / 16)) return false;

        boolean blocking = getWorld().level.isBlocking(x, y, xa, ya, _x, _y);
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

        if (type != 4){ //bobombs don't bump bricks at all
        	boolean canBreakBricks = (movedSinceRelease != Integer.MAX_VALUE); //if kicked or released, not just bouncing
        	if (blocking && xa != 0) getWorld().bumpUp(x, y, canBreakBricks); //shells only bump things upward and only horizontally
        }
        
        return blocking;
    }

	public void bumpCheck(int xTile, int yTile){
        if (getX() + getWidth() > xTile * 16 && getX() - getWidth() < xTile * 16 + 16 && yTile == (int) ((getY() - 1) / 16)){
        	if (facing == 0);
        	else if (getWorld().mario.getX()+getWorld().mario.getWidth()/2 > getX()+getWidth()/2) facing = -1;
        	else facing = 1;
	        
        	if (type == 4) setXa(facing*12);
	        else setXa(facing * 8);
            
        	setYa(-8);
        }
    }

    public void die(){
    	dead = true;
        carried = false;
        setXa(-facing*2);
        setYa(-5);
        deadTime = 100;
    }

    public boolean shellCollideCheck(Shell shell){
        if (deadTime != 0) return false;

        float xD = shell.getX() - getX();
        float yD = shell.getY() - getY();

        if (xD > -16 && xD < 16){
            if (yD > -getHeight() && yD < shell.getHeight()){
                getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);

                if (getWorld().mario.carried == shell || getWorld().mario.carried == this) getWorld().mario.carried = null;

                die();
                if (facing != 0) shell.die();
                return true;
            }
        }
        return false;
    }


    public void release(Mario mario){
        carried = false;
        facing = mario.facing;
        movedSinceRelease = 0;
    }
}