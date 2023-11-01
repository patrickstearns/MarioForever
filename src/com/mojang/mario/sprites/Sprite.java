package com.mojang.mario.sprites;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import com.mojang.mario.level.SpriteTemplate;
import com.mojang.mario.scene.LevelScene;
import com.mojang.mario.sprites.projectiles.Fireball;
import com.mojang.sonar.SoundSource;
import com.mojang.util.ColorFilterOp;
import com.mojang.util.FadeImageOp;

public class Sprite implements SoundSource {
	
    public static SpriteContext spriteContext;
    
    private float xOld, yOld, x, y, xa, ya;
    private int width, height;
    private int xPic, yPic, wPic, hPic;
    private int xPicO, yPicO, layer;
    private boolean xFlipPic, yFlipPic, visible, invulnerableFlash;
    protected Image[][] sheet;
    private SpriteTemplate spriteTemplate;
    private LevelScene world;
    public float alphaTransparency = 1f;
    protected boolean persistent;
    
    protected Sprite(LevelScene world){
    	setWorld(world);
    	setWidth(7);
    	setHeight(24);
    	setWPic(32);
    	setHPic(32);
    	setLayer(1);
    	setXFlipPic(false);
    	setYFlipPic(false);
    	setVisible(true);
    	setInvulnerableFlash(false);
    	setPersistent(false);
    }
    
    private Color[] invulnerableColors = new Color[]{
    	Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA
    };
    
    public void render(Graphics og, float alpha){
        if (!visible) return;

        //image location plus sprite offset
        int xPixel = (int)(getXOld()+(getX()-getXOld())*alpha)-xPicO;
        int yPixel = (int)(getYOld()+(getY()-getYOld())*alpha)-yPicO;
        
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

        if (isStarInvulnerable()){
        	Color xor = invulnerableColors[((int)(Math.random()*1000))%invulnerableColors.length];
        	Image tinted = tintImage(sheet[xPic][yPic], xor, null);
        	og.drawImage(tinted, xPixel+(xFlipPic?wPic:0), yPixel+(yFlipPic?hPic:0), xFlipPic?-wPic:wPic, yFlipPic?-hPic:hPic, null);
        }
        else {
        	Image image = sheet[xPic][yPic];
        	if (alphaTransparency != 1){
        		image = fadeImage(image, alphaTransparency, null);
        	}
        	og.drawImage(image, xPixel+(xFlipPic?wPic:0), yPixel+(yFlipPic?hPic:0), xFlipPic?-wPic:wPic, yFlipPic?-hPic:hPic, null);
        }

//        og.setColor(Color.RED);
//        og.drawRect((int)(getX()-getWidth()), (int)(getY()-getHeight()), getWidth()*2, getHeight());
    }
    
    public LevelScene getWorld(){ return world; }
	public void setWorld(LevelScene world){ this.world = world; }

	public float getXOld(){ return xOld; }
	public void setXOld(float old){ xOld = old; }

	public float getYOld(){ return yOld; }
	public void setYOld(float old){ yOld = old; }

    public float getX(float alpha){ return (getXOld()+(getX()-getXOld())*alpha)-xPicO; }
	public float getX(){ return x; }
	public void setX(float x){ this.x = x; }

    public float getY(float alpha){ return (getYOld()+(getY()-getYOld())*alpha)-yPicO; }
	public float getY(){ return y; }
	public void setY(float y){ this.y = y; }

	public float getXa(){ return xa; }
	public void setXa(float xa){ this.xa = xa; }

	public float getYa(){ return ya; }
	public void setYa(float ya){ this.ya = ya; }

	public int getWidth(){ return width; }
	public void setWidth(int width){ this.width = width; }

	public int getHeight(){ return height; }
	public void setHeight(int height){ this.height = height; }

	public int getXPic(){ return xPic; }
	public void setXPic(int pic){ xPic = pic; }

	public int getYPic(){ return yPic; }
	public void setYPic(int pic){ yPic = pic; }

