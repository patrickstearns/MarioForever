package com.mojang.mario;

import java.awt.*;
import java.util.ArrayList;
import com.mojang.mario.level.*;

public class LevelRenderer{

    public static boolean renderBehaviors = true;
    public static Color WATER_COLOR = new Color(0.3f, 0.3f, 1f, 0.3f);
    
	private int xCam, yCam, width, height;
    private Image image;
    private Graphics2D g;
    private static final Color transparent = new Color(0, 0, 0, 0);
    private Level level;
    private int levelBandY = 0;
 
    public LevelRenderer(Level level, GraphicsConfiguration graphicsConfiguration, int width, int height){
        this.width = width;
        this.height = height;
        this.level = level;

        image = graphicsConfiguration.createCompatibleImage(width, height, Transparency.BITMASK);
        g = (Graphics2D) image.getGraphics();
        g.setComposite(AlphaComposite.Src);
        updateArea(0, 0, width, height);
    }

    public void setCam(int xCam, int yCam){
        int xCamD = this.xCam - xCam;
        int yCamD = this.yCam - yCam;
        this.xCam = xCam;
        this.yCam = yCam;

        g.setComposite(AlphaComposite.Src);
        g.copyArea(0, 0, width, height, xCamD, yCamD);

        if (xCamD < 0)
        {
            if (xCamD < -width) xCamD = -width;
            updateArea(width + xCamD, 0, -xCamD, height);
        }
        else if (xCamD > 0)
        {
            if (xCamD > width) xCamD = width;
            updateArea(0, 0, xCamD, height);
        }

        if (yCamD < 0)
        {
            if (yCamD < -width) yCamD = -width;
            updateArea(0, height + yCamD, width, -yCamD);
        }
        else if (yCamD > 0)
        {
            if (yCamD > width) yCamD = width;
            updateArea(0, 0, width, yCamD);
        }
    }

    private void updateArea(int x0, int y0, int w, int h){
    	g.setBackground(transparent);
        g.clearRect(x0, y0, w, h);
        int xTileStart = (x0 + xCam) / 16;
        int yTileStart = (y0 + yCam) / 16;
        int xTileEnd = (x0 + xCam + w) / 16;
        int yTileEnd = (y0 + yCam + h) / 16;
        for (int x = xTileStart; x <= xTileEnd; x++){
            for (int y = yTileStart; y <= yTileEnd; y++){
                Block b = level.getBlock(x, y);
                ArrayList<Block.Trait> traits = Level.getBehavior(b.blockId);
               	if (!traits.contains(Block.Trait.Animated) &&
               			!traits.contains(Block.Trait.Behind)){
                    g.drawImage(Art.level[b.blockId % 16][b.blockId / 16], x*16 - xCam, y*16 - yCam, null);
                }
            }
        }
    }

