package com.mojang.mario.level;

import java.util.Random;

public class BgLevelGenerator{
    private static Random levelSeedRandom = new Random();

    public static Level createLevel(short width, short height, boolean distant, LevelGenerator.Background type){
        BgLevelGenerator levelGenerator = new BgLevelGenerator(width, height, distant, type);
        return levelGenerator.createLevel(levelSeedRandom.nextLong());
    }

    private short width, height;
    private boolean distant;
    private LevelGenerator.Background type;

    private BgLevelGenerator(short width, short height, boolean distant, LevelGenerator.Background type){
        this.width = width;
        this.height = height;
        this.distant = distant;
        this.type = type;
    }

    private Level createLevel(long seed){
        Level level = new Level(width, height);
        Random random = new Random(seed);

        switch (type){
            case HILLS:
            case SNOW:
            case LAVA:
            {
                int range = distant ? 4 : height-2;
                int offs = distant ? 2 : 1;
                int oh = random.nextInt(range) + offs;
                int h = random.nextInt(range) + offs;
                for (int x = 0; x < width; x++){
                    oh = h;
                    while (oh == h){
                        h = random.nextInt(range) + offs;
                    }
                    for (int y = 0; y < height; y++){
                        int h0 = (oh < h) ? oh : h;
                        int h1 = (oh < h) ? h : oh;
                        if (y < h0){
                            if (distant){
                                int s = 2;
                                if (y < 2) s = y;
                                level.getBlock(x, y).blockId =  (4 + s * 8);
                            }
                            else{
                                level.getBlock(x, y).blockId =  5;
                            }
                        }
                        else if (y == h0){
                            int s = h0 == h ? 0 : 1;
                            s += distant ? 2 : 0;
                            level.getBlock(x, y).blockId =  s;
                        }
                        else if (y == h1){
                            int s = h0 == h ? 0 : 1;
                            s += distant ? 2 : 0;
                            level.getBlock(x, y).blockId =  (s + 16);
                        }
                        else{
                            int s = y > h1 ? 1 : 0;
                            if (h0 == oh) s = 1 - s;
                            s += distant ? 2 : 0;
                            level.getBlock(x, y).blockId =  (s + 8);
                        }
                    }
                }
                break;
            }
            case UNDERGROUND: 
            case UNDERGROUND_2:
            {
                if (distant) { 
                    for (int x = 0; x < width; x++){
                        for (int y = 0; y < height; y++){
		                	int xTile = 4 + x%2;
		                	int yTile = 3;
		                	
		                	if (y < 2){
		                		xTile = 4;
		                		yTile = 8;
		                	}
		                	if (y == 2){ //4,3 to 5,3 repeating
		                		yTile = 3;
		                	}
		                	else if (y == 3){
		                		yTile = 4;
		                	}
		                	else if (y > 3 && y < height-4){
		                		yTile = 5;
		                	}
		                	else if (y == height-4){
		                		yTile = 6;
		                	}
		                	else if (y == height-3){
		                		yTile = 7;
		                	}
		                	else{
		                		xTile = 4;
		                		yTile = 8;
		                	}

		                	int blockId = xTile + 8*yTile;
		                	level.getBlock(x, y).blockId = blockId;
                        }
                    }
                }
                else{
                    for (int x = 0; x < width; x++){
                        for (int y = 0; y < height; y++){
		                	int xTile = 6 + x%2;
		                	int yTile = 3;
		                	
		                	if (y == 0){
		                		yTile = 7;
		                	}
		                	if (y == 1){
		                		yTile = 0;
		                	}
		                	else if (y == 2){
		                		yTile = 1;
		                	}
		                	else if (y == 3){
		                		yTile = 2;
		                	}
		                	else if (y > 3 && y < height-4){
		                		if (x%2 == 0){ //blank
		                			xTile = 5;
		                			yTile = 0;
		                		}
		                		else{
		                			yTile = 3;
		                		}
		                	}
		                	else if (y == height-4){
		                		if (x%2 == 0){ //blank
		                			xTile = 5;
		                			yTile = 0;
		                		}
		                		else{
		                			yTile = 4;
		                		}
		                	}
		                	else if (y == height-3){
		                		yTile = 5;
		                	}
		                	else if (y == height-2){
		                		yTile = 6;
		                	}
		                	else yTile = 7;

		                	int blockId = xTile + 8*yTile;
		                	level.getBlock(x, y).blockId = blockId;
                        }
                    }
                }
                break;
            }
            case CASTLE:
            case BOSS:
            case UNDERWATER:
            {
                if (distant){
                    for (int x = 0; x < width; x++){
                        for (int y = 0; y < height; y++){
                        	int xTile = 1 + x%2;
                        	int yTile = y;

                        	//top two rows are 3,8
                        	if (y == 0 || y == 1){
                        		xTile = 3;
                        		yTile = 8;
                        	}
                        	//next row is 1,4 to 2,4 repeating
                        	else if (y == 2){
                        		yTile = 4;
                        	}
                        	//next is [blank], [2,5]
                        	else if (y == 3){
                        		if (x%2==0){ //blank
                        			xTile = 1;
                        			yTile = 6;
                        		}
                        		else{
	                        		xTile = 2;
	                        		yTile = 5;
                        		}
                        	}
                        	//next is [blank], [2,6], which repeats until two rows from bottom
                        	else if (y >= 4 && y < height-4){
                        		if (x%2==0){ //blank
                        			xTile = 1;
                        			yTile = 6;
                        		}
                        		else{
                        			xTile = 2;
                        			yTile = 6;
                        		}
                        	}
                        	//then is [blank], [2,7]
                        	else if (y == height-4){
                        		if (x%2==0){ //blank
                        			xTile = 1;
                        			yTile = 6;
                        		}
                        		else{
                        			xTile = 2;
                        			yTile = 7;
                        		}
                        	}
                        	//then is just [0][7] repeating
                        	else if (y == height-3){
                        		xTile = 3;
                        		yTile = 7;
                        	}
                        	//bottom is 3,8 again
                        	else {
                        		xTile = 3;
                        		yTile = 8;
                        	}
                        	
                        	int blockId = xTile+8*yTile;
                            level.getBlock(x, y).blockId = blockId;
                        }
                    }
                }
                else{
                    for (int x = 0; x < width; x++){
                        for (int y = 0; y < height; y++){
                        	int xTile = 1 + x%3;
                        	int yTile = y;

                        	//top row is 2,8
                        	if (y == 0){
                        		xTile = 2;
                        		yTile = 8;
                        	}
                        	//next row is 1,3 to 3,3 repeating
                        	if (y == 1){
                        		yTile = 3;
                        	}
                        	//next is [blank], [blank], [0,4]
                        	else if (y == 2){
                        		if (x%3 < 2){ //blank
                        			xTile = 5;
                        			yTile = 0;
                        		}
                        		else{
                        			yTile = 4;
                        		}
                        	}
                        	//next is [blank], [blank], [0,5], which repeats until two rows from bottom
                        	else if (y >= 3 && y < height-3){
                        		if (x%3 < 2){ //blank
                        			xTile = 5;
                        			yTile = 0;
                        		}
                        		else{
                        			yTile = 5;
                        		}
                        	}
                        	//then is [blank], [blank], [0,6]
                        	else if (y == height-3){
                        		if (x%3 < 2){ //blank
                        			xTile = 5;
                        			yTile = 0;
                        		}
                        		else{
                        			yTile = 6;
                        		}
                        	}
                        	//then is just [1][8] repeating
                        	else if (y == height-2){
                        		xTile = 1;
                        		yTile = 8;
                        	}
                        	//back to 2,8 again
                        	else{
                        		xTile = 2;
                        		yTile = 8;
                        	}
                        	
                        	int blockId = xTile+8*yTile;
                            level.getBlock(x, y).blockId = blockId;
                        }
                    }
                }
                break;
            }
            case DESERT:{
            	if (distant){
            		//bottom row, copy from 0,6 to 3,6
            		int y = height-1;
            		for (int x = 0; x < width; x++){
            			int blockX = x%4;
            			int blockY = 6;
            			int blockId = blockX+8*blockY;
            			level.getBlock(x, y).blockId = blockId;
            		}
            		
            		//next row up, copy from 0,5 to 3,5
            		y = height-2;
            		for (int x = 0; x < width; x++){
            			int blockX = x%4;
            			int blockY = 5;
            			int blockId = blockX+8*blockY;
            			level.getBlock(x, y).blockId = blockId;
            		}

            		//from there up, copy from 0,3 to 3,4
            		for (y = height-3; y >= 0; y--){
	            		for (int x = 0; x < width; x++){
	            			int blockX = x%4;
	            			int blockY = 3+y%2;
	            			int blockId = blockX+8*blockY;
	            			level.getBlock(x, y).blockId = blockId;
	            		}
            		}
            	}
            	else {
            		//copy from 0,7 to 5,9 repeating but only along bottom row
           			for (int y = height-1; y >= 0 && y >= height-3; y--){
           				for (int x = 0; x < width; x++){
           					int blockX = x%6;
           					int blockY = 9-((height-1)-y);
	            			int blockId = blockX+8*blockY;
            				level.getBlock(x, y).blockId = blockId;
            			}
            		}
           			//then fill in the rest with 0,7 (blank)
           			for (int y = height-4; y >= 0; y--){
           				for (int x = 0; x < width; x++){
           					level.getBlock(x, y).blockId = 56; //0,7 (blank tile)
           				}
           			}
            	}
            	break;
            }
            case SKY:{
            	//from 4,3 height-4, width-4
            	if (distant){
            		for (int x = 0; x < width; x++){
            			for (int y = 0; y < height; y++){
            				int blockX = 4 + x%4;
           					int blockY = 3 + y%4;
	            			int blockId = blockX+8*blockY;
            				level.getBlock(x, y).blockId = blockId;
            			}
            		}
            	}
            	else {
           			//fill in with 0,7 (blank)
           			for (int y = height-1; y >= 0; y--){
           				for (int x = 0; x < width; x++){
           					level.getBlock(x, y).blockId = 56; //0,7 (blank tile)
           				}
           			}
            	}
            	break;
            }
        }
        return level;
    }
}