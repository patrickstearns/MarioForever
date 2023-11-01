package com.mojang.mario;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.*;

public class FrameLauncher{

	private static boolean FULLSCREEN = false;
	
	public static void main(String[] args){
		if (FULLSCREEN) fullscreenMain(args);
		else windowedMain(args);
	}
	
    public static void windowedMain(String[] args){
        LevelRenderer.renderBehaviors = false;
        final MarioComponent mario = new MarioComponent(640, 480);
        if (args.length > 2){
        	String loadedFilename = args[2];
        	mario.setLoadLevelFilename(loadedFilename);
        }
        
        JFrame frame = new JFrame("Super Mario Bros. Forever");
        frame.setContentPane(mario);
        frame.pack();
        frame.setResizable(false);

        if (args.length <= 2) frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        else frame.addWindowListener(new WindowAdapter(){
        	public void windowClosing(WindowEvent e){
        		mario.stop();
        	}
        });
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);
        frame.setVisible(true);
        mario.start();
    }
    
    public static void fullscreenMain(String[] args){
        LevelRenderer.renderBehaviors = false;
        final MarioComponent mario = new MarioComponent(640, 480);
        MarioComponent.FULLSCREEN = true;
        if (args.length > 2){
        	String loadedFilename = args[2];
        	mario.setLoadLevelFilename(loadedFilename);
        }
        
        Frame frame = new Frame("Super Mario Bros. Forever");
        frame.setUndecorated(true);
        frame.setLayout(new GridLayout(1, 1));
        frame.add(mario);
        frame.pack();
        frame.setResizable(false);

		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (device.isFullScreenSupported()) {
            DisplayMode mode = getBestDisplayMode(device);
        	device.setFullScreenWindow(frame);
        	if (mode == null) System.out.println("No appropriate screen resolution is supported by this graphics device.");
        	else{
	        	System.out.println("Attempting to use display mode: "+mode.getWidth()+"x"+mode.getHeight()+", "+mode.getBitDepth()+" bit depth, "+mode.getRefreshRate());
	        	device.setDisplayMode(mode);

				frame.setCursor(frame.getToolkit().createCustomCursor(
						new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB),
						new Point(0, 0), "null"));        	
        	}
        	frame.validate();
        } 
        else System.out.println("Fullscreen mode is not supported on this graphics device.");
        
        mario.start();
        frame.setVisible(true);
    }
    
	private static DisplayMode getBestDisplayMode(GraphicsDevice device){
		DisplayMode[] modes = device.getDisplayModes();

		Comparator<DisplayMode> comparator = new Comparator<DisplayMode>(){
			public int compare(DisplayMode mode1, DisplayMode mode2){
				if (mode1.getBitDepth() != mode2.getBitDepth()) return mode2.getBitDepth()-mode1.getBitDepth();
				else if (mode1.getWidth() != mode2.getWidth()) return mode1.getWidth() - mode2.getWidth();
				else return mode1.getHeight() - mode2.getHeight();
			}
		};
		
		Arrays.sort(modes, comparator);
		
		for (DisplayMode dm: modes){
			if (dm.getWidth() < 320 || dm.getHeight() < 240) continue;
//			if (dm.getWidth()/dm.getHeight() != (8/5)) continue;
//			if (dm.getBitDepth() == 8) continue;
			return dm;
		}
		
		return null;
	}
    
}