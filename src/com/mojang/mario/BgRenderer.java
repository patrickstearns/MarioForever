package com.mojang.mario;

import java.awt.*;
import com.mojang.mario.level.*;

public class BgRenderer{

	private static final Color transparent = new Color(0, 0, 0, 0);

	private int xCam, yCam, width, height, distance;
    private Image image;
    private Graphics2D g;
    private Level level;
    private LevelGenerator.Background type;
    
    public boolean renderBehaviors = false;

    public BgRenderer(Level level, GraphicsConfiguration graphicsConfiguration, int width, int height, int distance,
    		LevelGenerator.Background type){
        this.distance = distance;
        this.width = width;
        this.height = height;
        this.type = type;
        
        this.level = level;
        image = graphicsConfiguration.createCompatibleImage(width, height, Transparency.BITMASK);
        g = (Graphics2D) image.getGraphics();
        g.setComposite(AlphaComposite.Src);

        updateArea(0, 0, width, height);
    }

    public void setCam(int xCam, int yCam){
        xCam /= distance;
        yCam /= distance;
        int xCamD = this.xCam - xCam;
        int yCamD = this.yCam - yCam;
        this.xCam = xCam;
        this.yCam = yCam;

        g.setComposite(AlphaComposite.Src);
        g.copyArea(0, 0, width, height, xCamD, yCamD);

        if (xCamD < 0){
            if (xCamD < -width) xCamD = -width;
            updateArea(width + xCamD, 0, -xCamD, height);
        }
        else if (xCamD > 0){
            if (xCamD > width) xCamD = width;
            updateArea(0, 0, xCamD, height);
        }

        if (yCamD < 0){
            if (yCamD < -width) yCamD = -width;
            updateArea(0, height + yCamD, width, -yCamD);
        }
        else if (yCamD > 0){
            if (yCamD > width) yCamD = width;
            updateArea(0, 0, width, yCamD);
        }
    }

    private void updateArea(int x0, int y0, int w, int h){
    	Image[][] sheet = Art.bg;
    	if (type == LevelGenerator.Background.SNOW ||
    			type == LevelGenerator.Background.UNDERGROUND_2 ||
    			type == LevelGenerator.Background.UNDERWATER)
    		sheet = Art.bg2;
    	else if (type == LevelGenerator.Background.LAVA ||
    			type == LevelGenerator.Background.DESERT ||
    			type == LevelGenerator.Background.SKY)
    		sheet = Art.bg3;
    	g.setBackground(transparent);
        g.clearRect(x0, y0, w, h);
        int xTileStart = (x0 + xCam) / 32;
        int yTileStart = (y0 + yCam) / 32;
        int xTileEnd = (x0 + xCam + w) / 32;
        int yTileEnd = (y0 + yCam + h) / 32;
        
        //expand y's by 1 to avoid white stripes when scrolling up/down
        yTileStart -= 1;
        yTileEnd += 1;
        
        for (int x = xTileStart; x <= xTileEnd; x++){
            for (int y = yTileStart; y <= yTileEnd; y++){
                int b = level.getBlock(x, y).blockId ;// & 0xff; //what is this for?
try{
				g.drawImage(sheet[b % 8][b / 8], (x << 5) - xCam, (y << 5) - yCam-16, null);
				g.setColor(Color.MAGENTA);
//				g.drawString(x+","+y, (x << 5) - xCam, (y << 5) - yCam-16);
}
catch(ArrayIndexOutOfBoundsException e){
System.out.println("AIOOBE b="+b+" at "+x+","+y);
}
            }
        }
    }

    public void render(Graphics g, int tick, float alpha){
        g.drawImage(image, 0, 0, null);
    }

    public void setLevel(Level level){
        this.level = level;
        updateArea(0, 0, width, height);
    }
}