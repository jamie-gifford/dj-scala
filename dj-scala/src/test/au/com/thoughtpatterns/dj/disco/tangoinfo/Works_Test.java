package au.com.thoughtpatterns.dj.disco.tangoinfo;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import au.com.thoughtpatterns.core.util.CsvUtils;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.dj.disco.tangoinfo.TangoInfo.Work;

public class Works_Test {

	private static final Logger log = Logger.get(Works_Test.class);
	
	@Test
	public void testFetch() throws Exception {
		
		TangoInfo ti = new TangoInfo();
		List<Work> works = ti.fetchWorks();
		
		CsvUtils util = new CsvUtils();
		
		List<String[]> rows = new ArrayList<>();
		for (Work w : works) {
			String[] row = new String[] {
					w.title,
					w.genre,
					w.tiwc,
					w.composer,
					w.letrista
			};
			rows.add(row);
		}
		
		util.toCsv(rows);
		
		String out = util.getFormattedString();
		
		Writer w = new FileWriter(new File("tangoworks.csv"));
		w.write(out);
		w.close();
	}
	
}
