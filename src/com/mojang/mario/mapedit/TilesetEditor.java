package com.mojang.mario.mapedit;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import com.mojang.mario.*;
import com.mojang.mario.level.*;

@SuppressWarnings("serial")
public class TilesetEditor extends JPanel {

    private JCheckBox[] bitmapCheckboxes = new JCheckBox[Block.Trait.values().length];
    private JRadioButton[] powerupButtons = new JRadioButton[Block.Powerup.values().length];
    private TilePicker tilePicker;
    
    public TilesetEditor(LevelEditor levelEditor){
    	super(new BorderLayout());
    	
    	//tile picker
    	tilePicker = new TilePicker(levelEditor, this);
        JScrollPane tilePickerJsp = new JScrollPane(tilePicker);
        tilePickerJsp.getViewport().setPreferredSize(new Dimension(256, 300));

        //buttons - traits, then powerups
        JPanel buttonPanel = new JPanel(new GridLayout(15, 2));
        int i = 0;
        for (i = 0; i < Block.Trait.values().length; i++){
        	Block.Trait trait = Block.Trait.values()[i];
        	bitmapCheckboxes[i] = new JCheckBox(trait.name());
            buttonPanel.add(bitmapCheckboxes[i]);
            
            bitmapCheckboxes[i].addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                	if (((JCheckBox)e.getSource()).isSelected()){
                        ArrayList<Block.Trait> traits = Level.getBehavior(tilePicker.getPickedBlockId());
                        traits.add(Block.Trait.valueOf(e.getActionCommand()));
                        Level.setBehavior(tilePicker.getPickedBlockId(), traits.toArray(new Block.Trait[traits.size()]));
                	}
                	else {
                		ArrayList<Block.Trait> traits = Level.getBehavior(tilePicker.getPickedBlockId());
                		traits.remove(Block.Trait.valueOf(e.getActionCommand()));
                		Level.setBehavior(tilePicker.getPickedBlockId(), traits.toArray(new Block.Trait[traits.size()]));
                	}
                	
			        try {
                        File saveFile = new File(MarioComponent.TILE_BEHAVIOR_FILENAME);
			        	Level.saveBehaviors(new DataOutputStream(new FileOutputStream(saveFile)));
			        }
			        catch(IOException exc){ exc.printStackTrace(); }
                }
            });
        }
        if (i%2 == 1) buttonPanel.add(new JLabel(" "));
        ButtonGroup powerupButtonGroup = new ButtonGroup();
        for (i = 0; i < Block.Powerup.values().length; i++){
        	Block.Powerup powerup = Block.Powerup.values()[i];
        	powerupButtons[i] = new JRadioButton(powerup.name());
            powerupButtonGroup.add(powerupButtons[i]);
            buttonPanel.add(powerupButtons[i]);
            
            powerupButtons[i].addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                	if (((JRadioButton)e.getSource()).isSelected())
                		Level.setPowerup(tilePicker.getPickedBlockId(), Block.Powerup.valueOf(e.getActionCommand()));
			        try { Level.saveBehaviors(new DataOutputStream(new FileOutputStream(MarioComponent.TILE_BEHAVIOR_FILENAME))); }
			        catch(IOException exc){ exc.printStackTrace(); }
                }
            });
        }
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        
        add(BorderLayout.WEST, tilePickerJsp);
        add(BorderLayout.CENTER, buttonPanel);
        setBorder(BorderFactory.createTitledBorder("Tiles"));
    }
    
    public void tilePicked(){
    	int pickedBlockId = tilePicker.getPickedBlockId();
		ArrayList<Block.Trait> traits = Level.getBehavior(pickedBlockId);
		Block.Powerup powerup = Level.getPowerup(pickedBlockId);
    	for (JCheckBox box: bitmapCheckboxes)
    		box.setSelected(traits.contains(Block.Trait.valueOf(box.getActionCommand())));
    	for (JRadioButton button: powerupButtons)
    		button.setSelected(powerup == (Block.Powerup.valueOf(button.getActionCommand())));
    }
	
    public int getPickedBlockId(){ return tilePicker.getPickedBlockId(); }
    public void setPickedBlockId(int id){ tilePicker.setPickedBlockId(id); }
}
