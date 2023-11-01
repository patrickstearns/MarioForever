package com.mojang.mario.mapedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.mojang.mario.*;

@SuppressWarnings("serial")
public class TilePicker extends JComponent implements MouseListener, MouseMotionListener {

    private int xTile, yTile, pickedBlockId;
    private LevelEditor levelEditor;
    private TilesetEditor tilesetEditor;

    public TilePicker(LevelEditor levelEditor, TilesetEditor tilesetEditor){
    	this.levelEditor = levelEditor;
    	this.tilesetEditor = tilesetEditor;
    	
    	Dimension size = new Dimension(256, 768);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        addMouseListener(this);
        addMouseMotionListener(this);
        
        xTile = -1;
        yTile = -1;
    }

    public void addNotify(){
        super.addNotify();
        Art.init(getGraphicsConfiguration(), null);
    }

    public void paintComponent(Graphics g){
        g.setColor(new Color(0x8090ff));
        g.fillRect(0, 0, getSize().width, getSize().height);
        
        for (int x = 0; x < Art.level.length; x++)
            for (int y = 0; y < Art.level[x].length; y++)
                g.drawImage(Art.level[x][y], x*16, y*16, null);

        g.setColor(Color.WHITE);
        int xPickedTile = pickedBlockId%16;
        int yPickedTile = pickedBlockId/16;
        g.drawRect(xPickedTile * 16, yPickedTile * 16, 15, 15);

        g.setColor(Color.BLACK);
        g.drawRect(xTile * 16 - 1, yTile * 16 - 1, 17, 17);
    }

    public void mouseClicked(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){
        xTile = -1;
        yTile = -1;
        repaint();
    }

    public void mousePressed(MouseEvent e){
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;
        
        setPickedBlockId(xTile+yTile*16);
        repaint();
    }

    public void mouseReleased(MouseEvent e){}
    public void mouseDragged(MouseEvent e){
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;
        repaint();
    }

    public void mouseMoved(MouseEvent e){
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;
        repaint();
    }

    public int getPickedBlockId(){ return pickedBlockId; }
    public void setPickedBlockId(int blockId){
    	int old = pickedBlockId;
    	pickedBlockId = blockId;
        repaint();
        if (levelEditor != null && old != blockId){
        	levelEditor.tilePicked();
        	tilesetEditor.tilePicked();
        }
    }
}