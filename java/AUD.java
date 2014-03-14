import java.util.*;

public class AUD 
{
   AUD_interface interf;
   Random rand;
   private boolean has_gen = false;
   private int aud_seed, aud_pat, aud_patlen;

   public AUD()
   {
      interf = new AUD_interface();
      interf.start();
      
      AUD_matrices.init();
   }
   
   public void generatePattern(double stress, double energy, int seed, int num_patterns, int repeats)
   {
      if (num_patterns < 1) num_patterns = 1;
		if (num_patterns > 100) num_patterns = 100;
		if (repeats < 1) repeats = 1;
		if (repeats > 8) repeats = 8;
	
		// apply new pattern/length/seed
		aud_pat = num_patterns;
		aud_patlen = repeats;
		aud_seed = seed;
		
		has_gen = true;
		adaptPattern(stress, energy);	

		// reset song
      togglePause();
      reset();
   }
   
   public void adaptPattern(double stress, double energy)
   {
      if (!has_gen)
		{
			System.out.println("No generation call performed!");
			return;
		}

      // declare and clear new pattern	
		AUD_note pattern[][] = new AUD_note[AUD_interface.NUMTRACKS][32 * aud_pat * aud_patlen];
      for (int i = 0; i < AUD_interface.NUMTRACKS; i++)
         for (int j = 0; j < 32 * aud_pat * aud_patlen; j++)
            pattern[i][j] = new AUD_note(-1,0);
      
		rand = new Random(aud_seed);
		
      // pick base note and tempo
		int base_note = (int)Math.floor(rand.nextDouble() * 11.99) + 24;
		boolean transitions[] = new boolean[aud_pat];
		interf.tempo = (int)(150 * (0.9 + rand.nextDouble() * 0.9));
				
      // pick instrumentation
      int lowInstrs[] = new int[] { 20, 2, 6, 105, 108, 19, 16, 25, 28, 28, 32, 33, 34, 34, 35, 36, 36, 37, 28};
      int highInstrs[] = new int[] { 20, 2, 6, 105, 108, 19, 16, 25, 28, 28, 32, 33, 34, 34, 35, 36, 36, 37, 28, 4, 17, 27};
      
      interf.setInstrs(lowInstrs[rand.nextInt(lowInstrs.length)], lowInstrs[rand.nextInt(lowInstrs.length)],
                       highInstrs[rand.nextInt(highInstrs.length)], highInstrs[rand.nextInt(highInstrs.length)]);
      
		for (int i = 0; i < aud_pat; i++)
		{
			if (rand.nextDouble() < 0.5)
				transitions[i] = true;
			else
				transitions[i] = false;
		}
		
		rand = new Random(aud_seed);
		boolean[][] instr_arr = new boolean[aud_pat][4];
		populate_instr(energy, aud_pat, instr_arr);
		
		rand = new Random(aud_seed);
		int[][] fwd_arr = populate_fwd(instr_arr, aud_pat);
		
		// Pick base note and create base note
		double[][] Jmap = AUD_matrices.createTriJMap(stress, energy);
		double[] Pmap = AUD_matrices.createTriPMap(stress, energy);
		
		double n_eng = 0.5;
		double n_ran = 1.2;
		
		// reset RNG for pattern-length independence
		rand = new Random(aud_seed);
		int n_tracks = (int)Math.floor(2 + energy * n_eng + rand.nextDouble() * n_ran);
		// set other instruments & melody
		populate_melody(pattern, stress, energy, aud_pat, aud_patlen, transitions, Jmap, Pmap, base_note, 0, instr_arr, n_tracks, fwd_arr);
		
		rand = new Random(aud_seed);
		for (int temp = 0; temp < 20; temp++) rand.nextDouble(); // so the two melodic tracks aren't identical
		n_tracks = (int)Math.floor(2 + energy * n_eng + rand.nextDouble() * n_ran);
		populate_melody(pattern, stress, energy, aud_pat, aud_patlen, transitions, Jmap, Pmap, base_note + 7, 1, instr_arr, n_tracks, fwd_arr);
		
		rand = new Random(aud_seed);
		for (int temp = 0; temp < 40; temp++) rand.nextDouble(); // so the two melodic tracks aren't identical
		n_tracks = (int)Math.floor(2 + energy * n_eng + rand.nextDouble() * n_ran);
		populate_melody(pattern, stress, energy, aud_pat, aud_patlen, transitions, Jmap, Pmap, base_note + 12, 2, instr_arr, n_tracks, fwd_arr);
		
		rand = new Random(aud_seed);
		for (int temp = 0; temp < 80; temp++) rand.nextDouble(); // so the two melodic tracks aren't identical
		n_tracks = (int)Math.floor(2 + energy * n_eng + rand.nextDouble() * n_ran);
		populate_melody(pattern, stress, energy, aud_pat, aud_patlen, transitions, Jmap, Pmap, base_note + 24, 3, instr_arr, n_tracks, fwd_arr);
		
      
      // copy - need transition?
      interf.pattern = pattern;
   }

