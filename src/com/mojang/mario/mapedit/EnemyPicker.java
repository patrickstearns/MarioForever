package com.mojang.mario.mapedit;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import com.mojang.mario.*;
import com.mojang.mario.level.*;
import com.mojang.mario.sprites.*;

public class EnemyPicker extends JPanel implements ActionListener {
	private static final long serialVersionUID = 8627516001610731039L;

	public static class EnemyDef {
		public Enemy.Kind kind;
		public Integer dir;
		public Boolean winged;
		public EnemyDef(Enemy.Kind kind, Integer dir){
			this.kind = kind;
			this.dir = dir;
		}
		public ImageIcon getIcon(Component c){
			if (kind != null) return new ImageIcon(new SpriteTemplate(kind).getSpriteIcon(c, dir));
			else return new ImageIcon(Art.editorIcons[6][0]);
		}
	}
	
	@SuppressWarnings("serial")
	private class EnemyButton extends JToggleButton {
		public EnemyDef def;
		public EnemyButton(Component c, EnemyDef def){ 
			super(def.getIcon(c)); 
			setPreferredSize(new Dimension(48, 48));
			this.def = def;
		}
	}
	
	private static final EnemyDef[] enemyDefs;
	static {
		Enemy.Kind[] kinds = Enemy.Kind.values();
		enemyDefs = new EnemyDef[kinds.length+1];
		enemyDefs[0] = new EnemyDef(null, null);
		for (int i = 0; i < kinds.length; i++) enemyDefs[i+1] = new EnemyDef(kinds[i], 1);
	};
	
    public EnemyDef pickedDef;
    private ArrayList<EnemyButton> buttons;
    private LevelEditor levelEditor;

    public EnemyPicker(LevelEditor levelEditor){
    	this.levelEditor = levelEditor;
    }

    public void addNotify(){
        super.addNotify();
        Art.init(getGraphicsConfiguration(), null);
        
    	buttons = new ArrayList<EnemyButton>();
    	JPanel buttonPanel = new JPanel(new GridLayout(3, 12));
    	ButtonGroup bg = new ButtonGroup();
    	for (EnemyDef def: enemyDefs){
    		EnemyButton button = new EnemyButton(this, def);
    		bg.add(button);
    		buttons.add(button);
    		buttonPanel.add(button);
    		button.addActionListener(this);
    	}
    	
    	add(buttonPanel);
        setBorder(BorderFactory.createTitledBorder("Enemies"));
    }

    public void actionPerformed(ActionEvent e){ setPickedType(((EnemyButton)e.getSource()).def); }
    
    public void setPickedType(EnemyDef def){
        pickedDef = def;
        for (EnemyButton button: buttons) if (button.def == def) button.setSelected(true);
        if (levelEditor != null) levelEditor.enemyPicked();
    }
}