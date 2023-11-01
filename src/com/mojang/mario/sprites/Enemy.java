package com.mojang.mario.sprites;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import com.mojang.mario.Art;
import com.mojang.mario.Block;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.level.Level;
import com.mojang.mario.sprites.enemies.*;
import com.mojang.mario.sprites.projectiles.Fireball;

public class Enemy extends Sprite {
	
	public enum Kind {
        Goomba, RedGoomba, //YellowGoomba, ArmoredGoomba, MiniGoomba, BigGoomba
        Koopa, RedKoopa, WingedKoopa, WingedRedKoopa,
        //BlueKoopa, YellowKoopa, WingedBlueKoopa, WingedYellowKoopa
        DryBones, //DrySpike, WindupKoopa
        BulletBill, //SeekerBullet, MissileBullet, BigBullet
        HammerBro, BoomerangBro, FireBro, //QuakeBro, StompBro
        Spike, Buzzy,
        Lakitu, Spiny, SpinyEgg, //EggThrower, QueenSpiny
		PirahnaPlant, FirePlant, JumpPlant, //BlowPlant, WalkingBlowPlant
        SnapSprout, //FireSprout, FlamethrowerSprout
		BobOmb, //MomBomb
        DasherCheep, SeekerCheep, //JumperCheep, BigCheep, BoneFish
        Boo, //PinkBoo, FadeBoo, BarBoo, CircularBoo, KingBoo
        Pobodo, Thwomp, //BigThwomp
        //MagiKoopa
        Boomer, Bowser, //BowserJr, the Koopa Kids
        //BillyBat, ScareCrow
        //MontyMole, BigMole, WrenchThrower
        //Rhino, BigRhino, ReznorRhino
        //Ninji, Shyguy, <whatever the flying pitchfork things were>
        //Birdo, RedBirdo, GreenBirdo
    }
	
	public static Enemy create(Kind kind, LevelScene world, int x, int y, int dir){
		Enemy ret;
		switch(kind){
			case Koopa:
                ret = new Koopa(world, x, y, dir);
                break;
			case RedKoopa:
                ret = new RedKoopa(world, x, y, dir);
                break;
			case Buzzy:
                ret = new Buzzy(world, x, y, dir);
                break;
			case Spiny:
                ret = new Spiny(world, x, y, dir);
                break;
			case BobOmb:
				ret = new BobOmb(world, x, y, dir);
				break;
			case WingedKoopa:
                ret = new WingedKoopa(world, x, y, dir);
                break;
			case WingedRedKoopa:
				ret = new WingedRedKoopa(world, x, y, dir);
				break;
			case Goomba:
                ret = new Goomba(world, x, y, dir);
                break;
			case RedGoomba:
                ret = new RedGoomba(world, x, y, dir);
                break;
			case Spike:
                ret = new Spike(world, x, y, dir);
                break;
			case SpinyEgg:
				ret = new SpinyEgg(world, x, y, dir);
				break;
			case JumpPlant:
                ret = new JumpPlant(world, x, y);
                break;
			case Pobodo:
                ret = new Pobodo(world, x, y);
                break;
			case SnapSprout:
				ret = new SnapSprout(world, x, y);
				break;
			case PirahnaPlant:
                ret = new PirahnaPlant(world, x, y);
                break;
			case FirePlant:
				ret = new FirePlant(world, x, y);
				break;
			case DryBones:
				ret = new DryBones(world, x, y, dir);
				break;
			case Lakitu:
				ret = new Lakitu(world, x, y, dir);
				break;
			case HammerBro:
                ret = new Bro(world, x, y, dir, 0);
                break;
			case BoomerangBro:
                ret = new Bro(world, x, y, dir, 1);
                break;
			case FireBro:
				ret = new Bro(world, x, y, dir, 2);
				break;
			case DasherCheep:
                ret = new CheepCheep(world, x, y, dir, 0);
                break;
			case SeekerCheep:
				ret = new CheepCheep(world, x, y, dir, 1);
				break;
			case Boo:
				ret = new Boo(world, x, y, dir);
				break;
			case Thwomp:
				ret = new Thwomp(world, x, y, dir);
				break;
			case Boomer:
				ret = new Boomer(world, x, y, dir);
				break;
			case Bowser:
				ret = new Bowser(world, x, y, dir);
				break;
            case BulletBill:
                ret = new BulletBill(world, x, y, dir);
                break;
			default: 
				throw new IllegalArgumentException("No case for kind "+kind.name());
		}
		return ret;
	}

