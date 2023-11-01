package com.mojang.mario.mapedit;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import com.mojang.mario.*;
import com.mojang.mario.level.*;

public class LevelEditor extends JFrame implements ActionListener, ItemListener, ListSelectionListener {
    private static final long serialVersionUID = 7461321112832160393L;

    private JButton loadButton, saveButton, saveAsButton, newButton, testButton;
    private JButton addArea, removeArea;
    private JToggleButton behaviorsButton;
    private JComboBox backgroundCombo;
    private JLabel nameLabel;
    private AreaEditPanel levelEditView;
    private JFileChooser fileChooser;
    private JList areaList;

    public static enum Mode { TILE, MARKER, ENEMY };
    public Mode mode = Mode.TILE;
    public EnemyPicker enemyPicker;
    private MarkerPicker markerPicker;
    private TilesetEditor tilesetEditor;
    
    public static void main(String[] args){ new LevelEditor().setVisible(true); }

    @SuppressWarnings("serial")
	public LevelEditor(){
        super("Map Edit");

        try { Level.loadBehaviors(new DataInputStream(new FileInputStream(MarioComponent.TILE_BEHAVIOR_FILENAME))); }
        catch(IOException e){ e.printStackTrace(); }
        
        setSize(1200, 800);
        setLocation(20, 20);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //level edit area
        levelEditView = new AreaEditPanel(this);
        areaList = new JList();
        areaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final AreaEditPanel lev = levelEditView;
        areaList.setCellRenderer(new DefaultListCellRenderer(){
        	public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean focused){
        		JLabel ret = (JLabel)super.getListCellRendererComponent(list, value, index, selected, focused);
        		ret.setText(((LevelArea)value).getId());
        		if (lev.getLevel().getStartAreaIndex() == index){
        			if (selected || focused) ret.setBackground(new Color(100, 100, 255));
        			else ret.setBackground(new Color(200, 200, 255));
        		}
        		return ret;
        	}
        });
        addArea = new JButton("Add Area");
        removeArea = new JButton("Remove Area");
        areaList.addListSelectionListener(this);
        addArea.addActionListener(this);
        removeArea.addActionListener(this);
        JPanel areaButtonsPanel = new JPanel(new GridLayout(2, 1));
        areaButtonsPanel.add(addArea);
        areaButtonsPanel.add(removeArea);
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(areaList, BorderLayout.CENTER);
        listPanel.add(areaButtonsPanel, BorderLayout.SOUTH);
        listPanel.setBorder(BorderFactory.createTitledBorder("Areas"));
        
        JPanel levelPanel = new JPanel(new BorderLayout());
        levelPanel.add(listPanel, BorderLayout.WEST);
        levelPanel.add(new JScrollPane(levelEditView), BorderLayout.CENTER);

        //lower area
        tilesetEditor = new TilesetEditor(this);
        enemyPicker = new EnemyPicker(this);
        markerPicker = new MarkerPicker(this);

        JPanel lowerRightPanel = new JPanel(new GridLayout(2, 1));
        lowerRightPanel.add(enemyPicker);
        lowerRightPanel.add(markerPicker);
        JPanel lowerPanel = new JPanel(new GridLayout(1, 2));
        lowerPanel.add(tilesetEditor);
        lowerPanel.add(lowerRightPanel);

