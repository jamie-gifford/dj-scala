package au.com.thoughtpatterns.dj.disco.tangoinfo;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import au.com.thoughtpatterns.core.util.CsvUtils;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.dj.disco.tangoinfo.TangoInfo.Performance;


public class Tracks_Test {

    private static final Logger log = Logger.get(Tracks_Test.class);
    
	public static void main(String[] args) throws Exception {
		
		TangoInfo ti = new TangoInfo();
		List<TangoInfo.Track> tracks = ti.fetchTracks();
		
		CsvUtils util = new CsvUtils();
		
		List<String[]> rows = new ArrayList<>();
		for (TangoInfo.Track t : tracks) {
			Performance w = t.perf;
			String[] row = new String[] {
					w.title,
					w.tiwc,
					w.genre,
					w.orchestra,
					w.vocalist,
					w.date,
					w.duration,
					t.tint.toString()
			};
			rows.add(row);
		}
		
		util.toCsv(rows);
		
		String out = util.getFormattedString();
		
		Writer w = new FileWriter(new File("tangotracks.csv"));
		w.write(out);
		w.close();
	}

    
    // @Test
    public void load() throws Exception {
//    	Tracks ps = Tracks.loadFromWeb(new File("ti-tracks.csv"));
    }
    
}
