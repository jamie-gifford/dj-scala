package au.com.thoughtpatterns.dj.disco.tangoinfo;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import au.com.thoughtpatterns.core.util.CsvUtils;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.dj.disco.tangoinfo.TangoInfo.Performance;

public class Performances_Test {

	private static final Logger log = Logger.get(Performances_Test.class);
	
	@Test
	public void testFetch() throws Exception {
		
		TangoInfo ti = new TangoInfo();
		List<Performance> perfs = ti.fetchPerformances();
		
		CsvUtils util = new CsvUtils();
		
		List<String[]> rows = new ArrayList<>();
		for (Performance w : perfs) {
			String[] row = new String[] {
					w.title,
					w.tiwc,
					w.genre,
					w.orchestra,
					w.vocalist,
					w.date,
					w.duration
			};
			rows.add(row);
		}
		
		util.toCsv(rows);
		
		String out = util.getFormattedString();
		
		Writer w = new FileWriter(new File("tangoperformances.csv"));
		w.write(out);
		w.close();
	}

}
