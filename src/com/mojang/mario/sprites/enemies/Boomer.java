package com.mojang.mario.sprites.enemies;

import java.awt.*;

import com.mojang.mario.*;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.*;
import com.mojang.mario.sprites.projectiles.Fireball;

public class Boomer extends Enemy {

	private int counter; 	//tick counter
	private int timer; 		//used by states to count down
	private int hitTimer;	//time to flash red
	private boolean spiked, walking, waiting, swooping, rising, dropping;	//states
	private int oix, oiy; 	//to open up win door
	
	public Boomer(LevelScene world, int x, int y, int dir){
		super(world, x, y, dir, false);
		
		sheet = Art.boomer;
		setXPic(0);
		setYPic(0);
		setWPic(32);
		setHPic(32);
		setXPicO(16);
		setYPicO(31);
		setWidth(15);
		setHeight(24);

		counter = 0;
		timer = 50;
		hitTimer = 0;
		hitPoints = 5;
		persistent = true;
		
		spiked = true;
		walking = false;
		waiting = false;
		rising = false;
		swooping = false;
		dropping = false;
		squishable = false;

        immuneToFireballs = true;

		oix = x/16;
		oiy = y/16;
	}

	private void calcPic(){
		if (spiked){
			setXPic(0);
			setYPic(0);
			
			if (timer < 5 || timer > 15){
				setXPic(1);
				setYPic(0);
			}
		}
		else if (walking){
			setXPic((counter/2)%4);
			setYPic(1);
		}
		else if (waiting){
			setXPic(2);
			setYPic(0);
		}
		else if (rising || swooping){
			setXPic((counter/2)%3);
			setYPic(2);
		}
		else if (dropping){
			setXPic(2);
			setYPic(2);
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

        hurtsWhenStomped = false;
		if (spiked){
			hurtsWhenStomped = true;
			setXa(0);
			if (!onGround) setYa(getYa()+3);
			else setYa(0);
			
			if (timer <= 0){
				spiked = false;
				walking = true;
				timer = 30;
			}
		}
		else if (walking){
			//move in direction its facing
			setXa(facing);
			if (!onGround) setYa(getYa()+3);
			else setYa(0);
			
			//randomly turn around
			if (Math.random() > 0.95) facing *= -1;
			
			if (timer <= 0){
				walking = false;
				waiting = true;
				timer = 10;
			}
		}
		else if (waiting){
			setXa(0);
			if (!onGround) setYa(getYa()+3);
			else setYa(0);
			
			if (timer <= 0){
				waiting = false;
				rising = true;
				timer = 20;
			}
		}
		else if (rising){
			setYa(-4);
			setXa(facing);
			
			if (timer <= 0){
				rising = false;
				swooping = true;
				timer = 20;
			}
		}
		else if (swooping){
			setYa(timer/2);
			setXa(facing*5);

			if (timer <= -20){
				if (Math.random() > 0.5){
					timer = 10;
				}
				else{
					swooping = false;
					dropping = true;
					timer = 10;
					facing = -facing;
				}
			}
		}
		else if (dropping){
			setXa(0);
			setYa(getYa()+3);
			
			if (onGround){
				dropping = false;
				spiked = true;
				timer = 20;
			}
		}

		calcPic();

        immuneToFireballs = spiked;

		//if can't move to the side, change facing
		if (!move(getXa(), 0)) facing *= -1;
		onGround = false;
        move(0, getYa());
    }
	
	public boolean stomped(){
		if (hitTimer == 0)
			hitPoints -= 2;
		
		if (hitPoints > 0){
			timer = 5;
			hitTimer = 10;
			spiked = true;
			walking = false;
			waiting = false;
			rising = false;
			swooping = false;
			dropping = false;
			return false;
		}
		else return true;
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
					spiked = true;
					walking = false;
					waiting = false;
					rising = false;
					swooping = false;
					dropping = false;
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
