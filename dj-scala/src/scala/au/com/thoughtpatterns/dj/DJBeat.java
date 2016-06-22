package au.com.thoughtpatterns.dj;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import at.ofai.music.beatroot.AudioProcessor;
import at.ofai.music.beatroot.BeatTrackDisplay;
import at.ofai.music.util.Event;
import at.ofai.music.util.EventList;

public class DJBeat {

	public Double bpm(File in, int divisor) {
		
		try {
			File tmp = File.createTempFile("djbeat", ".wav");
			tmp.deleteOnExit();
			
			List<String> cmd = new ArrayList<>();
			cmd.add("sox");
			cmd.add(in.getAbsolutePath());
			cmd.add(tmp.getAbsolutePath());
			cmd.add("trim");
			cmd.add("32");
			cmd.add("32");
			ProcessBuilder b = new ProcessBuilder(cmd);
			
			Process p = b.start();
			int okay = p.waitFor();
			if (okay != 0) {
				return null;
			}
			
			Au au = new Au();
			au.setInputFile(tmp.getAbsolutePath());
			au.processFile();
			EventList beats = BeatTrackDisplay.beatTrack(au.getEvents(), null, divisor);

			Double start = null;
			Double last = null;
			int count = 0;
			for (Event e : beats.l) {
				if (start == null) {
					start = e.keyDown;
				} else {
					last = e.keyDown;
					count ++;
				}
			}
			
			if (last == null || start == null || last == start) {
				return null;
			}
			
			double bpm = 60d * count / (last - start);
			
			while (bpm > 45 * divisor) {
				bpm /= divisor;
			}
			
			while (bpm < 45) {
				bpm *= divisor;
			}
			
			bpm = Math.round(bpm * 10d) / 10d;
			
			return bpm;
		} catch (Exception ex) {
			return null;
		}
	}
	
	class Au extends AudioProcessor {
		
		EventList getEvents() {
			return onsetList;
		}
		
	}
	
}
