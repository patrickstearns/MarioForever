package com.mojang.mario.level;

import java.io.*;
import java.util.*;
import com.mojang.mario.*;

public class Level{
	
    public static final int FILE_HEADER = 0x271c4178;
    static Block.Trait[][] behaviors;
    static Block.Powerup[] powerups;

    //special tile constants
    public static final int MAX_BLOCK_ID = 48*16; //really is max+1
    
    //whole rows
    public static final int DEFAULT_BLOCK = 0;
    public static final int ORANGE_DEAD_BLOCK = 16;
    public static final int RED_DEAD_BLOCK = 32;
    public static final int BLUE_DEAD_BLOCK = 48;
    public static final int ORANGE_BRICK = 64;
    public static final int RED_BRICK = 80;
    public static final int BLUE_BRICK = 96;
    public static final int ORANGE_BLOCK = 112;
    public static final int RED_BLOCK = 128;
    public static final int BLUE_BLOCK = 144;
    public static final int NOTE_BLOCK = 160;

    //chunks of four
    public static final int WOOD_BLOCK = 176;
    public static final int VINE_BLOCK = 188;
    public static final int COIN = 192;
    public static final int BLUE_COIN = 196;
    public static final int GHOST_COIN = 208; //just below regular coins
    public static final int WATER_COIN = 252;
    public static final int WATER = 43*16;

    //goalposts
    public static final int GOAL_0_POST = 217;
    public static final int GOAL_0_CAP = 201;
    public static final int GOAL_0_BAND = 200;
    public static final int GOAL_1_POST = 218;
    public static final int GOAL_1_CAP = 202;
    public static final int GOAL_1_BAND = 216;

    //single blocks
    public static final int BULLET_BILL_CANNON = 203;
    public static final int FIRE_BAR_BLOCK = 187;

    //win door
    public static final int DOOR_TOP = 38*16;
    public static final int DOOR_BOTTOM = 38*16;
    public static final int DOOR_SHADOW_TOP = 39*16;
    public static final int DOOR_SHADOW_BOTTOM = 40*16;
    
    //true if is unbumpable plain block
    public static boolean isDeadBlockId(int blockId){
    	return (blockId >= ORANGE_DEAD_BLOCK && blockId < BLUE_DEAD_BLOCK+16) ||
    		blockId == WOOD_BLOCK || blockId == NOTE_BLOCK;
    }
    
    //the 'dead' block for anything bumpable
    public static int getDeadBlockId(int blockId){
		int ret = blockId;
		if (blockId > DEFAULT_BLOCK && blockId < ORANGE_DEAD_BLOCK) ret = ORANGE_DEAD_BLOCK;
		else if (blockId >= WOOD_BLOCK && blockId < WOOD_BLOCK+4) ret = WOOD_BLOCK;
    	else if (blockId >= NOTE_BLOCK && blockId < NOTE_BLOCK+16) ret = NOTE_BLOCK;
    	else if (blockId >= ORANGE_BLOCK && blockId < ORANGE_BLOCK+16) ret = ORANGE_DEAD_BLOCK;
    	else if (blockId >= ORANGE_BRICK && blockId < ORANGE_BRICK+16) ret = ORANGE_DEAD_BLOCK;
    	else if (blockId >= RED_BLOCK && blockId < RED_BLOCK+16) ret = RED_DEAD_BLOCK;
    	else if (blockId >= RED_BRICK && blockId < RED_BRICK+16) ret = RED_DEAD_BLOCK;
    	else if (blockId >= BLUE_BLOCK && blockId < BLUE_BLOCK+16) ret = BLUE_DEAD_BLOCK;
    	else if (blockId >= BLUE_BRICK && blockId < BLUE_BRICK+16) ret = BLUE_DEAD_BLOCK;
    	else if (blockId >= WATER_COIN && blockId < WATER_COIN+4) ret = WATER;
		return ret;
    }
    
