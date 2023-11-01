package com.mojang.mario.level;

import java.awt.*;
import java.awt.geom.Area;
import java.io.*;
import java.util.*;
import java.util.List;

import com.mojang.mario.*;
import com.mojang.mario.sprites.*;

import static com.mojang.mario.level.Level.*;

public class LevelArea{

	private short width, height;
    private Block[][] map;
    private Block[][] switchedMap;
    private short[][] xOffsets, yOffsets;
    private SpriteTemplate[][] spriteTemplates;
    private LevelGenerator.Background backgroundType;
    private String id;
    private Marker[][] markers;
    private boolean switched = false;
    private Area blockingArea;

    public LevelArea(String id, short width, short height){
    	this.id = id;
    	this.width = width;
        this.height = height;

        map = new Block[width][height];
        switchedMap = new Block[width][height];
        xOffsets = new short[width][height];
        yOffsets = new short[width][height];
        markers = new Marker[width][height];
        for (int i = 0; i < map.length; i++){
        	for (int j = 0; j < map[i].length; j++){
        		map[i][j] = new Block(Level.DEFAULT_BLOCK);
        		xOffsets[i][j] = 0;
        		yOffsets[i][j] = 0;
        	}
    	}
    
        spriteTemplates = new SpriteTemplate[width][height];
        backgroundType = LevelGenerator.Background.HILLS;

        blockingArea = new Area();
    }

    public static LevelArea load(DataInputStream dis) throws IOException{
    	String id = dis.readUTF();
    	short width = dis.readShort(), height = dis.readShort();
        LevelGenerator.Background bgType = LevelGenerator.Background.values()[dis.readShort()];
        LevelArea levelArea = new LevelArea(id, width, height);
        levelArea.backgroundType = bgType;

        for (short i = 0; i < width; i++)
            for (short j = 0; j < height; j++)
            	levelArea.setBlock(i, j, dis.readInt());
        
        int numST = dis.readInt();
        for (int i = 0; i < numST; i++){
        	int x = dis.readInt(), y = dis.readInt();
        	String type = dis.readUTF();
try{
        	levelArea.setSpriteTemplate(x, y, new SpriteTemplate(Enemy.Kind.valueOf(type)));
}catch(IllegalArgumentException e){}
        }

        int numMarkers = dis.readInt();
        for (int i = 0; i < numMarkers; i++){
        	String mid = dis.readUTF();
        	int mx = dis.readInt(), my = dis.readInt(), mwidth = dis.readInt(), mheight = dis.readInt();
        	Marker.Type type = Marker.Type.valueOf(dis.readUTF());
        	levelArea.setMarker(mx, my, new Marker(mid, mx, my, mwidth, mheight, type));
        }

        return levelArea;
    }

