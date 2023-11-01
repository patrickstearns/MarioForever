package com.mojang.mario.mapedit;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.*;
import com.mojang.mario.*;
import com.mojang.mario.level.*;

public class AreaEditPanel extends JComponent implements MouseListener, MouseMotionListener{
    private static final long serialVersionUID = -7696446733303717142L;

    private LevelRenderer levelRenderer;
    private Level level;

    private int xTile = -1;
    private int yTile = -1;
    private LevelEditor levelEditor;

    public AreaEditPanel(LevelEditor levelEditor){
        this.levelEditor = levelEditor;
        level = new Level((short)256, (short)128);

        addMouseListener(this);
        addMouseMotionListener(this);
        
        setLevel(level);
    }
    
    public void setLevel(Level level){
        this.level = level;
        Dimension size = new Dimension(level.getWidth() * 16, level.getHeight() * 16);
        setSize(size);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        if (levelRenderer != null) levelRenderer.setLevel(level);
        repaint();
    }
    
    public Level getLevel(){ return level; }

    public void addNotify(){
        super.addNotify();
        Art.init(getGraphicsConfiguration(), null);
        levelRenderer = new LevelRenderer(level, getGraphicsConfiguration(), level.getWidth() * 16, level.getHeight() * 16);
        LevelRenderer.renderBehaviors = false;

        repaint();
    }

    public void paintComponent(Graphics g){
        g.setColor(new Color(0x8090ff));
        g.fillRect(0, 0, level.getWidth() * 16, level.getHeight() * 16);
        levelRenderer.render(g, 0, 0, true, false);
        levelRenderer.render(g, 0, 0, false, false);
        g.setColor(Color.BLACK);
        g.drawRect(xTile * 16 - 1, yTile * 16 - 1, 17, 17);
        
        for (int i = 0; i < level.getWidth(); i++){
        	for (int j = 0; j < level.getHeight(); j++){
        		if (level.getSpriteTemplate(i, j) != null){
        			level.getSpriteTemplate(i, j).sprite.render(g, 1f);
        		}
        		
        		if (LevelRenderer.renderBehaviors && level.getMarker(i, j, true) != null){
        			Marker m = level.getMarker(i, j, true);
        			g.setColor(new Color(1f, 1f, 1f, 0.4f));
        			g.fillRect(i*16, j*16, m.width, m.height);
        			g.setColor(Color.PINK);
        			g.drawRect(i*16, j*16, m.width, m.height);
        			Image image = m.type.getEditorIcon().getImage();
        			g.drawImage(image, i*16+m.width/2-image.getWidth(this)/2, j*16+m.height/2-image.getHeight(this)/2, this);
        			g.setColor(Color.BLACK);
        			g.drawString(m.id, i*16, j*16);
        		}
        	}
        }
        levelRenderer.render(g, 0, 0, false, true); //water
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

        if (e.getButton() == 3){
            levelEditor.setPickedBlockId(level.getBlock(xTile, yTile).blockId);
        }
        else if (levelEditor.mode == LevelEditor.Mode.TILE){
            level.setBlock(xTile, yTile, levelEditor.getPickedBlockId());
            levelRenderer.repaint(xTile - 1, yTile - 1, 3, 3);
        }
        else if (levelEditor.mode == LevelEditor.Mode.ENEMY){
        	EnemyPicker.EnemyDef def = levelEditor.enemyPicker.pickedDef;
        	if (def == null) return;
        	if (def.kind != null){
	       	   	SpriteTemplate st = new SpriteTemplate(def.kind);
	        	level.setSpriteTemplate(xTile, yTile, st);
	        	st.spawn(null, xTile, yTile, def.dir);
        	}
        	else {
        		level.setSpriteTemplate(xTile, yTile, null);
        	}
        }
        else if (levelEditor.mode == LevelEditor.Mode.MARKER){
        	if (levelEditor.getSelectedMarkerType() == null) level.setMarker(xTile, yTile, null);
        	else if (levelEditor.getSelectedMarkerType() == Marker.Type.START_POS) level.setStartPos(xTile, yTile);
        	else if (levelEditor.getSelectedMarkerType() == Marker.Type.END_POS) level.setEndPos(xTile, yTile);
        	else{
        		String newId = JOptionPane.showInputDialog(this, "ID:", "New Marker", JOptionPane.QUESTION_MESSAGE);
        		if (newId == null) return;
        		Marker.Type type = levelEditor.getSelectedMarkerType();
        		int width = 16, height = 16;
        		if (type == Marker.Type.ENTRANCE_BIGPIPE_DOWN || type == Marker.Type.ENTRANCE_BIGPIPE_UP) width = 32;
        		if (type == Marker.Type.EXIT_BIGPIPE_DOWN || type == Marker.Type.EXIT_BIGPIPE_UP) width = 32;
        		if (type == Marker.Type.ENTRANCE_BIGPIPE_LEFT || type == Marker.Type.ENTRANCE_BIGPIPE_RIGHT) height = 32;
        		if (type == Marker.Type.EXIT_BIGPIPE_LEFT || type == Marker.Type.EXIT_BIGPIPE_RIGHT) height = 32;
        		level.setMarker(xTile, yTile, new Marker(newId, xTile, yTile, width, height, type));
        	}
        }
        repaint();
    }

    public void mouseReleased(MouseEvent e){}

    public void mouseDragged(MouseEvent e){
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;

        level.setBlock(xTile, yTile, levelEditor.getPickedBlockId());
        levelRenderer.repaint(xTile - 1, yTile - 1, 3, 3);

        repaint();
    }

    public void mouseMoved(MouseEvent e){
        xTile = e.getX() / 16;
        yTile = e.getY() / 16;
        repaint();
    }
}