    public static int getSwitchedBlockId(int blockId){
    	int ret = blockId;
    	if (blockId == ORANGE_BRICK || blockId == RED_BRICK || blockId == BLUE_BRICK) ret = COIN;
    	else if (blockId >= GHOST_COIN && blockId < GHOST_COIN+4) ret = BLUE_COIN;
    	else if (blockId >= COIN && blockId < COIN+4) ret = ORANGE_BRICK;
    	else if (blockId >= WATER_COIN && blockId < WATER_COIN+4) ret = BLUE_BRICK;

    	return ret;
    }
    
    public static int songForLevelType(LevelGenerator.Background levelType){
        if (levelType==LevelGenerator.Background.HILLS) return Art.SONG_OVERWORLD_3;
        else if (levelType==LevelGenerator.Background.SNOW) return Art.SONG_OVERWORLD_2;
        else if (levelType==LevelGenerator.Background.UNDERGROUND) return Art.SONG_UNDERGROUND_2;
        else if (levelType==LevelGenerator.Background.UNDERGROUND_2) return Art.SONG_UNDERGROUND_1;
        else if (levelType==LevelGenerator.Background.CASTLE) return Art.SONG_FORTRESS;
        else if (levelType==LevelGenerator.Background.UNDERWATER) return Art.SONG_UNDERWATER;
        else if (levelType==LevelGenerator.Background.LAVA) return Art.SONG_AIRSHIP;
        else if (levelType==LevelGenerator.Background.DESERT) return Art.SONG_OVERWORLD_1;
        else if (levelType==LevelGenerator.Background.SKY) return Art.SONG_SKY;
        else if (levelType==LevelGenerator.Background.BOSS) return Art.SONG_BOWSER;
        return -1;
    }
    
    private ArrayList<LevelArea> areas;
    private LevelArea currentArea;
    private int startAreaIndex;
    
    public Level(){
        if (behaviors == null) behaviors = new Block.Trait[MAX_BLOCK_ID][];
        if (powerups == null) powerups = new Block.Powerup[MAX_BLOCK_ID];
        areas = new ArrayList<LevelArea>();
        startAreaIndex = 0;
    }
    
    public Level(short width, short height){
    	this();
    	addArea(new LevelArea("Default", width, height));
    }

    public void addArea(LevelArea area){
    	areas.add(area);
    	if (areas.size() == 1) currentArea = area;
    }
    
    public void removeArea(LevelArea area){
    	areas.remove(area);
    }
    
    public static void loadBehaviors(DataInputStream dis) throws IOException {
    	behaviors = new Block.Trait[MAX_BLOCK_ID][];
    	for (int i = 0; i < behaviors.length; i++) behaviors[i] = new Block.Trait[0];
    	int mapSize = dis.readInt();
    	for (int i = 0; i < mapSize; i++){ //size of map
        	int blockId = dis.readInt(); //block Id
        	int traitsSize = dis.readInt(); //size of traits list
        	Block.Trait[] traits = new Block.Trait[traitsSize];
        	for (int j = 0; j < traitsSize; j++){
       			String traitname = dis.readUTF();
        		try{ traits[j] = Block.Trait.valueOf(traitname); } //trait name
        		catch(IllegalArgumentException e){ System.out.println("Warning: unrecognized tile trait name: "+traitname); }
        	}
        	behaviors[blockId] = traits;
        }
    	
    	powerups = new Block.Powerup[MAX_BLOCK_ID];
    	for (int i = 0; i < powerups.length; i++) powerups[i] = Block.Powerup.None;
    	int pmapSize = dis.readInt(); //size of map
    	for (int i = 0; i < pmapSize; i++){
    		int blockId = dis.readInt(); //blockId;
    		Block.Powerup powerup = Block.Powerup.valueOf(dis.readUTF());
    		powerups[blockId] = powerup;
    	}
    }
    
