package com.mojang.mario.sprites.enemies;

import java.awt.*;
import java.util.ArrayList;

import com.mojang.mario.*;
import com.mojang.mario.level.Level;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.*;
import com.mojang.mario.sprites.projectiles.FireBurst;
import com.mojang.mario.sprites.projectiles.Fireball;
import com.mojang.sonar.FixedSoundSource;

public class Bowser extends Enemy {

	private int hitPoints; 	//starts with 10, fireball = 1hp
	private int counter; 	//tick counter
	private int timer; 		//used by states to count down
	private int hitTimer;	//time to flash red
	private boolean waiting, turning, preshooting, shooting, postshooting, jumping, pausing, dropping;	//states
	private int oix, oiy;
	
	public Bowser(LevelScene world, int x, int y, int dir){
		super(world, x, y, dir, false);
		
		sheet = Art.bowser;
		setXPic(0);
		setYPic(0);
		setWPic(32);
		setHPic(48);
		setXPicO(16);
		setYPicO(47);
		setWidth(15);
		setHeight(40);

		counter = 0;
		timer = 60;
		hitTimer = 0;
		hitPoints = 15;
		persistent = true;
		hurtsWhenStomped = true;
		
		waiting = true;
		turning = false;
		preshooting = false;
		shooting = false;
		postshooting = false;
		jumping = false;
		pausing = false;
		dropping = false;
		squishable = false;
		
		oix = x/16;
		oiy = y/16;
	}

	private void calcPic(){
		if (waiting){
			int xp = (counter/10)%4;
			if (xp == 2) xp = 0;
			if (xp == 3) xp = 2;
			setXPic(2+xp);
		}
		else if (turning){
			int xp = 4;
			if (timer < 5) xp = 5;
			setXPic(xp);
		}
		else if (shooting){
			int xp = 6;
			if (timer < 17) xp = 7;
			if (timer < 14) xp = 8;
			if (timer < 11) xp = 9;
			if (timer < 5) xp = 8;
			if (timer < 3) xp = 6;
			setXPic(xp);
		}
		else if (preshooting || postshooting){
			setXPic(6);
		}
		else if (jumping){
			setXPic(1);
		}
		else if (pausing){
			setXPic(1);
		}
		else if (dropping){
			setXPic(0);
		}
		
		setXFlipPic(facing < 0);
	}
	