    public void save(DataOutputStream dos) throws IOException{
    	dos.writeUTF(id);
        dos.writeShort((short)width); //level width
        dos.writeShort((short)height); //level height
        dos.writeShort(backgroundType.ordinal()); //bg type ordinal

        int stCount = 0, markerCount = 0;
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++){
            	dos.writeInt(map[i][j].blockId); //block id
            	if (spriteTemplates[i][j] != null) stCount++;
            	if (markers[i][j] != null) markerCount++;
            }
        }

        dos.writeInt(stCount); //number of sprite templates
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++){
            	SpriteTemplate st = spriteTemplates[i][j];
            	if (st != null){
            		dos.writeInt(i); //sprite template x
            		dos.writeInt(j); //sprite template y
            		dos.writeUTF(st.kind.name()); //sprite template type id
            	}
            }
        }

        dos.writeInt(markerCount); //number of markers
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++){
            	Marker m = markers[i][j];
            	if (m != null){
            		dos.writeUTF(m.id);
            		dos.writeInt(m.x);
            		dos.writeInt(m.y);
            		dos.writeInt(m.width);
            		dos.writeInt(m.height);
            		dos.writeUTF(m.type.name());
            	}
            }
        }

    }

    public void tick(){
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++){
            	int oldX = xOffsets[x][y], oldY = yOffsets[x][y];
                boolean bouncy = Level.getBehavior(map[x][y].blockId).contains(Block.Trait.Bouncy);
            	
            	if (xOffsets[x][y] > 0) xOffsets[x][y]--;
                else if (xOffsets[x][y] < 0) xOffsets[x][y]++;
                if (yOffsets[x][y] > 0) yOffsets[x][y]--;
                else if (yOffsets[x][y] < 0) yOffsets[x][y]++;

                if (xOffsets[x][y] == 0 && oldX != 0 && bouncy){
                	if (oldX < 0) Mario.instance.getWorld().knockInto(x, y, false, false, false, true);
                	else Mario.instance.getWorld().knockInto(x, y, false, false, true, false);
                }
                if (yOffsets[x][y] == 0 && oldY != 0 && bouncy){
                	if (oldY < 0) Mario.instance.getWorld().knockInto(x, y, false, true, false, false);
                	else Mario.instance.getWorld().knockInto(x, y, true, false, false, false);
                }
            }
    }

    public boolean isSwitched(int flavor){ return switched; }
    
    public void activateSwitch(int flavor){
    	if (switched) return;
    	switched = true;
    	for (int x = 0; x < width; x++){
    		for (int y = 0; y < height; y++){
//    			if (powerups.get(map[x][y]) == Block.Powerup.None || 
//    					(map[x][y].blockId >= Level.COIN && map[x][y].blockId < Level.COIN+4))
    				switchedMap[x][y] = new Block(Level.getSwitchedBlockId(map[x][y].blockId));
//    			else 
//    				switchedMap[x][y] = map[x][y];
    		}
    	}
    }
    
    public void deactivateSwitch(int flavor){
    	if (!switched) return;
    	switched = false;
    	for (int x = 0; x < width; x++){
    		for (int y = 0; y < height; y++){
    			if (Level.DEFAULT_BLOCK == switchedMap[x][y].blockId) map[x][y] = switchedMap[x][y];
    			if (Level.RED_DEAD_BLOCK == switchedMap[x][y].blockId) map[x][y] = switchedMap[x][y];
    			if (Level.ORANGE_DEAD_BLOCK == switchedMap[x][y].blockId) map[x][y] = switchedMap[x][y];
    			if (Level.BLUE_DEAD_BLOCK == switchedMap[x][y].blockId) map[x][y] = switchedMap[x][y];

    			if (powerups[map[x][y].blockId] != null)
    				switchedMap[x][y] = null;
    		}
    	}
    }
    
    public Block getBlockCapped(int x, int y){
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x >= width) x = width - 1;
        if (y >= height) y = height - 1;
        
        if (switched) return switchedMap[x][y];
        else return map[x][y];
    }

    public Block getBlock(int x, int y){
        if (x < 0) x = 0;
        if (y < 0) return new Block(DEFAULT_BLOCK);
        if (x >= width) x = width - 1;
        if (y >= height) y = height - 1;

        if (switched) return switchedMap[x][y];
        else return map[x][y];
    }

    public void setBlock(int x, int y, int b){
        if (x < 0) return;
        if (y < 0) return;
        if (x >= width) return;
        if (y >= height) return;

        if (switched) switchedMap[x][y].blockId = b;
        else map[x][y].blockId = b;

        if (behaviors[b] == null) behaviors[b] = new Block.Trait[0];
        if (powerups[b] == null) powerups[b] =  Block.Powerup.None;

        List<Block.Trait> traits = Level.getBehavior(b);
        if (traits.contains(Block.Trait.BlockAll)){
            Rectangle tileArea = new Rectangle(x*16, y*16, 16, 16);
            blockingArea.add(new Area(tileArea));
        }
        else if (traits.contains(Block.Trait.SlideLeft)){
            int[] xs = new int[]{
                    x*16,
                    x*16 + 16,
                    x*16 + 16
            };
            int[] ys = new int[]{
                    y*16 + 16,
                    y*16 + 16,
                    y*16,
            };
            Polygon tileArea = new Polygon(xs, ys, 3);
            blockingArea.add(new Area(tileArea));
        }
        else if (traits.contains(Block.Trait.SlideRight)){
            int[] xs = new int[]{
                    x*16,
                    x*16 + 16,
                    x*16
            };
            int[] ys = new int[]{
                    y*16 + 16,
                    y*16 + 16,
                    y*16,
            };
            Polygon tileArea = new Polygon(xs, ys, 3);
            blockingArea.add(new Area(tileArea));
        }
        else { //if doesn't block at all
            Rectangle tileArea = new Rectangle(x*16, y*16, 16, 16);
            blockingArea.subtract(new Area(tileArea));
        }
    }

    public Area getBlockingArea(){ return blockingArea; }

    public boolean isBlocking(int x, int y, float xa, float ya, float rx, float ry, boolean noInvisible){
//        float nx = rx+xa, ny = ry+ya; //"real" coordinates after this move
//        boolean blocking = blockingArea.contains(nx, ny);

        Block block = getBlock(x, y);
        List<Block.Trait> traits = Level.getBehavior(block.blockId);
        boolean blocking = traits.contains(Block.Trait.BlockAll);
        blocking |= (ya > 0) && traits.contains(Block.Trait.BlockUpper);
        blocking |= (ya < 0) && traits.contains(Block.Trait.BlockLower);

        if (traits.contains(Block.Trait.SlideLeft)){
            int nx = (int)(rx+xa), ny = (int)(ry+ya); //"real" coordinates after this move
//            int nx = (int)(rx), ny = (int)(ry); //"real" coordinates after this move
            int nxo = nx-x*16, nyo = ny-y*16; // offsets from tile UL corner
//if (nyo > 8) blocking = true;
            if (nxo > 16-nyo) blocking = true;
//System.out.println(nxo+" "+nyo+" at "+x+","+y+" "+blocking);
        }
        if (traits.contains(Block.Trait.SlideRight)){
            int nx = (int)(rx+xa), ny = (int)(ry+ya); //"real" coordinates after this move
            int nxo = nx-x*16, nyo = ny-y*16; // offsets from tile UL corner
            if (nxo > nyo) blocking = true;
        }

        if (noInvisible && traits.contains(Block.Trait.Invisible)) blocking = false;
        return blocking;
    }

    public SpriteTemplate getSpriteTemplate(int x, int y){
    	SpriteTemplate ret = null;
        if (x < 0) ret = null;
        else if (y < 0) ret = null;
        else if (x >= width) ret = null;
        else if (y >= height) ret = null;
        else ret = spriteTemplates[x][y];
        return ret;
    }

    public void setSpriteTemplate(int x, int y, SpriteTemplate spriteTemplate){
        if (x < 0) return;
        if (y < 0) return;
        if (x >= width) return;
        if (y >= height) return;
        spriteTemplates[x][y] = spriteTemplate;
    }

    public Marker findMarker(String id, Marker.Type type){
    	for (int i = 0; i < width; i++)
    		for (int j = 0; j < height; j++)
    			if (markers[i][j] != null && markers[i][j].type == type && markers[i][j].id.equals(id))
    				return markers[i][j];
    	return null;
    }
    
    public Marker findMarker(Marker.Type type){
    	for (int i = 0; i < width; i++)
    		for (int j = 0; j < height; j++)
    			if (markers[i][j] != null && markers[i][j].type == type)
    				return markers[i][j];
    	return null;
    }
    
    public Marker getMarker(int x, int y, boolean strict){
    	Marker ret = null;
        if (x < 0) ret = null;
        else if (y < 0) ret = null;
        else if (x >= width) ret = null;
        else if (y >= height) ret = null;
        else ret = markers[x][y];

        if (!strict && ret == null)
	    	for (int i = 0; i < width; i++)
	    		for (int j = 0; j < height; j++)
	    			if (markers[i][j] != null)
	    				if (x >= i && x < i+markers[i][j].width/16 && y >= j && y < j+markers[i][j].height/16)
	    					return markers[i][j];
        
        return ret;
    }

    public void setMarker(int x, int y, Marker marker){
        if (x < 0) return;
        if (y < 0) return;
        if (x >= width) return;
        if (y >= height) return;
        markers[x][y] = marker;
    }

	public String getId(){ return id; }
	public void setId(String id){ this.id = id; }

	public short getWidth(){ return width; }
	public void setWidth(short width){ this.width = width; }

	public short getHeight(){ return height; }
	public void setHeight(short height){ this.height = height; }

	public short[][] getXOffsets(){ return xOffsets; }
	public void setXOffsets(short[][] offsets){ xOffsets = offsets; }

	public short[][] getYOffsets(){ return yOffsets; }
	public void setYOffsets(short[][] offsets){ yOffsets = offsets; }

	public LevelGenerator.Background getBackgroundType(){ return backgroundType; }
	public void setBackgroundType(LevelGenerator.Background backgroundType){ this.backgroundType = backgroundType; }
    
}