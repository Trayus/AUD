import javax.swing.*;
import java.awt.event.*;
import java.awt.*;


/** creates a new AUD and plays a piece **/
public class AUD_tester extends JFrame
{
   public static void main(String[] args)
   {
      new AUD_tester();
   }
   
   private class Windower implements WindowListener {
      AUD aud;
      public Windower(AUD aud) {
         this.aud = aud;
      }
      public void windowClosed(WindowEvent e) { }
      public void windowDeactivated(WindowEvent e) { }
      public void windowActivated(WindowEvent e) { }
      public void windowDeiconified(WindowEvent e) { }
      public void windowIconified(WindowEvent e) { }
      public void windowClosing(WindowEvent e) {
         aud.close();
      }
      public void windowOpened(WindowEvent e) { }
   }
   
   private class GenButtonListener implements ActionListener
   {
      AUD aud;
      public GenButtonListener(AUD aud) {
         this.aud = aud;
      }
      public void actionPerformed(ActionEvent ev) {
         double s = Double.parseDouble(stressArea.getText());
         if (s > 1) s = 1; if (s < 0) s = 0;
         double e = Double.parseDouble(energyArea.getText());
         if (e > 1) e = 1; if (e < 0) e = 0;
         int sd = Integer.parseInt(seedArea.getText());
         if (sd < 0) sd = 0;
         int p = Integer.parseInt(patArea.getText());
         if (p < 1) p = 1; if (p > 100) p = 100;
         int pl = Integer.parseInt(patlenArea.getText());
         if (pl < 1) pl = 1; if (pl > 8) pl = 8;
      
         aud.generatePattern(s, e, sd, p, pl);
      }
   }
   private class AdaptButtonListener implements ActionListener
   {
      AUD aud;
      public AdaptButtonListener(AUD aud) {
         this.aud = aud;
      }
      public void actionPerformed(ActionEvent ev) {
         double s = Double.parseDouble(stressArea.getText());
         if (s > 1) s = 1; if (s < 0) s = 0;
         double e = Double.parseDouble(energyArea.getText());
         if (e > 1) e = 1; if (e < 0) e = 0;
         
         aud.adaptPattern(s, e);
      }
   }
   
   private class PlayButtonListener implements ActionListener
   {
      AUD aud;
      public PlayButtonListener(AUD aud) {
         this.aud = aud;
      }
      public void actionPerformed(ActionEvent e) {
         aud.togglePlay();
      }
   }
   private class PauseButtonListener implements ActionListener
   {
      AUD aud;
      public PauseButtonListener(AUD aud) {
         this.aud = aud;
      }
      public void actionPerformed(ActionEvent e) {
         aud.togglePause();
      }
   }
   private class ResetButtonListener implements ActionListener
   {
      AUD aud;
      public ResetButtonListener(AUD aud) {
         this.aud = aud;
      }
      public void actionPerformed(ActionEvent e) {
         aud.reset();
      }
   }
   private class SaveButtonListener implements ActionListener
   {
      AUD aud;
      public SaveButtonListener(AUD aud) {
         this.aud = aud;
      }
      public void actionPerformed(ActionEvent e) {
         aud.saveAsMidi();
      }
   }
   
   private JTextArea stressArea, energyArea, seedArea;
   private JTextArea patArea, patlenArea;
   
   public AUD_tester()
   {
      super("AUD.java test");
      AUD aud = new AUD();
      this.addWindowListener(new Windower(aud));
      this.setLayout(new GridLayout(3, 2));
      
      JPanel a = new JPanel();
      a.setLayout(new GridLayout(3, 2));
      
      a.add(new JLabel("Stress: "));
      stressArea = new JTextArea("0.5", 1, 10);
      a.add(stressArea);
      
      a.add(new JLabel("Energy: "));
      energyArea = new JTextArea("0.5", 1, 10);
      a.add(energyArea);
      
      a.add(new JLabel("Seed: "));
      seedArea = new JTextArea("0", 1, 10);
      a.add(seedArea);
      
      this.add(a);
      
      JPanel d = new JPanel();
      d.setLayout(new GridLayout(3, 2));
      
      d.add(new JLabel("Patterns: "));
      patArea = new JTextArea("1", 1, 10);
      d.add(patArea);
      
      d.add(new JLabel("Repeats: "));
      patlenArea = new JTextArea("1", 1, 10);
      d.add(patlenArea);
      
      this.add(d);
      
      JPanel b = new JPanel();
      
      JButton gen = new JButton("Generate");
      gen.addActionListener(new GenButtonListener(aud));
      b.add(gen);
      
      JButton adapt = new JButton("Adapt");
      adapt.addActionListener(new AdaptButtonListener(aud));
      b.add(adapt);
      
      this.add(b);
      
      JPanel c = new JPanel();
      
      JButton play = new JButton("Play");
      play.addActionListener(new PlayButtonListener(aud));
      c.add(play);
      
      JButton pause = new JButton("Pause");
      pause.addActionListener(new PauseButtonListener(aud));
      c.add(pause);
      
      JButton reset = new JButton("Reset");
      reset.addActionListener(new ResetButtonListener(aud));
      c.add(reset);
      
      this.add(c);
      
      
      JButton save = new JButton("Save as .midi");
      save.addActionListener(new SaveButtonListener(aud));
      this.add(save);
         
      pack();
      this.setVisible(true);
      this.setDefaultCloseOperation(EXIT_ON_CLOSE);
      
      this.setResizable(false);
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
   }
   
}