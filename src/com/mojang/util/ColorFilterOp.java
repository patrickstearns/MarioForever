package com.mojang.util;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

public class ColorFilterOp implements BufferedImageOp {

	private Color color;
	private float targetHue;
	
	public ColorFilterOp(Color color){
		super();
		this.color = color;

        int target = color.getRGB();
        int tRed   = (target >> 16) & 0xff;
		int tGreen = (target >> 8)  & 0xff;
		int tBlue  = target        & 0xff;
		float[] hsb = new float[3];
		hsb = Color.RGBtoHSB(tRed, tGreen, tBlue, hsb);
		targetHue = hsb[0];
	}
	
	public final BufferedImage filter(BufferedImage source_img, BufferedImage dest_img) {
		// If no destination image provided, make one of same form as source
		if (dest_img == null) dest_img = createCompatibleDestImage (source_img, null);
		
		int width = source_img.getWidth();
		int height= source_img.getHeight();
		
		for (int y=0; y < height; y++) {
		    for (int x=0; x < width; x++) {
		    	//color of current pixel
		    	int pixel = source_img.getRGB (x,y);
		        
				// Get the component HSB
		    	int pAlpha = (pixel >> 24) & 0xff;
		    	int pRed   = (pixel >> 16) & 0xff;
				int pGreen = (pixel >> 8)  & 0xff;
				int pBlue  =  pixel        & 0xff;
				float[] pHSB = new float[3];
				pHSB = Color.RGBtoHSB(pRed, pGreen, pBlue, pHSB);
				
				//convert back to RGB
				pixel = Color.HSBtoRGB(targetHue, pHSB[1], pHSB[2]);
		    	int p2Red   = (pixel >> 16) & 0xff;
				int p2Green = (pixel >> 8)  & 0xff;
				int p2Blue  =  pixel        & 0xff;
				int newPixel = (pAlpha << 24)+(p2Red << 16)+(p2Green << 8)+(p2Blue);

				// Put new value into corresponding pixel of destination image;
		        dest_img.setRGB(x, y, newPixel);
		    }
		}
		return dest_img;
	}
	
	public final BufferedImage old_filter(BufferedImage source_img, BufferedImage dest_img) {
		// If no destination image provided, make one of same form as source
		if (dest_img == null) dest_img = createCompatibleDestImage (source_img, null);
		
		int width = source_img.getWidth();
		int height= source_img.getHeight();
		
		for (int y=0; y < height; y++) {
		    for (int x=0; x < width; x++) {
		        int pixel = source_img.getRGB (x,y);

				// Get the component colors
		        int pAlpha = (pixel >> 24) & 0xff;
		        int pRed   = (pixel >> 16) & 0xff;
				int pGreen = (pixel >> 8)  & 0xff;
				int pBlue  =  pixel        & 0xff;
				
				double cRatio = (double)color.getAlpha()/255d;
				cRatio /= 2d;
				
				int cRed = (int)(cRatio*(color.getRed()-pRed));
				int cGreen = (int)(cRatio*(color.getGreen()-pGreen));
				int cBlue = (int)(cRatio*(color.getBlue()-pBlue));
				
				int alpha = pAlpha;
				int red =  Math.min(pRed+cRed, 255);
				int green =  Math.min(pGreen+cGreen, 255);
				int blue =  Math.min(pBlue+cBlue, 255);

				//since we're only using this for stars, begin special part to brighten image:
				red += (255-red)/2;
				blue += (255-blue)/2;
				green += (255-green)/2;
				//end special part
				
				if (red < 0) red = 0;
				if (green < 0) green = 0;
				if (blue < 0) blue = 0;

				// Put new value into corresponding pixel of destination image;
		        pixel = (alpha << 24) |  (red << 16) |  (green << 8) | blue;
		        dest_img.setRGB (x,y,pixel);
		    }
		}
		return dest_img;
	}

    /**
     *  Create a destination image if needed. Must be same width as source
     *  and will by default use the same color model. Otherwise, it will use
     *  the one passed to it.
    **/
	public BufferedImage createCompatibleDestImage (BufferedImage source_img,  ColorModel dest_color_model) {
	    // If no color model passed, use the same as in source
	    if (dest_color_model == null)
	        dest_color_model = source_img.getColorModel ();
	
	    int width = source_img.getWidth ();
	    int height= source_img.getHeight ();
	
	    // Create a new image with this color model & raster. Check if the
	    // color components already multiplied by the alpha factor.
	    return new BufferedImage (
	           dest_color_model,
	           dest_color_model.createCompatibleWritableRaster (width,height),
	           dest_color_model.isAlphaPremultiplied(),
	           null);
	}

	public final Rectangle2D getBounds2D (BufferedImage source_img) {
		return source_img.getRaster().getBounds();
	}

	public final Point2D getPoint2D (Point2D source_point, Point2D dest_point) {
	    if (dest_point == null) dest_point = new Point2D.Float ();
	    dest_point.setLocation (source_point.getX(), source_point.getY());
	    return dest_point;
	}

	public final RenderingHints getRenderingHints(){ return null; }
}