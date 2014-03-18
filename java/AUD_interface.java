import java.util.*;
import java.io.*;
import javax.sound.midi.*;
import javax.sound.midi.spi.*;

public class AUD_interface extends Thread
{
   public boolean active = true, paused = true;
   public AUD_note pattern[][]; 
   public int ndx = 0;
   public int tempo = 150; 
   private MidiChannel[] channels;
   public static final int NUMTRACKS = 16;
   public static final int NUMPERC = 3; 

   public void run()
   {
      System.out.println("loading AUD_interface");
      try {
          Synthesizer synthesizer = MidiSystem.getSynthesizer();
          synthesizer.open();
          
          // 20, 2, (4, 17, 27 for high notes), 6, 105, 108, 19, 16, 25, 28!, 32, 33, 34!, 35, 36!, 37, 28

          //I'm picking some presets below - they can be overriden by calling setInstrs
          
          channels = synthesizer.getChannels();
          channels[0].programChange(36); 
          channels[1].programChange(28); 
          channels[2].programChange(34); 
          channels[3].programChange(17);  
          channels[9].programChange(24);
          
          while (this.active)
          {
             if (this.paused)
             {
                Thread.sleep(50);
             }
             else
             {
                playAt(ndx, channels);
             
                Thread.sleep(tempo);
                
                stopAt(ndx, channels[0]);
                stopAt(ndx, channels[1]);
                stopAt(ndx, channels[2]);
                stopAt(ndx, channels[3]);
                stopAt(ndx, channels[9]);
                
                ndx++;
                if (ndx >= pattern[0].length)
                  ndx = 0;
             }
          }
          
          synthesizer.close();
      } catch (Exception e)
      {
          e.printStackTrace();
      }
   }
   
   public void setInstrs(int a, int b, int c, int d)
   {
      channels[0].programChange(a);
      channels[1].programChange(b);
      channels[2].programChange(c);
      channels[3].programChange(d);
   }
   
   private void playAt(int ndx, MidiChannel[] channels)
   {
      for (int i = 0; i < NUMTRACKS; i++)
      {
         switch (i % 4)
         {
            case 0: if (ndx % 8 == 0) 
               channels[i / 4].noteOn(this.pattern[i][ndx - ndx % 8].note_num, this.pattern[i][ndx - ndx % 8].vel); break;
            case 1: if (ndx % 4 == 0) 
               channels[i / 4].noteOn(this.pattern[i][ndx - ndx % 4].note_num, this.pattern[i][ndx - ndx % 4].vel); break;
            case 2: if (ndx % 2 == 0) 
               channels[i / 4].noteOn(this.pattern[i][ndx - ndx % 2].note_num, this.pattern[i][ndx - ndx % 2].vel); break;
            case 3: if (ndx % 1 == 0)
               channels[i / 4].noteOn(this.pattern[i][ndx - ndx % 1].note_num, this.pattern[i][ndx - ndx % 1].vel); break;
         }
      } 
      if (this.pattern[NUMTRACKS][ndx].note_num > 0)
         channels[9].noteOn(this.pattern[NUMTRACKS][ndx].note_num, this.pattern[NUMTRACKS][ndx].vel);
      if (this.pattern[NUMTRACKS + 1][ndx - ndx % 2].note_num > 0 && ndx % 2 == 0)
         channels[9].noteOn(this.pattern[NUMTRACKS + 1][ndx - ndx % 2].note_num, this.pattern[NUMTRACKS + 1][ndx - ndx % 2].vel);
      if (this.pattern[NUMTRACKS + 2][ndx].note_num > 0)
         channels[9].noteOn(this.pattern[NUMTRACKS + 2][ndx].note_num, this.pattern[NUMTRACKS + 2][ndx].vel);
   }
   private void stopAt(int ndx, MidiChannel channel)
   {
      for (int i = 0; i < NUMTRACKS; i++)
      {
         switch (i % 4)
         {
            case 0: if (ndx % 8 == 7) 
               channel.noteOn(this.pattern[i][ndx - ndx % 8].note_num, 0); break;
            case 1: if (ndx % 4 == 3) 
               channel.noteOn(this.pattern[i][ndx - ndx % 4].note_num, 0); break;
            case 2: if (ndx % 2 == 1) 
               channel.noteOn(this.pattern[i][ndx - ndx % 2].note_num, 0); break;
            case 3: if (ndx % 1 == 0)
               channel.noteOn(this.pattern[i][ndx - ndx % 1].note_num, 0); break;
         }
      } 
      if (this.pattern[NUMTRACKS][ndx].note_num > 0)
         channels[9].noteOn(this.pattern[NUMTRACKS][ndx].note_num, 0);
      if (this.pattern[NUMTRACKS + 1][ndx - ndx % 2].note_num > 0 && ndx % 2 == 1)
         channels[9].noteOn(this.pattern[NUMTRACKS + 1][ndx - ndx % 2].note_num, 0);
      if (this.pattern[NUMTRACKS + 2][ndx].note_num > 0)
         channels[9].noteOn(this.pattern[NUMTRACKS + 2][ndx].note_num, 0);
      
   }
   