    protected static float GROUND_INERTIA = 0.89f;
    protected static float AIR_INERTIA = 0.89f;

    protected float runTime;
    protected boolean onGround = false, bumpedCeiling  = false, inWater = false;

    protected int hitPoints;
    protected int wingTime = 0;
    public int deadTime = 0;
    public int facing;
    public boolean flyDeath = false;
    public boolean winged = true, doubleWinged = true;
    protected boolean hurtsWhenStomped = false, tangible = true;
    private int xPicBase;
    protected boolean squishable, immuneToFireballs, avoidsCliffs;
    protected boolean canBlock, knockable, bumpable, swims;
    protected int shellDropType;
    
    protected Enemy(LevelScene world, int x, int y, int dir, boolean winged){
    	super(world);
        setX(x);
        setY(y);
        facing = dir;
        this.winged = winged;

        if (facing == 0) facing = 1;
        setSheet(Art.enemies);
        setXPicO(8);
        setYPicO(31);

        setWPic(16);
        setHPic(32);
        setXPicBase(0);
        setXPic(getXPicBase());
        setYPic(0);
		setWidth(7);
		setHeight(24);

        hitPoints = 1;
        canBlock = true;  //terrain blocks this enemy (doesn't pass thru)
        knockable = true; //this enemy can be knocked from the side
        bumpable = true;  //this enemy can be bumped from beneath
        swims = false;    //this enemy is not limited only to water
        shellDropType = -1; //this enemy doesn't leave a shell behind
    }

	public void collideCheck(){
        if (deadTime != 0) return;
        if (!tangible) return;
        
        boolean collided = false;
        
        float yMarioD = getWorld().mario.getY() - getY();

        Rectangle myRect = new Rectangle((int)(getX()-getWidth()), (int)(getY()-getHeight()), getWidth()*2, getHeight()); 
        Rectangle marioRect = new Rectangle((int)(getWorld().mario.getX()-getWorld().mario.getWidth()), 
        		(int)(getWorld().mario.getY()-getWorld().mario.getHeight()), 
        		getWorld().mario.getWidth()*2, getWorld().mario.getHeight());
        if (myRect.intersects(marioRect))
        	collided = true;
        
        if (collided){
        	if (getWorld().mario.isStarInvulnerable()){
                setXa(getWorld().mario.facing * 2);
                setYa(-5);
                flyDeath = true;
                if (getSpriteTemplate() != null) getSpriteTemplate().isDead = true;
                deadTime = 100;
                winged = false;
                setHPic(-getHPic());
                setYPicO(-getYPicO() + 16);
            	getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);
        	}
        	else if (!hurtsWhenStomped && getWorld().mario.getYa() > 0 && yMarioD <= 0 && 
        			(!getWorld().mario.onGround || !getWorld().mario.wasOnGround) && !getWorld().mario.inWater){
                getWorld().mario.stomp(this);
                if (winged){
                    winged = false;
                    setYa(0);
                }
                else{
                	if (stomped()){
                        //if (hitPoints > 0) return; //CHANGE

                		if (squishable){
                    		setYPicO(31 - (32 - 8));
	                        setHPic(8);
                		}
                        if (getSpriteTemplate() != null) getSpriteTemplate().isDead = true;
                        deadTime = 10;
                        winged = false;
                        getHurt();
                	}
                }
            }
        	else getWorld().mario.getHurt();
        }
        
