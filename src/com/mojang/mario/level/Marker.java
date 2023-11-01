package com.mojang.mario.level;

import javax.swing.*;
import com.mojang.mario.*;

public class Marker {

    public static enum Type { 
    	START_POS { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[4][0]); }}, 
    	END_POS { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[4][1]); }}, 
    	ENTRANCE_BIGPIPE_UP { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[2][0]); }}, 
    	ENTRANCE_BIGPIPE_DOWN { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[2][1]); }},  
    	ENTRANCE_BIGPIPE_LEFT { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[3][1]); }}, 
    	ENTRANCE_BIGPIPE_RIGHT { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[3][0]); }}, 
    	EXIT_BIGPIPE_UP { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[0][1]); }}, 
    	EXIT_BIGPIPE_DOWN { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[0][0]); }},  
    	EXIT_BIGPIPE_LEFT { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[1][0]); }}, 
    	EXIT_BIGPIPE_RIGHT { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[1][1]); }}, 
    	ENTRANCE_SMALLPIPE_UP { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[2][2]); }}, 
    	ENTRANCE_SMALLPIPE_DOWN { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[2][3]); }},  
    	ENTRANCE_SMALLPIPE_LEFT { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[3][3]); }}, 
    	ENTRANCE_SMALLPIPE_RIGHT { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[3][2]); }}, 
    	EXIT_SMALLPIPE_UP { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[0][3]); }}, 
    	EXIT_SMALLPIPE_DOWN { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[0][2]); }},  
    	EXIT_SMALLPIPE_LEFT { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[1][2]); }}, 
    	EXIT_SMALLPIPE_RIGHT { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[1][3]); }},
    	ENTER_DOOR { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[4][3]); }},
    	EXIT_DOOR { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[4][2]); }},
    	WIN_DOOR { public ImageIcon getEditorIcon(){ return null; }},
    	EXIT_BEANSTALK_UPWARD { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[5][2]); }},
    	ENTER_BEANSTALK_UPWARD { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[5][3]); }},
    	EXIT_BEANSTALK_DOWNWARD { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[5][0]); }},
    	ENTER_BEANSTALK_DOWNWARD { public ImageIcon getEditorIcon(){ return new ImageIcon(Art.editorIcons[5][1]); }},
    	;
    	public abstract ImageIcon getEditorIcon();
    } //eventually doors

	public String id;
	public Type type;
	public int x, y, width, height;

	public Marker(String id, int x, int y, int width, int height, Type type){
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.type = type;
	}
	
	public boolean isEntrance(){ return type == Type.ENTRANCE_BIGPIPE_UP || type == Type.ENTRANCE_BIGPIPE_DOWN ||
		type == Type.ENTRANCE_BIGPIPE_LEFT || type == Type.ENTRANCE_BIGPIPE_RIGHT || 
		type == Type.ENTRANCE_SMALLPIPE_UP || type == Type.ENTRANCE_SMALLPIPE_DOWN ||
		type == Type.ENTRANCE_SMALLPIPE_LEFT || type == Type.ENTRANCE_SMALLPIPE_RIGHT ||
		type == Type.ENTER_DOOR || 
		type == Type.ENTER_BEANSTALK_DOWNWARD || type == Type.ENTER_BEANSTALK_UPWARD;
	}
	
	public boolean isExit(){ return type == Type.EXIT_BIGPIPE_UP || type == Type.EXIT_BIGPIPE_DOWN ||
		type == Type.EXIT_BIGPIPE_LEFT || type == Type.EXIT_BIGPIPE_RIGHT || 
		type == Type.EXIT_SMALLPIPE_UP || type == Type.EXIT_SMALLPIPE_DOWN ||
		type == Type.EXIT_SMALLPIPE_LEFT || type == Type.EXIT_SMALLPIPE_RIGHT ||
		type == Type.EXIT_DOOR || type == Type.WIN_DOOR || 
		type == Type.EXIT_BEANSTALK_DOWNWARD || type == Type.EXIT_BEANSTALK_UPWARD;
	}
}