    public void render(Graphics g, int tick, float alpha, boolean behindOnly, boolean waterOnly){
        if (!waterOnly && !behindOnly) 
        	g.drawImage(image, 0, 0, null);

        for (int x = xCam / 16; x <= (xCam + width) / 16; x++){
            for (int y = yCam / 16; y <= (yCam + height) / 16; y++){
                Block b = level.getBlock(x, y);
                ArrayList<Block.Trait> traits = Level.getBehavior(b.blockId);
                boolean behind = traits.contains(Block.Trait.Behind);
                boolean swimmable = traits.contains(Block.Trait.Swimmable);
                boolean animated = traits.contains(Block.Trait.Animated);

                if (behindOnly && !behind) continue;
                if (waterOnly && !swimmable) continue;

                if (waterOnly){
                	if (swimmable){
	                	g.setColor(WATER_COLOR);	
	                	g.fillRect( (x*16) - xCam, (y*16) - yCam, 16, 16);
                	}
                }
                else{
	                int xo = 0, yo = 0;
	                if (x >= 0 && y >= 0 && x < level.getWidth() && y < level.getHeight()){
	                	xo = level.getXOffsets()[x][y];
	                	yo = level.getYOffsets()[x][y];
	                	
	                	if (xo > 0) xo = (int) (Math.cos((xo - alpha) / 4.0f * Math.PI) * 8);
	                	else if (xo < 0) xo = -(int)(Math.cos((-xo - alpha) / 4.0f * Math.PI) * 8);
	                	if (yo > 0) yo = (int) (Math.sin((yo - alpha) / 4.0f * Math.PI) * 8);
	                	else if (yo < 0) yo = -(int)(Math.sin((-yo - alpha) / 4.0f * Math.PI) * 8);
	                }
	                
	               	if (behindOnly && behind){
	                    g.drawImage(Art.level[b.blockId % 16][b.blockId / 16], 
	                    		(x*16) - xCam - xo, (y*16) - yCam - yo, null);
	               	}
	               	else if (!behind && animated){
	                    int animTime = (tick / 3) % 4;
	
	                    if ((b.blockId % 16) / 4 == 0 && b.blockId / 16 == 1){
	                        animTime = (tick / 2 + (x + y) / 8) % 20;
	                        if (animTime > 3) animTime = 0;
	                    }
	                    if ((b.blockId % 16) / 4 == 3 && b.blockId / 16 == 0){
	                        animTime = 2;
	                    }
	                    
	                    g.drawImage(Art.level[(b.blockId % 16) / 4 * 4 + animTime][b.blockId / 16], 
	                    		(x*16) - xCam - xo, (y*16) - yCam - yo, null);
	                }
                }
                
                if (!behindOnly && renderBehaviors){
                	if (traits.contains(Block.Trait.BlockUpper)){
                        g.setColor(Color.RED);
                        g.fillRect((x*16) - xCam, (y*16) - yCam, 16, 2);
                    }
//                	if (traits.contains(Block.Trait.BlockAll)){
//                        g.setColor(Color.RED);
//                        g.fillRect((x*16) - xCam, (y*16) - yCam, 16, 2);
//                        g.fillRect((x*16) - xCam, (y*16) - yCam + 14, 16, 2);
//                        g.fillRect((x*16) - xCam, (y*16) - yCam, 2, 16);
//                        g.fillRect((x*16) - xCam + 14, (y*16) - yCam, 2, 16);
//                    }
                	if (traits.contains(Block.Trait.BlockLower)){
                        g.setColor(Color.RED);
                        g.fillRect((x*16) - xCam, (y*16) - yCam + 14, 16, 2);
                    }
//                    if (traits.contains(Block.Trait.SlideLeft)){
//                        g.setColor(Color.RED);
//                        int[] xs = new int[]{
//                                (x*16) - xCam,
//                                (x*16) + 16 - xCam,
//                                (x*16) + 16 - xCam
//                        };
//                        int[] ys = new int[]{
//                                (y*16) + 16 - yCam,
//                                (y*16) + 16 - yCam,
//                                (y*16) + yCam,
//                        };
//                        g.fillPolygon(xs, ys, 3);
//                    }
//                    if (traits.contains(Block.Trait.SlideRight)){
//                        g.setColor(Color.RED);
//                        int[] xs = new int[]{
//                                (x*16) - xCam,
//                                (x*16) + 16 - xCam,
//                                (x*16) - xCam
//                        };
//                        int[] ys = new int[]{
//                                (y*16) + 16 - yCam,
//                                (y*16) + 16 - yCam,
//                                (y*16) + yCam,
//                        };
//                        g.fillPolygon(xs, ys, 3);
//                    }
                	if (traits.contains(Block.Trait.VertBumpable)){
                        g.setColor(Color.BLUE);
                        g.fillRect((x*16) - xCam + 2, (y*16) - yCam + 2, 4, 4);
                    }
                	if (traits.contains(Block.Trait.Breakable)){
                        g.setColor(Color.GREEN);
                        g.fillRect((x*16) - xCam + 2 + 4, (y*16) - yCam + 2, 4, 4);
                    }
                	if (traits.contains(Block.Trait.Pickupable)){
                        g.setColor(Color.YELLOW);
                        g.fillRect((x*16) - xCam + 2, (y*16) - yCam + 2 + 4, 4, 4);
                    }
                	if (traits.contains(Block.Trait.Animated)){}
                	if (b.blockId >= Level.GHOST_COIN && b.blockId < Level.GHOST_COIN+4){
	                    g.drawImage(Art.level[Level.BLUE_COIN % 16][Level.BLUE_COIN / 16], 
	                    		(x*16) - xCam, (y*16) - yCam, null);
                	}

                	//powerups
                	Image powerupImage = null;
                	Block.Powerup p = Level.getPowerup(b.blockId);
                	if (p == Block.Powerup.FireFlower) powerupImage = Art.items[1][0];
                	else if (p == Block.Powerup.OneUpMushroom) powerupImage = Art.items[2][0];
                	else if (p == Block.Powerup.Coin) powerupImage = Art.map[Level.COIN/16][Level.COIN%16];
                	else if (p == Block.Powerup.Multicoin) powerupImage = Art.items[0][2];
                	else if (p == Block.Powerup.Star) powerupImage = Art.items[0][1];
                	else if (p == Block.Powerup.BlueSwitch) powerupImage = Art.items[0][3];
                	else if (p == Block.Powerup.RedSwitch) powerupImage = Art.items[0][4];
                	else if (p == Block.Powerup.YellowSwitch) powerupImage = Art.items[0][5];
                	else if (p == Block.Powerup.GreenSwitch) powerupImage = Art.items[0][6];
                	else if (p == Block.Powerup.Beanstalk) powerupImage = Art.items[0][7];
                	if (powerupImage != null) g.drawImage(powerupImage, x*16+8-powerupImage.getWidth(null)/2, y*16+8-powerupImage.getHeight(null)/2, null);

//                	//markers
//                	if (level.getMarker(x, y, true) != null){
//                		Marker marker = level.getMarker(x, y, true);
//                		Image markerImage = marker.type.getEditorIcon().getImage();
//                		String markerName = marker.id;
//                		g.drawImage(markerImage, x*16+8-markerImage.getWidth(null)/2, y*16+8-markerImage.getHeight(null)/2, null);
//                		if (marker.isEntrance()) g.setColor(Color.RED);
//                		else g.setColor(Color.BLUE);
//                		g.setFont(new Font("Arial", Font.PLAIN, 10));
//                		g.drawString(markerName, x*16+markerImage.getWidth(null)+1, y*16+16);
//                	}
                }
            }
        }
    }