    public static void saveBehaviors(DataOutputStream dos) throws IOException {
    	dos.writeInt(behaviors.length); //size of behaviors map
        for (int b = 0; b < behaviors.length; b++){
        	Block.Trait[] traits = behaviors[b];
        	dos.writeInt(b); //block Id
        	dos.writeInt(traits.length); //size of traits list
        	for (int i = 0; i < traits.length; i++){
        		dos.writeUTF(traits[i].name()); //trait name
        	}
        }
        
        dos.writeInt(powerups.length);
        for (int b = 0; b < powerups.length; b++){
        	dos.writeInt(b);
        	dos.writeUTF(powerups[b].name());
        }
    }
    
    public static Level load(DataInputStream dis) throws IOException{
        long header = dis.readLong(); //header
        if (header != Level.FILE_HEADER) throw new IOException("Bad level header");

        Level level = new Level();
        level.setStartAreaIndex(dis.readInt());
        int numAreas = dis.readInt();
        for (int i = 0; i < numAreas; i++) level.addArea(LevelArea.load(dis));
        level.setCurrentArea(level.getAreas().get(level.getStartAreaIndex()));
        
        return level;
    }

    public void save(DataOutputStream dos) throws IOException{
        dos.writeLong(Level.FILE_HEADER); //header
        dos.writeInt(startAreaIndex);
        dos.writeInt(areas.size());
        for (LevelArea area: areas) area.save(dos);
    }

    public void tick(){ currentArea.tick(); }

    public Block getBlockCapped(int x, int y){ return currentArea.getBlockCapped(x, y); }
    public Block getBlock(int x, int y){ return currentArea.getBlock(x, y); }
    public void setBlock(int x, int y, int b){ currentArea.setBlock(x, y, b); }
    public boolean isBlocking(int x, int y, float xa, float ya, float rx, float ry){ return currentArea.isBlocking(x, y, xa, ya, rx, ry, true); }
    public boolean isBlocking(int x, int y, float xa, float ya, float rx, float ry, boolean noInvisible){ return currentArea.isBlocking(x, y, xa, ya, rx, ry, noInvisible); }
    public SpriteTemplate getSpriteTemplate(int x, int y){ return currentArea.getSpriteTemplate(x, y); }
    public void setSpriteTemplate(int x, int y, SpriteTemplate spriteTemplate){ currentArea.setSpriteTemplate(x, y, spriteTemplate); }

    public static ArrayList<Block.Trait> getBehavior(int blockId){ 
    	Block.Trait[] traits = behaviors[blockId];
    	if (traits == null){
    		traits = new Block.Trait[0];
    		behaviors[blockId] = traits;
    	}
    	ArrayList<Block.Trait> ret = new ArrayList<Block.Trait>(traits.length);
    	for (int i = 0; i < traits.length; i++) ret.add(traits[i]);
    	
    	return ret;
    }
    public static void setBehavior(int blockId, Block.Trait[] traits){ behaviors[blockId] = traits; }

    public static Block.Powerup getPowerup(int blockId){ 
    	Block.Powerup ret = powerups[blockId];
    	if (ret == null){
    		ret = Block.Powerup.None;
    		powerups[blockId] = ret;
    	}
    	return ret;
    }
    public static void setPowerup(int blockId, Block.Powerup powerup){ powerups[blockId] = powerup; }

    private int switchSetCount = 0;
    
    public void setSwitch(int flavor){
    	//restart music anytime we hit a new switch
    	Art.startMusic(Art.SONG_POW_SWITCH);

    	//but only do anything if this if the first switch we've hit
    	if (switchSetCount == 0){
	    	if (currentArea.isSwitched(flavor)) return;
	    	getCurrentArea().activateSwitch(flavor);
    	}
    	
    	switchSetCount++;
    }