   public void close()
   { 
      System.out.println("AUD closed");
      interf.active = false;
   }
   
   public void togglePlay()
   {
      interf.paused = false;
   }
   
   public void togglePause()
   {
      interf.paused = true;
   }
   
   public void reset()
   {
      interf.ndx = 0;
   }
   
   public void setVolume(float vol)
   {
      // I should be doing something here... 
   }
   
   private boolean canUse(double[][] map, int note, int refnote, double cutoff)
	{
		if (map[note][refnote] > cutoff)
			return true;
		return false;
	}
   
   boolean[][] assure_instr(double energy, boolean[][] array, int no)
	{
		int num = 0;
		for (int t = 0; t < 4; t++)
		{
			if (array[no][t])
				num++;
		}
		
		if (energy < 0.3 && num < 1)
		{
			if (no % 2 == 0)
				array[no][0] = true;
			else
				array[no][1] = true;
		}
		if (energy > 0.3 && num < 2)
		{
			if (num == 0)
			{
				if (no % 2 == 0)
				{
					array[no][0] = true;
					if (no % 3 == 0)
						array[no][1] = true;
					else 
						array[no][2] = true;
				}
				else
				{
					array[no][1] = true;
					if (no % 3 == 0)
						array[no][0] = true;
					else 
						array[no][2] = true;
				}
			}
			else
			{
				if (!array[no][0])
					array[no][0] = true;
				else
					array[no][1] = true;
			}
		}	
		if (num > 3)
		{
			if (no % 2 == 0)
				array[no][2] = false;
			else
				array[no][3] = false;
		}
		if (energy < 0.5 && num > 2)
		{
			if (array[no][3])
				array[no][3] = false;
			else if (no % 2 == 0)
				array[no][2] = false;
			else 
				array[no][0] = false;
		}
		return array;
	}
	void populate_instr(double energy, int pat, boolean[][] array)
	{
		int[] howlong = new int[]{0,0,0,0};
		
		array[0][0] = rand.nextDouble() < 0.5 + energy * 0.2;
		array[0][1] = rand.nextDouble() < 0.4 + energy * 0.2;
		array[0][2] = rand.nextDouble() < 0.3 + energy * 0.2;
		array[0][3] = rand.nextDouble() < 0.3 + energy * 0.1;
		
		array = assure_instr(energy, array, 0);
		
		for (int i = 1; i < pat; i++)
		{
			array[i][0] = rand.nextDouble() < 0.4 + energy * 0.2 - howlong[0] * 0.2;
			if (array[i][0]) howlong[0]++; else howlong[0] = 0; 
			array[i][1] = rand.nextDouble() < 0.4 + energy * 0.2 - howlong[1] * 0.2;
			if (array[i][1]) howlong[1]++; else howlong[1] = 0; 
			array[i][2] = rand.nextDouble() < 0.4 + energy * 0.2 - howlong[2] * 0.2;
			if (array[i][2]) howlong[2]++; else howlong[2] = 0; 
			array[i][3] = rand.nextDouble() < 0.4 + energy * 0.2 - howlong[3] * 0.2;	
			if (array[i][3]) howlong[3]++; else howlong[3] = 0; 
			
			array = assure_instr(energy, array, i);
		}
	}
	int[][] populate_fwd(boolean[][] instr_arr, int pat)
	{
		boolean aseen = false, bseen = false, cseen = false, dseen = false;
		int[][] fwd_arr = new int[pat][4];
		
		for (int i = 0; i < pat; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				fwd_arr[i][j] = -1;
			}
		}
		