        //put it all together
        JPanel borderPanel = new JPanel(new BorderLayout());
        borderPanel.add(BorderLayout.CENTER, levelPanel);
        borderPanel.add(BorderLayout.SOUTH, lowerPanel);
        borderPanel.add(BorderLayout.NORTH, buildButtonPanel());
        setContentPane(borderPanel);

        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("/Users/patrick/Documents/workspace/Mario/bin/"));
    }

    public int getPickedBlockId(){ return tilesetEditor.getPickedBlockId(); }
    
    public Container buildButtonPanel(){
        loadButton = new JButton("Load");
        saveButton = new JButton("Save");
        saveAsButton = new JButton("Save As");
        newButton = new JButton("New");
        testButton = new JButton("Test");
        behaviorsButton = new JToggleButton("Render Behaviors", false);
        nameLabel = new JLabel("        ---        ");
        
        DefaultComboBoxModel bgModel = new DefaultComboBoxModel(LevelGenerator.Background.values());
        backgroundCombo = new JComboBox(bgModel);
        
        loadButton.addActionListener(this);
        saveButton.addActionListener(this);
        saveAsButton.addActionListener(this);
        newButton.addActionListener(this);
        testButton.addActionListener(this);
        behaviorsButton.addActionListener(this);
        backgroundCombo.addItemListener(this);
        
        Box panel = Box.createHorizontalBox();
        panel.add(loadButton);
        panel.add(saveButton);
        panel.add(saveAsButton);
        panel.add(newButton);
        panel.add(testButton);
        panel.add(Box.createHorizontalGlue());
        panel.add(behaviorsButton);
        panel.add(Box.createHorizontalGlue());
        panel.add(nameLabel);
        panel.add(Box.createHorizontalGlue());
        panel.add(backgroundCombo);
        return panel;
    }

    public void actionPerformed(ActionEvent e){
        try{
            if (e.getSource() == loadButton) load();
            else if (e.getSource() == saveButton) save(false);
            else if (e.getSource() == saveAsButton) save(true);
            else if (e.getSource() == newButton) newLevel();
            else if (e.getSource() == testButton) test();
            else if (e.getSource() == addArea) addArea();
            else if (e.getSource() == removeArea) removeArea();
            else if (e.getSource() == behaviorsButton) toggleBehaviors();
        }
        catch (Exception ex){
        	ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.toString(), "Operation failed.", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void itemStateChanged(ItemEvent e){
    	levelEditView.getLevel().setBackgroundType((LevelGenerator.Background)backgroundCombo.getSelectedItem());
    }
   
    public void valueChanged(ListSelectionEvent e){
    	if (e.getValueIsAdjusting()) return;
    	if (areaList.getSelectedIndex() < 0) return;
    	levelEditView.getLevel().setCurrentArea((LevelArea)areaList.getSelectedValue());
    	levelEditView.setLevel(levelEditView.getLevel());
        backgroundCombo.setSelectedItem(levelEditView.getLevel().getBackgroundType());
        for (int i = 0; i < levelEditView.getLevel().getWidth(); i++)
        	for (int j = 0; j < levelEditView.getLevel().getHeight(); j++)
        		if (levelEditView.getLevel().getSpriteTemplate(i, j) != null)
        			levelEditView.getLevel().getSpriteTemplate(i, j).spawn(null, i, j, 0);
    }
    
    private void refreshAreaList(){
    	DefaultListModel model = new DefaultListModel();
    	for (LevelArea area: levelEditView.getLevel().getAreas()) model.addElement(area);
    	areaList.setModel(model);
    	areaList.setSelectedValue(levelEditView.getLevel().getCurrentArea(), true);
        backgroundCombo.setSelectedItem(levelEditView.getLevel().getBackgroundType());
    }
    
    private void load() throws IOException {
    	if (JFileChooser.CANCEL_OPTION == fileChooser.showOpenDialog(this)) return;
        levelEditView.setLevel(Level.load(new DataInputStream(new FileInputStream(fileChooser.getSelectedFile()))));
        refreshAreaList();
        for (int i = 0; i < levelEditView.getLevel().getWidth(); i++)
        	for (int j = 0; j < levelEditView.getLevel().getHeight(); j++)
        		if (levelEditView.getLevel().getSpriteTemplate(i, j) != null)
        			levelEditView.getLevel().getSpriteTemplate(i, j).spawn(null, i, j, 0);
        nameLabel.setText(fileChooser.getSelectedFile().getName());
    }
    
    private void save(boolean saveAs) throws IOException {
    	if (fileChooser.getSelectedFile() == null || saveAs)
    		if (JFileChooser.CANCEL_OPTION == fileChooser.showSaveDialog(this)) return;
    	levelEditView.getLevel().save(new DataOutputStream(new FileOutputStream(fileChooser.getSelectedFile())));
    }
    
    private void newLevel(){
    	LevelArea defaultArea = showNewAreaDialog(this);
    	if (defaultArea == null) return;
    	
    	Level level = new Level();
    	level.addArea(defaultArea);
    	level.setCurrentArea(defaultArea);
    	levelEditView.setLevel(level);
    	refreshAreaList();
        for (int i = 0; i < levelEditView.getLevel().getWidth(); i++)
        	for (int j = 0; j < levelEditView.getLevel().getHeight(); j++)
        		if (levelEditView.getLevel().getSpriteTemplate(i, j) != null)
        			levelEditView.getLevel().getSpriteTemplate(i, j).spawn(null, i, j, 0);
    }

    private LevelArea showNewAreaDialog(Component c){
    	JPanel inputPanel = new JPanel(new GridLayout(3, 2));
    	JTextField width = new JTextField(), height = new JTextField(), id = new JTextField();
    	inputPanel.add(new JLabel("ID:"));
    	inputPanel.add(id);
    	inputPanel.add(new JLabel("Width:"));
    	inputPanel.add(width);
    	inputPanel.add(new JLabel("Height:"));
    	inputPanel.add(height);

    	if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(c, inputPanel, "New Level", JOptionPane.OK_CANCEL_OPTION))
    		return new LevelArea(id.getText().trim(), Short.parseShort(width.getText()), Short.parseShort(height.getText()));
    	else return null;
    }

    private void addArea(){
    	LevelArea area = showNewAreaDialog(this);
    	if (area == null) return;
    	levelEditView.getLevel().addArea(area);
    	refreshAreaList();
    }
    
    private void removeArea(){
    	if (JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog(this, "You sure you want to delete this area?")) return;
    	levelEditView.getLevel().removeArea(levelEditView.getLevel().getCurrentArea());
    	refreshAreaList();
    }
    
    private void toggleBehaviors(){
    	LevelRenderer.renderBehaviors = !LevelRenderer.renderBehaviors;
    	repaint();
    }
    
    private void test(){
/*
    	try{ Process p = Runtime.getRuntime().exec("java FrameLauncher "+fileChooser.getSelectedFile().getAbsolutePath()); }
    	catch(IOException e){ e.printStackTrace(); }
*/
    	com.mojang.mario.FrameLauncher.main(new String[]{"", "", fileChooser.getSelectedFile().getAbsolutePath()});
    }
    
    public Marker.Type getSelectedMarkerType(){ return markerPicker.getSelectedMarkerType(); }
    
    public void setPickedBlockId(int pickedBlockId){ tilesetEditor.setPickedBlockId(pickedBlockId); }
    
    public void tilePicked(){ mode = Mode.TILE; }
    public void enemyPicked(){ mode = Mode.ENEMY; }
    public void markerPicked(){ mode = Mode.MARKER; }
}