    public void unsetSwitch(int flavor){
    	if (switchSetCount == 0) return; //short circuit if nothing is actually switched
    	
    	switchSetCount--;

    	//only actually unset if this is last switch unsetting
    	if (switchSetCount == 0){
	    	Art.startMusic(Level.songForLevelType(getCurrentArea().getBackgroundType()));
	    	
	    	if (!currentArea.isSwitched(flavor)) return;
	    	for (LevelArea area: getAreas()){
	    		area.deactivateSwitch(flavor);
	    	}
    	}
    }
    
	public ArrayList<LevelArea> getAreas(){ return areas; }
	public void setAreas(ArrayList<LevelArea> areas){ this.areas = areas; }

	public LevelArea getCurrentArea(){ return currentArea; }
	public void setCurrentArea(LevelArea currentArea){ this.currentArea = currentArea; }

	public int getStartAreaIndex(){ return startAreaIndex; }
	public void setStartAreaIndex(int startAreaIndex){ this.startAreaIndex = startAreaIndex; }

	public short getWidth(){ return currentArea.getWidth(); }
	public void setWidth(short width){ currentArea.setWidth(width); }

	public short getHeight(){ return currentArea.getHeight(); }
	public void setHeight(short height){ currentArea.setHeight(height); }

	public short[][] getXOffsets(){ return currentArea.getXOffsets(); }
	public void setXOffsets(short[][] offsets){ currentArea.setXOffsets(offsets); }

	public short[][] getYOffsets(){ return currentArea.getYOffsets(); }
	public void setYOffsets(short[][] offsets){ currentArea.setYOffsets(offsets); }

	public Marker getMarker(int x, int y, boolean strict){ return currentArea.getMarker(x, y, strict); }
	public void setMarker(int x, int y, Marker marker){ currentArea.setMarker(x, y, marker); }
	
	public void setStartPos(int x, int y){
		for (LevelArea area: areas)
			for (int i = 0; i < area.getWidth(); i++)
				for (int j = 0; j < area.getHeight(); j++)
					if (area.getMarker(i, j, true) != null && area.getMarker(i, j, true).type == Marker.Type.START_POS)
						area.setMarker(i, j, null);
		setMarker(x, y, new Marker("Start Position", x, y, 16, 16, Marker.Type.START_POS));
		setStartAreaIndex(areas.indexOf(currentArea));
	}
	
	public void setEndPos(int x, int y){
		for (LevelArea area: areas)
			for (int i = 0; i < area.getWidth(); i++)
				for (int j = 0; j < area.getHeight(); j++)
					if (area.getMarker(i, j, true) != null && area.getMarker(i, j, true).type == Marker.Type.END_POS)
						area.setMarker(i, j, null);
		setMarker(x, y, new Marker("End Position", x, y, 16, 16, Marker.Type.END_POS));
	}
	
	public LevelArea findAreaForPortal(String id, boolean findExits){
		for (LevelArea area: areas)
			for (int i = 0; i < area.getWidth(); i++)
				for (int j = 0; j < area.getHeight(); j++)
					if (area.getMarker(i, j, true) != null && area.getMarker(i, j, true).id.equals(id)){
						if (findExits && area.getMarker(i, j, true).isExit()) return area;
						if (!findExits && area.getMarker(i, j, true).isEntrance()) return area;
					}
		return null;
	}
	
	public Marker findEntranceForExit(String id){
		for (LevelArea area: areas)
			for (int i = 0; i < area.getWidth(); i++)
				for (int j = 0; j < area.getHeight(); j++)
					if (area.getMarker(i, j, true) != null && area.getMarker(i, j, true).id.equals(id))
						if (area.getMarker(i, j, true).isEntrance()) 
							return area.getMarker(i, j, true);
		return null;
	}
	
	public LevelGenerator.Background getBackgroundType(){ return currentArea.getBackgroundType(); }
	public void setBackgroundType(LevelGenerator.Background backgroundType){ currentArea.setBackgroundType(backgroundType); }
    
}