    public void move(){
		counter++;
    	timer--;
    	if (hitTimer > 0) hitTimer--;
    	
        if (deadTime > 0){
            deadTime--;

            if (deadTime == 0){
                deadTime = 1;
                for (int i = 0; i < 8; i++)
                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int) (getY() - Math.random() * 8) + 4, (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
                spriteContext.removeSprite(this);
                
                //open up win door
                getWorld().createWinDoor(oix, oiy);

                //play music
                Art.stopMusic();
                getWorld().sound.play(Art.samples[Art.SAMPLE_WIN_DOOR], this, 3, 1, 1);
            }

            if (flyDeath){
                setX(getX()+getXa());
                setY(getY()+getYa());
                setYa((getYa() * 0.95f)+1);
            }
            
            setLayer(1);
            return;
        }

		if (waiting){
			setXa(0);
			if (!onGround) setYa(getYa()+3);
			else setYa(0);
			//start turning
			if (timer <= 0){
				//change facing to face Mario
				int xMarioD = (int)(getWorld().mario.getX()-getX());
				facing = xMarioD > 0 ? 1 : -1;

				waiting = false;
				turning = true;
				timer = 10;
			}
		}
		else if (turning){
			setXa(0);
			if (!onGround) setYa(getYa()+3);
			
			if (timer <= 0){
				turning = false;
				preshooting = true;
				timer = 10;
			}
		}
		else if (preshooting){
			setXa(0);
			if (!onGround) setYa(getYa()+3);

			if (timer <= 0){
				preshooting = false;
				shooting = true;
				timer = 10;
			}
		}
		else if (shooting){
			setXa(0);
			if (!onGround) setYa(getYa()+3);
			else setYa(0);

			if (timer == 4 && hitPoints <= 10) getWorld().addSprite(new FireBurst(getWorld(), getX()+facing*12, getY()-30, facing, this, 1));
			if (timer == 2 && hitPoints <= 5) getWorld().addSprite(new FireBurst(getWorld(), getX()+facing*12, getY()-30, facing, this, 0));
			if (timer <= 0){
				getWorld().addSprite(new FireBurst(getWorld(), getX()+facing*12, getY()-30, facing, this, -1));

            	if (Math.random() > 0.8){
					timer = 10;
				}
				else{
					shooting = false;
					postshooting = true;
					timer = 10;
				}
			}
		}
		else if (postshooting){
			setXa(0);
			if (!onGround) setYa(getYa()+3);

			if (timer <= 0){
				postshooting = false;
				jumping = true;
				timer = 20;
				setXa(0);
				setYa(-14);
			}
		}
		else if (jumping){
			setXa(getXa()+facing);
			setYa(getYa()+0.5f);

			//end jump when over Mario
			int xMarioD = (int)(getWorld().mario.getX()-getX());
			if (Math.abs(xMarioD) < 8){
				jumping = false;
				pausing = true;
				timer = 5;
			}
		}
		else if (pausing){
			setXa(0);
			setYa(0);

			if (timer <= 0){
				pausing = false;
				dropping = true;
				timer = 10;
			}
		}
		else if (dropping){
			setXa(0);
			setYa(getYa()+3);
			
			if (onGround){
				dropping = false;
				waiting = true;
				timer = 60;
				crush();
				
	        	getWorld().sound.play(Art.samples[Art.SAMPLE_THUD], this, 3, 1, 1);
	            for (int i = 0; i < 24; i++)
	                getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 32), (int) (getY() + Math.random() * 8-4)+2, (float) Math.random() * 2 - 1-facing, (float) Math.random() *2 -1, 0, 1, 5));
	            for (int i = 0; i < 24; i++)
	                getWorld().addSprite(new Sparkle((int) (getX() - Math.random() * 32), (int) (getY() + Math.random() * 8-4)+2, (float) Math.random() * 2 - 1-facing, (float) Math.random() *2 -1, 0, 1, 5));
			}
		}

		calcPic();
		
		//if can't move to the side, change facing
		boolean couldMoveX = move(getXa(), 0);
		onGround = false;
        boolean couldMoveY = move(0, getYa());

        //end jump if couldn't move and drop straight down
        if (jumping && (!couldMoveX || !couldMoveY)){
			jumping = false;
			pausing = true;
			timer = 5;
			setXa(0);
			setYa(0);
        }
        
        //if fell off bottom of screen, die
        if (getY() > getWorld().level.getHeight()*16+16){
            getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);
            if (getSpriteTemplate() != null) getSpriteTemplate().isDead = true;
            deadTime = 1;
        }
    }
	
    private void crush(){
    	int ilx = (int)((getX()-getWidth())/16f);
    	int irx = (int)((getX()+getWidth())/16f);
    	int iy = (int)((getY()+2)/16f);
    	for (int x = ilx; x <= irx; x++){
            int blockId = getWorld().level.getBlock(x, iy).blockId;
            ArrayList<Block.Trait> traits = Level.getBehavior(blockId);
    		
            if (!traits.contains(Block.Trait.Breakable)) continue;
    				
    		//break block - cloned from LevelScene
            getWorld().sound.play(Art.samples[Art.SAMPLE_BREAK_BLOCK], new FixedSoundSource(x * 16 + 8, iy * 16 + 8), 1, 1, 1);

            //figure out which variety of particle this should create
            int type = 0;
            if (blockId >= Level.ORANGE_BRICK && blockId < Level.ORANGE_BRICK+16) type = 0;
            else if (blockId >= Level.RED_BRICK && blockId < Level.RED_BRICK+16) type = 1;
            else if (blockId >= Level.BLUE_BRICK && blockId < Level.BLUE_BRICK+16) type = 2;
            
            getWorld().level.setBlock(x, iy, Level.DEFAULT_BLOCK);
            for (int xx = 0; xx < 2; xx++)
                for (int yy = 0; yy < 2; yy++)
                    getWorld().addSprite(new Particle(x * 16 + xx * 8 + 4, iy * 16 + yy * 8 + 4, (xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8, type));

            for (int i = 0; i < 24; i++)
                getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 32), (int) (getY() + Math.random() * 8-4)+2, (float) Math.random() * 2 - 1-facing, (float) Math.random() *2 -1, 0, 1, 5));
            for (int i = 0; i < 24; i++)
                getWorld().addSprite(new Sparkle((int) (getX() - Math.random() * 32), (int) (getY() + Math.random() * 8-4)+2, (float) Math.random() * 2 - 1-facing, (float) Math.random() *2 -1, 0, 1, 5));
    	}
    	
		getWorld().sound.play(Art.samples[Art.SAMPLE_THUD], new FixedSoundSource(getX(), getY()), 1, 1, 1);
        for (int i = 0; i < 24; i++)
            getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int)getY(), (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
    }
    
    public boolean fireballCollideCheck(Fireball fireball){
        if (deadTime != 0) return false;
        if (fireball.getSource() != getWorld().mario) return false; //enemy fireballs can only hit Mario

        float xD = fireball.getX() - getX();
        float yD = fireball.getY() - getY();

        if (xD > -16 && xD < 16){
            if (yD > -getHeight() && yD < fireball.getHeight()){
                if (immuneToFireballs || hitTimer > 0) return true;
                
                hitPoints -= 1;
                
                if (hitPoints > 0){
                	getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_STOMP], this, 1, 1, 1);
					timer = 30;
					hitTimer = 10;
                }
                else{
	                getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);
	                setXa(fireball.facing * 2);
	                setYa(-5);
	                flyDeath = true;
	                if (getSpriteTemplate() != null) getSpriteTemplate().isDead = true;
	                deadTime = 100;
	                winged = false;
	                setHPic(-getHPic());
	                setYPicO(-getYPicO() + 16);
                }

                return true;
            }
        }
        return false;
    }
    
    public void render(Graphics og, float alpha){

        //image location plus sprite offset
        int xPixel = (int)(getXOld()+(getX()-getXOld())*alpha)-getXPicO();
        int yPixel = (int)(getYOld()+(getY()-getYOld())*alpha)-getYPicO();
        
        //figure offsets due to tiles
        if (Mario.instance != null){
        	int ix = (int)(getX()/16), iy = (int)(getY()/16)+1;

        	if (ix >= 0 && ix < Mario.instance.getWorld().level.getWidth() &&
        			iy >= 0 && iy < Mario.instance.getWorld().level.getHeight()){
	        	int xo = Mario.instance.getWorld().level.getXOffsets()[ix][iy];
	        	int yo = Mario.instance.getWorld().level.getYOffsets()[ix][iy];
	
	        	if (xo != 0){
		        	if (xo > 0) xo = (int) (Math.cos((xo - alpha) / 4.0f * Math.PI) * 8);
		        	else if (xo < 0) xo = -(int)(Math.cos((-xo - alpha) / 4.0f * Math.PI) * 8);
		        	xPixel -= xo;
	        	}
	        	if (yo != 0){
		        	if (yo > 0) yo = (int) (Math.sin((yo - alpha) / 4.0f * Math.PI) * 8);
		        	else if (yo < 0) yo = -(int)(Math.sin((-yo - alpha) / 4.0f * Math.PI) * 8);
		        	yPixel -= yo;
	        	}
        	}
        }

        boolean xFlipPic = isXFlipPic(), yFlipPic = isYFlipPic();
        int wPic = getWPic(), hPic = getHPic();
       	Image image = sheet[getXPic()][getYPic()];
        if (hitTimer > 0){
        	image = tintImage(sheet[getXPic()][getYPic()], Color.RED.darker().darker(), null);
        }
    	if (alphaTransparency != 1){
    		image = fadeImage(image, alphaTransparency, null);
    	}
       	og.drawImage(image, xPixel+(xFlipPic?wPic:0), yPixel+(yFlipPic?hPic:0), xFlipPic?-wPic:wPic, yFlipPic?-hPic:hPic, null);
    }
}
