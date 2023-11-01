package com.mojang.mario;

import java.awt.*;

public class Block {

    public static enum Trait {
        Animated,
        BlockAll, 
		BlockUpper, 
        BlockLower,
        Invisible,
        Pickupable,
        VertBumpable, 
        HorizBumpable,
        Breakable,
        Swimmable,
        Bouncy,
        Slippery,
        Climbable,
        Hurts,
        Kills,
        Behind,
        SlideLeft,
        SlideRight
        //other types - meltable, freezable, quicksand etc.
    	;
    };
    
    public static enum Powerup {
    	None,
    	Coin,
    	Multicoin,
    	OneUpMushroom,
        FireFlower,
        Star,
        Beanstalk,
        BlueSwitch,
        RedSwitch,
        YellowSwitch,
        GreenSwitch,
        //other powerups - ghost, etc.
    	;
    };

    public int blockId;
    public boolean wasBouncingHorizontally = false;
    
    public Block(int blockId){
    	this.blockId = blockId;
    }
    
    public Image getImage(){
    	return null;
    }
}
