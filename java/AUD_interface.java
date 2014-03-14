import java.util.*;
import javax.sound.midi.*;

public class AUD_interface extends Thread
{
   public boolean active = true, paused = true;
   public AUD_note pattern[][]; 
   public int ndx = 0;
   public int tempo = 150; 
   private MidiChannel[] channels;
   public static final int NUMTRACKS = 16;

   public void run()
   {
      System.out.println("loading AUD_interface");
      try {
          Synthesizer synthesizer = MidiSystem.getSynthesizer();
          synthesizer.open();
          
          // 20, 2, (4, 17, 27 for high notes), 6, 105, 108, 19, 16, 25, 28!, 32, 33, 34!, 35, 36!, 37, 28
                    
          int num = 2;
          
          channels = synthesizer.getChannels();
          channels[0].programChange(num);
          channels[1].programChange(num);
          channels[2].programChange(num);
          channels[3].programChange(num);
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
   }
}