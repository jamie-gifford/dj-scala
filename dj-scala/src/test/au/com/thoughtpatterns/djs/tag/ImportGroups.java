package au.com.thoughtpatterns.djs.tag;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import au.com.thoughtpatterns.core.json.JsonyObject;
import au.com.thoughtpatterns.core.json.JsonyParser;
import au.com.thoughtpatterns.core.util.CsvUtils;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.SystemException;

import com.sun.xml.internal.bind.v2.schemagen.Util;

public class ImportGroups {

	private static final Logger log = Logger.get(ImportGroups.class);
	
	public static void main(String[] args) {
		new ImportGroups().run();
	}
	
	void run() {
		try {
			run0();
		} catch (Exception ex) {
			throw new SystemException(ex);
		}
	}
	
	void run0() throws Exception {
		
		CsvUtils csv = new CsvUtils();
		List<String[]> lines = csv.fromCsv(new FileReader("groups.csv"));
		
		for (String[] line: lines) {
			
			String key = line[1];
			String filename = line[4];
			
			String title = line[2];
			String artist = line[3];
			
			//log.info(key + " : " + filename);
			
			int pos = filename.lastIndexOf('.');
			
			if (pos == -1) {
				//log.error("Unexpected filename " + filename);
				continue;
			}
			
			String mdfilename = filename.substring(0,  pos) + ".md";
			
			File mdfile = new File(mdfilename);
			
			if (! mdfile.exists()) {
				continue;
			}

			/*
			if (artist == null || ! artist.contains("Donato Racciatti")) {
				continue;
			}
			*/
			
			fix(mdfile, key);
		}
	}

	void fix(File mdfile, String key) throws Exception {
		JsonyObject md = (JsonyObject) (new JsonyParser()).parse(new FileReader(mdfile));
		
		String group = md.getCast("group", String.class);
		
		if (Util.equal(group, key)) {
			return;
		}
		
		md.set("group", key);

		log.info("Writing " + mdfile);
		Writer w = new PrintWriter(new FileWriter(mdfile));
		w.write(md.toJson());
		w.close();
	}
	
}