	public int getWPic(){ return wPic; }
	public void setWPic(int pic){ wPic = pic; }

	public int getHPic(){ return hPic; }
	public void setHPic(int pic){ hPic = pic; }

	public int getXPicO(){ return xPicO; }
	public void setXPicO(int picO){ xPicO = picO; }

	public int getYPicO(){ return yPicO; }
	public void setYPicO(int picO){ yPicO = picO; }

	public int getLayer(){ return layer; }
	public void setLayer(int layer){ this.layer = layer; }

	public boolean isXFlipPic(){ return xFlipPic; }
	public void setXFlipPic(boolean flipPic){ xFlipPic = flipPic; }

	public boolean isYFlipPic(){ return yFlipPic; }
	public void setYFlipPic(boolean flipPic){ yFlipPic = flipPic; }

	public boolean isVisible(){ return visible; }
	public void setVisible(boolean visible){ this.visible = visible; }

	public boolean isStarInvulnerable(){ return false; }
	public boolean isInvulnerableFlash(){ return invulnerableFlash; }
	public void setInvulnerableFlash(boolean invulnerableFlash){ this.invulnerableFlash = invulnerableFlash; }

	public Image[][] getSheet(){ return sheet; }
	public void setSheet(Image[][] sheet){ this.sheet = sheet; }

	public SpriteTemplate getSpriteTemplate(){ return spriteTemplate; }
	public void setSpriteTemplate(SpriteTemplate spriteTemplate){ this.spriteTemplate = spriteTemplate; }

	public boolean isPersistent(){ return persistent; }
	public void setPersistent(boolean persistent){ this.persistent = persistent; }

	protected boolean move(float xa, float ya){ return true; }
    public void move(){
    	setX(getX()+getXa());
    	setY(getY()+getYa());
    }
    
    public final void tick(){
    	setXOld(getX());
    	setYOld(getY());
        move();
    }

    public final void tickNoMove(){
    	setXOld(getX());
    	setYOld(getY());
    }

    public boolean knockCheck(int xt, int yt, boolean up, boolean down, boolean left, boolean right){
    	boolean inXRange = false, inYRange = false;
    	inXRange = (getX()+width >= xt*16 && getX()-width <= (xt+1)*16);
		inYRange = (getY() >= yt*16 && getY() <= (yt+1)*16);

		boolean ret = false;
    	if (inXRange && inYRange){
    		if (up) setYa(getYa()+20);
    		else if (down) setYa(getYa()-15);
    		else if (left) setXa(getXa()-5);
    		else if (right) setXa(getXa()+5);
    		ret = true;
    	}
		
		return ret;
    }

    public void collideCheck(){}
    public void bumpCheck(int xTile, int yTile){}
    public void release(Mario mario){}
    public boolean shellCollideCheck(Shell shell){ return false; }
    public boolean fireballCollideCheck(Fireball fireball){ return false; }
    public boolean spriteCollideCheck(Sprite sprite){ return false; }
    
	public static BufferedImage tintImage(Image image, Color color, ImageObserver io){
		ColorFilterOp op = new ColorFilterOp(color);
		if (!(image instanceof BufferedImage)){
			BufferedImage buf = new BufferedImage(image.getWidth(io), image.getHeight(io), BufferedImage.TYPE_4BYTE_ABGR);
			buf.getGraphics().drawImage(image, 0, 0, io);
			image = buf;
		}
		return op.filter((BufferedImage)image, null);			
	}

	public static BufferedImage fadeImage(Image image, double fade, ImageObserver io){
		FadeImageOp fadeOp = new FadeImageOp();
		if (!(image instanceof BufferedImage)){
			BufferedImage buf = new BufferedImage(image.getWidth(io), image.getHeight(io), BufferedImage.TYPE_4BYTE_ABGR);
			buf.getGraphics().drawImage(image, 0, 0, io);
			image = buf;
		}
		return fadeOp.filter((BufferedImage)image, null, fade);			
	}
}