		for (int i = 0; i < pat - 1; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				double rnd = rand.nextDouble();
				double[] rarr = new double[]{rand.nextDouble(),rand.nextDouble(),rand.nextDouble()};
				if (instr_arr[i][j] && rnd < 0.7)
				{
					for (int ti = i + 1, ndx = 0; ti < pat && ndx < 3; ti++)
					{
						if (instr_arr[ti][j] && 
							(fwd_arr[ti][0] == -1 || fwd_arr[ti][0] == i) && 
							(fwd_arr[ti][1] == -1 || fwd_arr[ti][1] == i) && 
							(fwd_arr[ti][2] == -1 || fwd_arr[ti][2] == i) && 
							(fwd_arr[ti][3] == -1 || fwd_arr[ti][3] == i) && 
							(!instr_arr[ti][0] || fwd_arr[ti][0] == i || j == 0) && 
							(!instr_arr[ti][1] || fwd_arr[ti][1] == i || j <= 1) && 
							(!instr_arr[ti][2] || fwd_arr[ti][2] == i || j <= 2) && 
							(!instr_arr[ti][3] || fwd_arr[ti][3] == i || j <= 3))
						{
							if (rarr[ndx] < 0.6)
							{
								fwd_arr[ti][j] = i;
							}
							ndx++;
						}
					}
				}
			}
		}
		return fwd_arr;
	}
   
   
   
	private ArrayList<Integer> getJoiningNotes(int pat_pos, int instr_no, AUD_note pattern[][])
	{
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		for (int i = 0; i < pattern.length; i++)
		{
			if (i == instr_no)
				continue;
			if ((i - 2) % 4 == 0)
			{
				if (pattern[i][pat_pos - pat_pos % 8].note_num != -1)
				{
					ret.add(pattern[i][pat_pos - pat_pos % 8].note_num);
				}
			}
			else if ((i - 2) % 4 == 1)
			{
				if (pattern[i][pat_pos - pat_pos % 4].note_num != -1)
				{
					ret.add(pattern[i][pat_pos - pat_pos % 4].note_num);
				}
			}
			else if ((i - 2) % 4 == 2)
			{
				if (pattern[i][pat_pos - pat_pos % 2].note_num != -1)
				{
					ret.add(pattern[i][pat_pos - pat_pos % 2].note_num);
				}
			}
			else if ((i - 2) % 4 == 3)
			{
				if (pattern[i][pat_pos].note_num != -1)
				{
					ret.add(pattern[i][pat_pos].note_num);
				}
			}
		}
		return ret;
	}
   
   private ArrayList<Integer> usableNotes(ArrayList<Integer> notesToJoin, double[][] Jmap, int base_note, double cutoff, boolean expand)
	{
		ArrayList<Integer> avail = new ArrayList<Integer>();
      for (int i = 0; i < 12; i++) avail.add(i);
		
		for (int i = 0; i < notesToJoin.size(); i++)
		{
			int curnote = notesToJoin.get(i) - (base_note % 12);
			curnote %= 12; 
			
			for (int j = 0; j < avail.size(); j++)
			{
				if (!canUse(Jmap, avail.get(j), curnote, cutoff))
				{
					avail.remove(j);
					j--;
				}
			}			
		}
		if (avail.indexOf(0) != -1)
			avail.add(12);
		if (expand)
		{
			if (avail.indexOf(1) != -1)
				avail.add(13);
			if (avail.indexOf(2) != -1)
				avail.add(14);
			if (avail.indexOf(3) != -1)
				avail.add(15);
			if (avail.indexOf(4) != -1)
				avail.add(16);
			if (avail.indexOf(5) != -1)
				avail.add(17);
			if (avail.indexOf(6) != -1)
				avail.add(18);
			if (avail.indexOf(7) != -1)
				avail.add(19);
			if (avail.indexOf(8) != -1)
				avail.add(20);
			if (avail.indexOf(9) != -1)
				avail.add(21);
			if (avail.indexOf(10) != -1)
				avail.add(22);
			if (avail.indexOf(11) != -1)
				avail.add(23);
			if (avail.indexOf(12) != -1)
				avail.add(24);
		}
		for (int i = 0; i < avail.size(); i++)
		{
			avail.set(i, avail.get(i) + base_note);
		}
		if (avail.size() == 0 && notesToJoin.size() != 0)
      {
			for (int i = 0; i < notesToJoin.size(); i++)
			{
				avail.add(notesToJoin.get(i) + base_note);
			}
      }
		return avail;
	}
	
	private int pickNoteFromUsable(double[] Pmap, ArrayList<Integer> n, double nt_r, int base_note, double cutoff)
	{
		if (n.size() == 0) return -1;
		
		int nt = -1; 
		ArrayList<Double> prev = new ArrayList<Double>();
      double pntot = 0;
		for (int pnt = 0; pnt < n.size(); pnt++)
		{
			prev.add(Pmap[(n.get(pnt) - base_note) % 12]);
			pntot += Pmap[(n.get(pnt) - base_note) % 12];
		}
		if (pntot > 0)
		{
			for (int pnt = 0; pnt < prev.size(); pnt++)
			{
				prev.set(pnt, prev.get(pnt) / pntot); 
			}
		}		
		
		do
		{
			nt++;
         if (nt >= n.size()) break;
			nt_r -= prev.get(nt);
		} while(nt_r > 0 || prev.get(nt) < cutoff);
		
		if (nt >= n.size()) return -1;
		
		return n.get(nt);
	}
   
   void populate_melody (AUD_note[][] pattern, double stress, double energy, int pat, int patlen, 
                         boolean[] transitions, double[][] Jmap, double[] Pmap, int base_note, int n_instr, 
                         boolean[][] instr_arr, int n_tracks, int[][] fwd_arr)
	{
		int a = n_instr * 4;
		int b = n_instr * 4 + 1;
		int c = n_instr * 4 + 2;
		int d = n_instr * 4 + 3;		
				
		for (int p = 0; p < pat; p++)
		{		
			double sp_r = rand.nextDouble();
		
			boolean shouldplay = instr_arr[p][n_instr];
			boolean shouldclear = true;
			if (fwd_arr[p][n_instr] != -1)
			{
				int ndxpos = fwd_arr[p][n_instr] * patlen * 32;
				int pos = p * patlen * 32;
				for ( ; pos < (p + 1) * 32 * patlen; pos++, ndxpos++)
				{
					pattern[a][pos] = pattern[a][ndxpos];
					pattern[b][pos] = pattern[b][ndxpos];
					pattern[c][pos] = pattern[c][ndxpos];
					pattern[d][pos] = pattern[d][ndxpos];	
				}
				
				shouldplay = false;
				shouldclear = false;
			}
				
			double[] instr_fractals = new double[]{0.3, 0.5, 0.7};	
			double[] noteplay_fractals = new double[]{0.05, 0.4, 0.6, 0.9};
			double[] joinlimit_fractals = new double[]{0.2, 0.15, 0.08, 0.04};
			double[] prevalence_fractals = new double[]{0.12, 0.05, 0.03, 0.02};
			
			int t_n_tracks = n_tracks;
			int tempnum = 0;
			
			for (int tempndx = 0; tempndx < 4; tempndx++)
			{
				if (instr_arr[p][tempndx])
					tempnum++;
			}
			if (tempnum <= 1 && t_n_tracks <= 2) 
			{
				t_n_tracks = 3;
			}
			
			double renergy = energy + (rand.nextDouble() - 0.5) * 1.0;
			for (int pl = 0; pl < patlen; pl++)
			{
				int pat_pos = p * patlen * 32 + pl * 32;
				
				if (pl == 0)
				{				
					for (int i = 0; i < 32; i++)
					{
						double tenergy = energy;
						double trenergy = renergy;
						
						if (patlen == 1 && i > 32 - 8)
						{
							if (transitions[p]) // low
							{
								tenergy = energy * energy * energy - 0.2;
								trenergy = renergy * renergy * renergy - 0.2;
							}
							else
							{
								tenergy = Math.sqrt(Math.sqrt(energy)) + 0.2;
								trenergy = Math.sqrt(Math.sqrt(renergy)) + 0.2;
							}
						}
						if (n_instr == 0)
						{
							tenergy /= 1.5;
							trenergy /= 1.5;
						}
						
						if (shouldclear)
						{
							pattern[a][pat_pos + i].note_num = -1;
							pattern[a][pat_pos + i].vel = 0;
							pattern[b][pat_pos + i].note_num = -1;
							pattern[b][pat_pos + i].vel = 0;
							pattern[c][pat_pos + i].note_num = -1;
							pattern[c][pat_pos + i].vel = 0;
							pattern[d][pat_pos + i].note_num = -1;
							pattern[d][pat_pos + i].vel = 0;
						}
						
						double r1 = rand.nextDouble();
						double r2 = rand.nextDouble();
						double r3 = rand.nextDouble();
						double r4 = rand.nextDouble();
						double r5 = rand.nextDouble();
						double r6 = rand.nextDouble();
						double r7 = rand.nextDouble();
						double r8 = rand.nextDouble();
						
						if (!shouldplay)
							continue;
						
						if (i % 8 == 0 && (trenergy < instr_fractals[0] || (t_n_tracks >= 3 && trenergy < instr_fractals[1])))
						{
							if (r1 + tenergy > noteplay_fractals[0])
							{
								ArrayList<Integer> ntj = getJoiningNotes(pat_pos + i, a, pattern);
								ArrayList<Integer> n = usableNotes(ntj, Jmap, base_note, joinlimit_fractals[0], false);
								pattern[a][pat_pos + i].note_num = pickNoteFromUsable(Pmap, n, r5, base_note, prevalence_fractals[0]);
								pattern[a][pat_pos + i].vel = 100;
							}
						}
						if (i % 4 == 0 && 
							(t_n_tracks >= 3 || (trenergy > instr_fractals[0] && trenergy < instr_fractals[2]) || (trenergy < instr_fractals[1])))
						{
							if (r2 + tenergy > noteplay_fractals[1])
							{
								ArrayList<Integer> ntj = getJoiningNotes(pat_pos + i, b, pattern);
								ArrayList<Integer> n = usableNotes(ntj, Jmap, base_note, joinlimit_fractals[1], true);
								pattern[b][pat_pos + i].note_num = pickNoteFromUsable(Pmap, n, r6, base_note, prevalence_fractals[1]);
                        pattern[b][pat_pos + i].vel = 100;
							}
						}
						if (i % 2 == 0 && 
							(t_n_tracks >= 3 || (trenergy > instr_fractals[0] && trenergy < instr_fractals[2]) || (trenergy > instr_fractals[1])))
						{
							if (r3 + tenergy > noteplay_fractals[2])
							{
								ArrayList<Integer> ntj = getJoiningNotes(pat_pos + i, c, pattern);
								ArrayList<Integer> n = usableNotes(ntj, Jmap, base_note, joinlimit_fractals[2], true);
								pattern[c][pat_pos + i].note_num = pickNoteFromUsable(Pmap, n, r7, base_note, prevalence_fractals[2]);
							   pattern[c][pat_pos + i].vel = 100;
                     }
						}
						if (i % 1 == 0 && (trenergy > instr_fractals[2] || (t_n_tracks >= 3 && trenergy > instr_fractals[1])))
						{
							if (r4 + tenergy > noteplay_fractals[3])
							{
								ArrayList<Integer> ntj = getJoiningNotes(pat_pos + i, d, pattern);
								ArrayList<Integer> n = usableNotes(ntj, Jmap, base_note, joinlimit_fractals[3], true);
								pattern[d][pat_pos + i].note_num = pickNoteFromUsable(Pmap, n, r8, base_note, prevalence_fractals[3]);
                        pattern[d][pat_pos + i].vel = 100;
							}
						}
					}
				}
				else // copy
				{
					double variance = 0;
					
					if (patlen % 2 == 0)
					{
						if (pl / patlen < 0.5) 
							variance = 0.3; // first half
						else
							variance = 0.1; // second half
					}
					else
					{
						if (pl % 2 == 1 && pl < 4)
							variance = 0.3; // B & C
						else
							variance = 0.1;
					}
					
					if (pl < patlen - 1)
					{
						if (shouldclear)
						{
							int copyfrom = 0;
							if (patlen % 2 == 0)
							{
								if (pl / patlen < 0.5)
									copyfrom = pl;
								else
									copyfrom = patlen / 2;
							}
							else
							{
								if (pl < 4)
									copyfrom = pl;
								else if (patlen == 5 && pl == 4)
									copyfrom = 2;
								else if (pl == 4)
									copyfrom = 3;
								else if (pl == 5)
									copyfrom = 2;
								else if (pl == 6)
									copyfrom = 4;									
							}
						
							for (int i = 0; i < 32; i++)
							{
								double r1 = rand.nextDouble();
								double r2 = rand.nextDouble();
								double r3 = rand.nextDouble();
								double r4 = rand.nextDouble();
								double r5 = rand.nextDouble();
								double r6 = rand.nextDouble();
								double r7 = rand.nextDouble();
								double r8 = rand.nextDouble();
								
								pattern[a][pat_pos + i] = pattern[a][pat_pos + i - 32 * copyfrom];
								pattern[b][pat_pos + i] = pattern[b][pat_pos + i - 32 * copyfrom];
								pattern[c][pat_pos + i] = pattern[c][pat_pos + i - 32 * copyfrom];
								pattern[d][pat_pos + i] = pattern[d][pat_pos + i - 32 * copyfrom];
								
								if (pattern[a][pat_pos + i].note_num != -1 && r1 < variance - 0.1)
								{
									ArrayList<Integer> ntj = getJoiningNotes(pat_pos + i, a, pattern);
									ArrayList<Integer> n = usableNotes(ntj, Jmap, base_note, 0.10, false);
									pattern[a][pat_pos + i].note_num = pickNoteFromUsable(Pmap, n, r5, base_note, 0.05);
                           pattern[a][pat_pos + i].vel = 100;
								}
								if (pattern[b][pat_pos + i].note_num != -1 && r2 < variance)
								{
									ArrayList<Integer> ntj = getJoiningNotes(pat_pos + i, b, pattern);
									ArrayList<Integer> n = usableNotes(ntj, Jmap, base_note, 0.08, false);
									pattern[b][pat_pos + i].note_num = pickNoteFromUsable(Pmap, n, r6, base_note, 0.04);
                           pattern[b][pat_pos + i].vel = 100;
								}
								if (pattern[c][pat_pos + i].note_num != -1 && r3 < variance + 0.1)
								{
									ArrayList<Integer> ntj = getJoiningNotes(pat_pos + i, c, pattern);
									ArrayList<Integer> n = usableNotes(ntj, Jmap, base_note, 0.05, false);
									pattern[c][pat_pos + i].note_num = pickNoteFromUsable(Pmap, n, r7, base_note, 0.02);
                           pattern[c][pat_pos + i].vel = 100;
								}
								if (pattern[d][pat_pos + i].note_num != -1 && r4 < variance + 0.2)
								{
									ArrayList<Integer> ntj = getJoiningNotes(pat_pos + i, d, pattern);
									ArrayList<Integer> n = usableNotes(ntj, Jmap, base_note, 0.03, false);
									pattern[d][pat_pos + i].note_num = pickNoteFromUsable(Pmap, n, r8, base_note, 0.02);
                           pattern[d][pat_pos + i].vel = 100;
								}
							}
						}
					}
					else
					{
						if (shouldclear)
						{
							int copyfrom;
							if (patlen % 2 == 0)
							{
								copyfrom = patlen / 2;
							}
							else
							{
								if (patlen == 3)
									copyfrom = 2;
								else 
									copyfrom = pl - 2;
							} 
						
							for (int i = 0; i < 32 - 8; i++)
							{
                        double r1 = rand.nextDouble();
								double r2 = rand.nextDouble();
								double r3 = rand.nextDouble();
								double r4 = rand.nextDouble();
								double r5 = rand.nextDouble();
								double r6 = rand.nextDouble();
								double r7 = rand.nextDouble();
								double r8 = rand.nextDouble();
								
								pattern[a][pat_pos + i] = pattern[a][pat_pos + i - 32 * copyfrom];
								pattern[b][pat_pos + i] = pattern[b][pat_pos + i - 32 * copyfrom];
								pattern[c][pat_pos + i] = pattern[c][pat_pos + i - 32 * copyfrom];
								pattern[d][pat_pos + i] = pattern[d][pat_pos + i - 32 * copyfrom];
							}
						}
						
						double tenergy = energy;
						double trenergy = renergy;
						if (transitions[p]) // low
						{
							tenergy = energy * energy * energy - 0.2;
							trenergy = renergy * renergy * renergy - 0.2;
						}
						else
						{
							tenergy = Math.sqrt(Math.sqrt(energy)) + 0.2;
							trenergy = Math.sqrt(Math.sqrt(renergy)) + 0.2;
						}
						if (n_instr == 0)
						{
							tenergy /= 1.5;
							trenergy /= 1.5;
						}
						// design transition
						for (int i = 32 - 8; i < 32; i++)
						{
							if (shouldclear)
							{
								pattern[a][pat_pos + i].note_num = -1;
                        pattern[a][pat_pos + i].vel = 0;
								pattern[b][pat_pos + i].note_num = -1;
                        pattern[b][pat_pos + i].vel = 0;
								pattern[c][pat_pos + i].note_num = -1;
                        pattern[c][pat_pos + i].vel = 0;
								pattern[d][pat_pos + i].note_num = -1;
                        pattern[d][pat_pos + i].vel = 0;
							}
                     double r1 = rand.nextDouble();
							double r2 = rand.nextDouble();
							double r3 = rand.nextDouble();
							double r4 = rand.nextDouble();
							double r5 = rand.nextDouble();
							double r6 = rand.nextDouble();
							double r7 = rand.nextDouble();
							double r8 = rand.nextDouble();
							
                     if (!shouldplay)
								continue;
                        
							if (i % 8 == 0 && (trenergy < 0.3 || (n_tracks >= 3 && trenergy < 0.5)))
							{
								if (r1 + tenergy > 0.1)
								{
									ArrayList<Integer> ntj = getJoiningNotes(pat_pos + i, a, pattern);
									ArrayList<Integer> n = usableNotes(ntj, Jmap, base_note, 0.12, false);
									pattern[a][pat_pos + i].note_num = pickNoteFromUsable(Pmap, n, r5, base_note, 0.06);
                           pattern[a][pat_pos + i].vel = 100;                  
								}
							}
							if (i % 4 == 0 && (n_tracks >= 3 || (trenergy > 0.3 && trenergy < 0.7) || (trenergy < 0.5)))
							{
								if (r2 + tenergy > 0.4)
								{
									ArrayList<Integer> ntj = getJoiningNotes(pat_pos + i, b, pattern);
									ArrayList<Integer> n = usableNotes(ntj, Jmap, base_note, 0.09, true);
									pattern[b][pat_pos + i].note_num = pickNoteFromUsable(Pmap, n, r6, base_note, 0.03);
                           pattern[b][pat_pos + i].vel = 100;
								}
							}
							if (i % 2 == 0 && (n_tracks >= 3 || (trenergy > 0.3 && trenergy < 0.7) || (trenergy > 0.5)))
							{
								if (r3 + tenergy > 0.6)
								{
									ArrayList<Integer> ntj = getJoiningNotes(pat_pos + i, c, pattern);
									ArrayList<Integer> n = usableNotes(ntj, Jmap, base_note, 0.05, true);
									pattern[c][pat_pos + i].note_num = pickNoteFromUsable(Pmap, n, r7, base_note, 0.02);
                           pattern[c][pat_pos + i].vel = 100;
								}
							}
							if (i % 1 == 0 && (trenergy > 0.7 || (n_tracks >= 3 && trenergy > 0.5)))
							{
								if (r4 + tenergy > 0.9)
								{
									ArrayList<Integer> ntj = getJoiningNotes(pat_pos + i, d, pattern);
									ArrayList<Integer> n = usableNotes(ntj, Jmap, base_note, 0.03, true);
									pattern[d][pat_pos + i].note_num = pickNoteFromUsable(Pmap, n, r8, base_note, 0.02);
                           pattern[d][pat_pos + i].vel = 100;
								}
							}
						}
					}	
				}
			}
			
			// consume more random numbers to compensate for # over repeats - consume numbers equivalent to 8 repeats 
			for (int tmp = 0; tmp < (8 - patlen) * 32 * 8 ; tmp++) 
				rand.nextDouble();
		}
	}
}