   public void saveMidi(String filename)
   {
      if (this.pattern == null)
      {
         System.out.println("No data to save!");
         return;
      }
   
      try
      {
         File f = new File(filename + ".mid");
         
         Sequence s = new Sequence(Sequence.PPQ, 24);
         
         for (int i = 0; i < 5; i++)
         {
            Track t = s.createTrack();
            int track = -1;
            int start_instr = -1, end_instr = -1;
                      
           
            //****  General MIDI sysex -- turn on General MIDI sound set  ****
		      byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
		      SysexMessage sm = new SysexMessage();
		      sm.setMessage(b, 6);
		      MidiEvent me = new MidiEvent(sm,(long)0);
		      t.add(me);

//****  set tempo (meta event)  ****
      		MetaMessage mt = new MetaMessage();
            byte[] bt = {0x02, (byte)0x00, 0x00};
      		mt.setMessage(0x51 ,bt, 3);
      		me = new MidiEvent(mt,(long)0);
      		t.add(me);

//****  set track name (meta event)  ****
      		mt = new MetaMessage();
      		String TrackName = new String("midifile track");
      		mt.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
      		me = new MidiEvent(mt,(long)0);
      		t.add(me);

//****  set omni on  ****
      		ShortMessage mm = new ShortMessage();
      		mm.setMessage(0xB0, 0x7D,0x00);
      		me = new MidiEvent(mm,(long)0);
      		t.add(me);

//****  set poly on  ****
      		mm = new ShortMessage();
	      	mm.setMessage(0xB0, 0x7F,0x00);
		      me = new MidiEvent(mm,(long)0);
		      t.add(me);
            
            
            switch(i)
            {
               case 0:  track = 0; t.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, track, 0, 36), (long)0)); 
                        start_instr = 0; end_instr = 4; break;
               case 1:  track = 1; t.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, track, 0, 28), (long)0)); 
                        start_instr = 4; end_instr = 8; break;
               case 2:  track = 2; t.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, track, 0, 34), (long)0)); 
                        start_instr = 8; end_instr = 12; break;
               case 3:  track = 3; t.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, track, 0, 27), (long)0)); 
                        start_instr = 12; end_instr = 16; break;
               case 4:  track = 9; t.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, track, 0, 24), (long)0)); 
                        start_instr = 16; end_instr = 19; break;
            }
            
            int delta = tempo / 3; // not sure why, but it works...
                        
            int tick;
            for (tick = 0; tick < this.pattern[0].length; tick++)
            {
                for (int ndx = start_instr; ndx < end_instr; ndx++)
                {
                  // check for note_on's
                  switch (ndx % 4)
                  {
                     case 0: if (tick % 8 == 0 && this.pattern[ndx][tick - tick % 8].note_num > 0) 
                        t.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, track, 
                                 this.pattern[ndx][tick - tick % 8].note_num, 
                                 this.pattern[ndx][tick - tick % 8].vel), (long)tick * delta)); break;
                     case 1: if (tick % 4 == 0 && this.pattern[ndx][tick - tick % 4].note_num > 0) 
                        t.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, track, 
                                 this.pattern[ndx][tick - tick % 4].note_num, 
                                 this.pattern[ndx][tick - tick % 4].vel), (long)tick * delta)); break;
                     case 2: if (tick % 2 == 0 && this.pattern[ndx][tick - tick % 2].note_num > 0) 
                        t.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, track, 
                                 this.pattern[ndx][tick - tick % 2].note_num, 
                                 this.pattern[ndx][tick - tick % 2].vel), (long)tick * delta)); break;
                     case 3: if (tick % 1 == 0 && this.pattern[ndx][tick - tick % 1].note_num > 0) 
                        t.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, track, 
                                 this.pattern[ndx][tick - tick % 1].note_num, 
                                 this.pattern[ndx][tick - tick % 1].vel), (long)tick * delta)); break;
                  }
                } 
            }
            
            mt = new MetaMessage();
            byte[] bet = {}; // empty array
      		mt.setMessage(0x2F,bet,0);
      		me = new MidiEvent(mt, (long)tick * delta);
      		t.add(me);
            
         }
         
         MidiSystem.write(s, 1, f);
         System.out.println(filename + ".mid saved");
      }
      catch(Exception e)
      {
         System.out.println("File could not be saved: " + e.toString());
      }
   }
}