    public int getLevelBandY(){ return levelBandY; }
    
    public void repaint(int x, int y, int w, int h){
        updateArea(x * 16 - xCam, y * 16 - yCam, w * 16, h * 16);
    }

    public void setLevel(Level level){
        this.level = level;
        updateArea(0, 0, width, height);
    }

    public void renderExit0(Graphics g, int tick, float alpha, boolean bar){
        if (level.getCurrentArea().findMarker(Marker.Type.END_POS) == null) return;;
    	
        int xExit = level.getCurrentArea().findMarker(Marker.Type.END_POS).x;
        int yExit = level.getCurrentArea().findMarker(Marker.Type.END_POS).y+1;
    	
    	int postX = Level.GOAL_0_POST%16, postY = Level.GOAL_0_POST/16;
        for (int y = yExit-7; y < yExit; y++){
            g.drawImage(Art.level[postX][postY], (xExit*16) - xCam - 16, (y*16) - yCam, null);
        }

        int capX = Level.GOAL_0_CAP%16, capY = Level.GOAL_0_CAP/16;
        g.drawImage(Art.level[capX][capY], (xExit*16) - xCam - 16, ((yExit-8)*16) - yCam, null);
        
        if (bar){
        	int yh = yExit*16 - (int) ((Math.sin((tick + alpha) / 20) * 0.5 + 0.5) * 7 * 16) - 8;
        	levelBandY = yh;
        	int bar0X = Level.GOAL_0_BAND%16, bar0Y = Level.GOAL_0_BAND/16;
        	int bar1X = Level.GOAL_1_BAND%16, bar1Y = Level.GOAL_1_BAND/16;
            g.drawImage(Art.level[bar0X][bar0Y], (xExit*16) - xCam - 16, yh - yCam, null);
            g.drawImage(Art.level[bar1X][bar1Y], (xExit*16) - xCam, yh - yCam, null);
        }
    }

    public void renderExit1(Graphics g, int tick, float alpha){
        if (level.getCurrentArea().findMarker(Marker.Type.END_POS) == null) return;
    	
        int xExit = level.getCurrentArea().findMarker(Marker.Type.END_POS).x;
        int yExit = level.getCurrentArea().findMarker(Marker.Type.END_POS).y+1;
    	
       	int postX = Level.GOAL_1_POST%16, postY = Level.GOAL_1_POST/16;
        for (int y = yExit - 7; y < yExit; y++){
            g.drawImage(Art.level[postX][postY], (xExit*16) - xCam + 16, (y*16) - yCam, null);
        }

        int capX = Level.GOAL_1_CAP%16, capY = Level.GOAL_1_CAP/16;
        g.drawImage(Art.level[capX][capY], (xExit*16) - xCam + 16, ((yExit-8)*16) - yCam, null);
    }

	public int getWidth(){ return width; }
	public void setWidth(int width){ this.width = width; }

	public int getHeight(){ return height; }
	public void setHeight(int height){ this.height = height; }
}