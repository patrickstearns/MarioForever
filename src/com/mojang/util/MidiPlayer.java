package com.mojang.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

///**
// * An example that plays a Midi sequence. First, the sequence is played once
// * with track 1 turned off. Then the sequence is played once with track 1 turned
// * on. Track 1 is the drum track in the example midi file.
// */
//public class MidiTest implements MetaEventListener {
//
//  // The drum track in the example Midi file
//  private static final int DRUM_TRACK = 1;
//
//  public static void main(String[] args) {
//    new MidiTest().run();
//  }
//
//  private MidiPlayer player;
//
//  public void run() {
//
//    player = new MidiPlayer();
//
//    // load a sequence
//    Sequence sequence = player.getSequence("../sounds/music.midi");
//
//    // play the sequence
//    player.play(sequence, true);
//
//    // turn off the drums
//    System.out.println("Playing (without drums)...");
//    Sequencer sequencer = player.getSequencer();
//    sequencer.setTrackMute(DRUM_TRACK, true);
//    sequencer.addMetaEventListener(this);
//  }
//
//  /**
//   * This method is called by the sound system when a meta event occurs. In
//   * this case, when the end-of-track meta event is received, the drum track
//   * is turned on.
//   */
//  public void meta(MetaMessage event) {
//    if (event.getType() == MidiPlayer.END_OF_TRACK_MESSAGE) {
//      Sequencer sequencer = player.getSequencer();
//      if (sequencer.getTrackMute(DRUM_TRACK)) {
//        // turn on the drum track
//        System.out.println("Turning on drums...");
//        sequencer.setTrackMute(DRUM_TRACK, false);
//      } else {
//        // close the sequencer and exit
//        System.out.println("Exiting...");
//        player.close();
//        System.exit(0);
//      }
//    }
//  }
//
//}

public class MidiPlayer implements MetaEventListener {

  // Midi meta event
  public static final int END_OF_TRACK_MESSAGE = 47;

  private Sequencer sequencer;

  private boolean loop;

  private boolean paused;

  /**
   * Creates a new MidiPlayer object.
   */
  public MidiPlayer() {
	  open();
  }

  private void open(){
    try {
      sequencer = MidiSystem.getSequencer();
      sequencer.open();
      sequencer.addMetaEventListener(this);
    } catch (MidiUnavailableException ex) {
      sequencer = null;
      ex.printStackTrace();
    }
  }

  /**
   * Loads a sequence from an input stream. Returns null if an error occurs.
   */
  public Sequence loadSequence(InputStream is) {
    try {
      if (!is.markSupported()) {
        is = new BufferedInputStream(is);
      }
      Sequence s = MidiSystem.getSequence(is);
      is.close();
      return s;
    } catch (InvalidMidiDataException ex) {
      ex.printStackTrace();
      return null;
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Plays a sequence, optionally looping. This method returns immediately.
   * The sequence is not played if it is invalid.
   */
  public void play(Sequence sequence, boolean loop) {

	  open();
	  
	  if (sequencer != null && sequence != null && sequencer.isOpen()) {
      try {
        sequencer.setSequence(sequence);
        sequencer.start();
        this.loop = loop;
      } catch (InvalidMidiDataException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * This method is called by the sound system when a meta event occurs. In
   * this case, when the end-of-track meta event is received, the sequence is
   * restarted if looping is on.
   */
  public void meta(MetaMessage event) {
    if (event.getType() == END_OF_TRACK_MESSAGE) {
      if (sequencer != null && sequencer.isOpen() && loop) {
        sequencer.start();
      }
    }
  }

  /**
   * Stops the sequencer and resets its position to 0.
   */
  public void stop() {
    if (sequencer != null && sequencer.isOpen()) {
      sequencer.stop();
      close();
    }
  }

  /**
   * Closes the sequencer.
   */
  public void close() {
    if (sequencer != null && sequencer.isOpen()) {
      sequencer.close();
    }
  }

  /**
   * Gets the sequencer.
   */
  public Sequencer getSequencer() {
    return sequencer;
  }

  /**
   * Sets the paused state. Music may not immediately pause.
   */
  public void setPaused(boolean paused) {
    if (this.paused != paused && sequencer != null && sequencer.isOpen()) {
      this.paused = paused;
      if (paused) {
        sequencer.stop();
      } else {
        sequencer.start();
      }
    }
  }

  /**
   * Returns the paused state.
   */
  public boolean isPaused() {
    return paused;
  }

}