        //bounce if you hit another enemy
        for (Sprite sprite: getWorld().getSprites()){
        	if (sprite != this && (sprite instanceof Enemy || sprite instanceof Shell)){
		        float xSpriteD = sprite.getX() - getX();
		        float ySpriteD = sprite.getY() - getY();
		        int width = getWidth();
		        int height = getHeight();
		        if (xSpriteD > -width-4 && xSpriteD < width+4){
		            if (ySpriteD > -height && ySpriteD < getWorld().mario.getHeight()){
		            	collidedWithSprite(sprite);
		            }
		        }
        	}
        }
    }

	//usually just turn the other way...
	protected void collidedWithSprite(Sprite sprite){
		facing = -facing;
	}
	
	public boolean stomped(){ return true; }
	
    public int getXPicBase(){ return xPicBase; }
	public void setXPicBase(int picBase){ xPicBase = picBase; }

	//returns any sprite created by the death (like a shell or item)
    public Sprite getHurt(){
        if (shellDropType > -1){
            Shell shell = new Shell(getWorld(), getX(), getY(), shellDropType);
            spriteContext.addSprite(shell);
            return shell;
        }
        return null;
    }
    
    public void move(){
        wingTime++;
        if (deadTime > 0){
            deadTime--;

            //if a shelled enemy, just go poof (and leave a shell) instead of showing squished
            if (!flyDeath && shellDropType > -1){
                deadTime = 0;
            }
            
            if (deadTime == 0){
                deadTime = 1;
                for (int i = 0; i < 8; i++)
                    getWorld().addSprite(new Sparkle((int) (getX() + Math.random() * 16 - 8) + 4, (int) (getY() - Math.random() * 8) + 4, (float) (Math.random() * 2 - 1), (float) Math.random() * -1, 0, 1, 5));
                spriteContext.removeSprite(this);
            }

            if (flyDeath){
                setX(getX()+getXa());
                setY(getY()+getYa());
                setYa((getYa() * 0.95f)+1);
            }
            
            setLayer(1);
            return;
        }


        float sideWaysSpeed = 1.2f;
        //float sideWaysSpeed = onGround ? 2.5f : 1.2f;

        if (getXa() > 2) facing = 1;
        if (getXa() < -2) facing = -1;

        setXa(facing * sideWaysSpeed);

        setXFlipPic(facing == -1);

        runTime += (Math.abs(getXa())) + 5;

        int runFrame = ((int) (runTime / 20)) % 2;

        if (!onGround) runFrame = 1;

        if (!move(getXa(), 0)) facing = -facing;
        onGround = false;
        bumpedCeiling = false;
        move(0, getYa());

        setYa(getYa() * (winged ? 0.95f : 0.85f));
        if (onGround) setXa(getXa() * GROUND_INERTIA);
        else setXa(getXa() * AIR_INERTIA);

        if (!onGround){
            if (winged) setYa(getYa() + 0.6f);
            else setYa(getYa() + 2);
        }
        else if (winged) setYa(-10);

        if (winged) runFrame = wingTime / 4 % 2;

        setXPic(getXPicBase()+runFrame);

    }

    protected boolean move(float xa, float ya){
        while (xa > 8){
            if (!move(8, 0)) return false;
            xa -= 8;
        }
        while (xa < -8){
            if (!move(-8, 0)) return false;
            xa += 8;
        }
        while (ya > 8){
            if (!move(0, 8)) return false;
            ya -= 8;
        }
        while (ya < -8){
            if (!move(0, -8)) return false;
            ya += 8;
        }

        boolean collide = false;
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        if (ya > 0){
            if (isBlocking(x + xa - width, y + ya, xa, 0)) collide = true;
            else if (isBlocking(x + xa + width, y + ya, xa, 0)) collide = true;
            else if (isBlocking(x + xa - width, y + ya + 1, xa, ya)) collide = true;
            else if (isBlocking(x + xa + width, y + ya + 1, xa, ya)) collide = true;

    		int ix = (int)(x/16);
    		int iy = (int)(y/16)+1;
			Block block = getWorld().level.getBlock(ix, iy);
			ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
			if (traits.contains(Block.Trait.Kills)){
				deadTime = 1;
			}
        }
        if (ya < 0){
            if (isBlocking(x + xa, y + ya - height, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
            else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;

    		int ix = (int)(x/16);
    		int iy = (int)(y/16)-1;
			Block block = getWorld().level.getBlock(ix, iy);
			ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
			if (traits.contains(Block.Trait.Kills)){
				deadTime = 1;
			}
        }
        if (xa > 0){
            if (isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
            if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) collide = true;
            if (isBlocking(x + xa + width, y + ya, xa, ya)) collide = true;

            if (avoidsCliffs && onGround && !getWorld().level.isBlocking((int) ((x + xa + width) / 16), (int) ((y) / 16 + 1), xa, 1, x + xa + width, y+1)) collide = true;

    		int ix = (int)(x/16)+1;
    		int iy = (int)(y/16);
			Block block = getWorld().level.getBlock(ix, iy);
			ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
			if (traits.contains(Block.Trait.Kills)){
				deadTime = 1;
			}
        }
        if (xa < 0){
            if (isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
            if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya)) collide = true;
            if (isBlocking(x + xa - width, y + ya, xa, ya)) collide = true;

            if (avoidsCliffs && onGround && !getWorld().level.isBlocking((int) ((x + xa - width) / 16), (int) ((y) / 16 + 1), xa, 1, x + xa - width, y+1)) collide = true;

    		int ix = (int)(x/16)-1;
    		int iy = (int)(y/16);
			Block block = getWorld().level.getBlock(ix, iy);
			ArrayList<Block.Trait> traits = Level.getBehavior(block.blockId);
			if (traits.contains(Block.Trait.Kills)){
				deadTime = 1;
			}
        }

		if (collide){
            if (xa < 0){
				setX((int) ((x - width) / 16) * 16 + width);
                setXa(0);
            }
            if (xa > 0){
                setX((int) ((x + width) / 16 + 1) * 16 - width - 1);
                setXa(0);
            }
            if (ya < 0){
                setY((int) ((y - height) / 16) * 16 + height);
                setYa(0);
                bumpedCeiling = true;
            }
            if (ya > 0){
                setY((int) (y / 16 + 1) * 16 - 1);
                onGround = true;
            }
            return false;
        }
        else{
            setX(getX() + xa);
            setY(getY() + ya);
            return true;
        }
    }

    protected boolean isBlocking(float _x, float _y, float xa, float ya){
        if (!canBlock) return false;

        int x = (int) (_x / 16);
        int y = (int) (_y / 16);
        if (x == (int) (getX()/16) && y == (int) (getY()/16)) return false;

        if (swims){
            if (!Level.getBehavior(getWorld().level.getBlock(x, y).blockId).contains(Block.Trait.Swimmable))
                return true;
        }

        return getWorld().level.isBlocking(x, y, xa, ya, _x, _y);
    }

    public boolean shellCollideCheck(Shell shell){
        if (deadTime != 0) return false;
        if (!tangible) return false;

        float xD = shell.getX() - getX();
        float yD = shell.getY() - getY();

        if (shell.type != 4) //bob-ombs don't hurt
	        if (xD > -16 && xD < 16){
	            if (yD > -getHeight() && yD < shell.getHeight()){
	                getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);
	
	                setXa(shell.facing * 2);
	                setYa(-5);
	                flyDeath = true;
	                if (getSpriteTemplate() != null) getSpriteTemplate().isDead = true;
	                deadTime = 100;
	                winged = false;
	                setHPic(-getHPic());
	                setYPicO(-getYPicO() + 16);
	                return true;
	            }
	        }
        return false;
    }

    public boolean fireballCollideCheck(Fireball fireball){
        if (deadTime != 0) return false;
        if (!tangible) return false;
        if (fireball.getSource() != getWorld().mario) return false; //enemy fireballs can only hit Mario

        float xD = fireball.getX() - getX();
        float yD = fireball.getY() - getY();

        if (xD > -16 && xD < 16){
            if (yD > -getHeight() && yD < fireball.getHeight()){
                if (immuneToFireballs) return true;
                
                getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);

                setXa(fireball.facing * 2);
                setYa(-5);
                flyDeath = true;
                if (getSpriteTemplate() != null) getSpriteTemplate().isDead = true;
                deadTime = 100;
                winged = false;
                setHPic(-getHPic());
                setYPicO(-getYPicO() + 16);
                return true;
            }
        }
        return false;
    }

    public boolean knockCheck(int xTile, int yTile, boolean up, boolean down, boolean left, boolean right){
        if (deadTime != 0) return false;
        if (!tangible) return false;
        if (!knockable) return false;

    	boolean ret = super.knockCheck(xTile, yTile, up, down, left, right);
    	if (ret) facing = -1*facing;
    	return ret;
    }

    public void bumpCheck(int xTile, int yTile){
        if (deadTime != 0) return;
        if (!tangible) return;
        if (!bumpable) return;

        if (getX() + getWidth() > xTile * 16 && getX() - getWidth() < xTile * 16 + 16 && yTile == (int) ((getY() - 1) / 16)){
            getWorld().sound.play(Art.samples[Art.SAMPLE_MARIO_KICK], this, 1, 1, 1);
            bumped();
        }
    }

    protected void bumped(){
        if (shellDropType > -1){ //drops something
            if (winged){
                winged = false;
                setYa(-5);
            }
            else{
                setYPicO(31 - (32 - 8));
                setHPic(8);
                if (getSpriteTemplate() != null) getSpriteTemplate().isDead = true;
                deadTime = 10;
                winged = false;

                Sprite shell = getHurt();
                shell.setYa(-8);
            }
        }
        else{ //doesn't drop a shell
            setXa(-getWorld().mario.facing * 2);
            setYa(-5);
            flyDeath = true;
            if (getSpriteTemplate() != null) getSpriteTemplate().isDead = true;
            deadTime = 100;
            winged = false;
            setHPic(-getHPic());
            setYPicO(-getYPicO() + 16);
        }
    }
    
    public void render(Graphics og, float alpha){
    	if (winged){
            int xPixel = (int) (getXOld() + (getX() - getXOld()) * alpha) - getXPicO();
            int yPixel = (int) (getYOld() + (getY() - getYOld()) * alpha) - getYPicO();

            if (doubleWinged){
                setXFlipPic(!isXFlipPic());
                og.drawImage(getSheet()[wingTime / 4 % 2][4], xPixel + (isXFlipPic() ? getWPic() : 0) + (isXFlipPic() ? 10 : -10), yPixel + (isYFlipPic() ? getHPic() : 0) - 8, isXFlipPic() ? -getWPic() : getWPic(), isYFlipPic() ? -getHPic() : getHPic(), null);
                setXFlipPic(!isXFlipPic());
                og.drawImage(getSheet()[wingTime / 4 % 2][4], xPixel + (isXFlipPic() ? getWPic() : 0) + (isXFlipPic() ? 10 : -10), yPixel + (isYFlipPic() ? getHPic() : 0) - 8, isXFlipPic() ? -getWPic() : getWPic(), isYFlipPic() ? -getHPic() : getHPic(), null);
            }
            else
                og.drawImage(getSheet()[wingTime / 4 % 2][4], xPixel + (isXFlipPic() ? getWPic() : 0) + (isXFlipPic() ? 10 : -10), yPixel + (isYFlipPic() ? getHPic() : 0) - 10, isXFlipPic() ? -getWPic() : getWPic(), isYFlipPic() ? -getHPic() : getHPic(), null);
        }

        super.render(og, alpha);
    }
}