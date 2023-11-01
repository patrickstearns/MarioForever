package com.mojang.mario;

import java.io.*;
import java.util.*;
import com.mojang.mario.level.*;

public class LevelOrder {

	//creates a level order file from internal variables
	public static void main(String[] args){
		String gameDirName = "main";
		String[][] levelPointers = new String[][]{
				{"1-1.lvl","1-2.lvl","1-3.lvl","1-4.lvl"},	
				{"2-1.lvl","2-2.lvl","2-3.lvl","2-4.lvl"},	
				{"3-1.lvl","3-2.lvl","3-3.lvl","3-4.lvl"},	
				{"4-1.lvl","4-2.lvl","4-3.lvl","4-4.lvl"},	
				{"5-1.lvl","5-2.lvl","5-3.lvl","5-4.lvl"},	
		};
		String[] messages = new String[]{
			"Sorry Mario, but our princessis in another castle!",
			"Sorry Mario, but she's not   here, either.",
			"Nope...still no princess.",
			"Sorry Mario, but the princesswas just here!",
			"Hooray, I'm rescued!         Thank you, Mario!",
		};
		
		ArrayList<LevelPointer> pointers = new ArrayList<LevelPointer>();
		int m = 0;
		for (int i = 0; i < levelPointers.length; i++){
			for (int j = 0; j < levelPointers[i].length; j++){
				LevelPointer pointer = new LevelPointer();
				pointer.worldIndex = i;
				pointer.levelIndex = j;
				pointer.filename = MarioComponent.GAME_DATA_DIR+gameDirName+"/"+levelPointers[i][j];
				pointers.add(pointer);
				
				if (j == levelPointers[i].length-1){
					LevelPointer p = new LevelPointer();
					p.worldIndex = i;
					p.levelIndex = j+1;
					p.talkMessage = messages[m];
					pointers.add(p);
					m++;
				}
			}
		}

		LevelOrder order = new LevelOrder();
		order.pointers = pointers;
		
		try{
			String filename = MarioComponent.GAME_DATA_DIR+"/"+gameDirName+"/levels.txt";
			order.save(new DataOutputStream(new FileOutputStream(filename)));
			System.out.println("Level order file generated at "+filename);
		}
		catch(IOException e){
			System.err.println("Error writing level order file:");
			e.printStackTrace();
		}
	}
	
	public static LevelOrder loadLevelOrder(String levelOrderFilename) throws IOException {
		LevelOrder order = new LevelOrder();
		order.load(new DataInputStream(LevelOrder.class.getResourceAsStream(levelOrderFilename)));
		return order;
	}

	private static class LevelPointer {
		public String filename;
		public int worldIndex;
		public int levelIndex;
		public String talkMessage;
	}
	
	private ArrayList<LevelPointer> pointers;

	private LevelOrder(){}

	public boolean isLevelTalk(int index){ return pointers.get(index).talkMessage != null; }
	public String getLevelFilename(int index){ return pointers.get(index).filename; }
	public String getLevelMessage(int index){ return pointers.get(index).talkMessage; }
	public int getWorldIndex(int index){ return pointers.get(index).worldIndex; }
	public int getLevelIndex(int index){ return pointers.get(index).levelIndex; }
	public int getLevelCount(){ return pointers.size(); }
	
	private void load(DataInputStream dis) throws IOException {
		pointers = new ArrayList<LevelPointer>();
		
		long header = dis.readLong(); //header
        if (header != Level.FILE_HEADER) throw new IOException("Bad level header reading level ordering file.");

        int numWorlds = dis.readInt();
        int currentWorld = 1;
        for (int i = 0; i < numWorlds; i++){
        	int numLevels = dis.readInt();
        	for (int j = 0; j < numLevels; j++){
	        	LevelPointer pointer = new LevelPointer();
	        	pointer.worldIndex = currentWorld;
	        	pointer.levelIndex = j+1;
	        	pointer.filename = dis.readUTF();
	        	pointer.talkMessage = dis.readUTF();
	        	if ("null".equals(pointer.filename)) pointer.filename = null;
	        	if ("null".equals(pointer.talkMessage)) pointer.talkMessage = null;
	        	pointers.add(pointer);
        	}
        	currentWorld++;
        }
	}
	
	//public but should only be used by editor!
	public void save(DataOutputStream dos) throws IOException {
		dos.writeLong(Level.FILE_HEADER); //file header
		
		int numWorlds = getNumWorlds();
		dos.writeInt(numWorlds);
		for (int i = 0; i < numWorlds; i++){
			ArrayList<LevelPointer> worldPointers = getPointersForWorld(i);
			dos.writeInt(worldPointers.size());
			for (int j = 0; j < worldPointers.size(); j++){
				if (worldPointers.get(j).filename == null) dos.writeUTF("null");
				else dos.writeUTF(worldPointers.get(j).filename);

				if (worldPointers.get(j).talkMessage == null) dos.writeUTF("null");
				else dos.writeUTF(worldPointers.get(j).talkMessage);
			}
		}
	}

	//really just finds the highest world index and returns that+1
	public int getNumWorlds(){
		int highest = 0;
		for (LevelPointer pointer: pointers){
			if (pointer.worldIndex > highest) highest = pointer.worldIndex;
		}
		return highest+1;
	}
	
	public int getNumLevels(int world){
		int count = 0;
		for (LevelPointer pointer: pointers){
			if (pointer.worldIndex == world) count++;
		}
		return count;
	}
	
	public ArrayList<LevelPointer> getPointersForWorld(int world){
		ArrayList<LevelPointer> ret = new ArrayList<LevelPointer>();
		for (LevelPointer pointer: pointers){
			if (pointer.worldIndex == world) ret.add(pointer);
		}
		return ret;
	}
	
}
