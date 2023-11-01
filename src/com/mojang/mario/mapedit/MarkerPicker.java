package com.mojang.mario.mapedit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.mojang.mario.Art;
import com.mojang.mario.level.*;

@SuppressWarnings("serial")
public class MarkerPicker extends JPanel implements ActionListener {

	private ButtonGroup buttonGroup;
	private LevelEditor levelEditor;
    
    public MarkerPicker(LevelEditor levelEditor){
    	super();
    	this.levelEditor = levelEditor;
    }
    
    public void addNotify(){
    	super.addNotify();
    	buttonGroup = new ButtonGroup();
    	setLayout(new GridLayout(2, 16));

		JToggleButton xbutton = new JToggleButton(new ImageIcon(Art.editorIcons[6][0]));
		xbutton.addActionListener(this);
		xbutton.setActionCommand(null);
		buttonGroup.add(xbutton);
		add(xbutton);
    	
    	for (Marker.Type type: Marker.Type.values()){
    		JToggleButton button = new JToggleButton(type.getEditorIcon());
    		button.addActionListener(this);
    		button.setActionCommand(type.name());
    		buttonGroup.add(button);
    		add(button);
    	}
    	setBorder(BorderFactory.createTitledBorder("Markers"));
    }
    
    public void actionPerformed(ActionEvent e){ levelEditor.markerPicked(); }

    public Marker.Type getSelectedMarkerType(){
    	ButtonModel selectedModel = buttonGroup.getSelection();
    	if (selectedModel == null) return null;
    	else if (selectedModel.getActionCommand() == null) return null;
    	else return Marker.Type.valueOf(selectedModel.getActionCommand());
    }
    
}
