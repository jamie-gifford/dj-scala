package au.com.thoughtpatterns.dj.disco.batc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import au.com.thoughtpatterns.core.util.CsvUtils;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.SystemException;

public class Batc {

	private static final Logger log = Logger.get(Batc.class);

	private static final File CACHE_DIR = new File("data/batc");
	
	private static final String BASE_URL = "http://buenosairestangoclub.com/en/13-mp3";

	public static class Metadata {
		public String title;
		public String artist;
		public String album;
		public String num;
		
		public String toString() {
			return String.format("artist=[%s],title=[%s],album=[%s],num=[%s]", artist, title, album, num);
		}
	}

	private List<Metadata> list = new ArrayList<>();
	
	public void toCSV(File out) throws IOException {

		int page = 1;
		while (true) {
			boolean cont = load(page);
			if (! cont) {
				break;
			}
			page ++;
		}

		List<String[]> lines = new ArrayList<>();
		
		lines.add(new String[] {
				"Interprete",
				"Album",
				"Track",
				"Tema"
		});

		
		for (Metadata md : list) {
			String[] line = new String[] {
			    md.artist,
			    md.album,
			    md.num,
			    md.title
			};
			lines.add(line);
		}
		
		CsvUtils utils = new CsvUtils();
		utils.toCsv(lines);
		String str = utils.getFormattedString();
		PrintWriter w = new PrintWriter(new FileWriter(out));
		w.print(str);
		w.close();
	}
	
	private boolean load(int page) {
		
		int pageSize = 1000;
		
		try {
			String url = BASE_URL + "?n=" + pageSize + "&p=" + page;
			
			Document doc;
			
			File local = new File(CACHE_DIR, "batc-n1000-p" + page + ".html");
			if (local.exists()) {

				doc = Jsoup.parse(local, "UTF-8", url);				
				
			} else {
				Connection conn = Jsoup.connect(url);
				conn.timeout(120 * 1000);
				
				conn.method(Method.POST);
				
				doc = conn.get();

				PrintWriter w = new PrintWriter(new FileWriter(local));
				
				w.print(doc);
				w.close();
			}
			
			Elements tracks = doc.select(".product-container .product-desc");

			log.info("Detected " + tracks.size() + " tracks in page " + page);
			
			if (tracks.size() < 1) {
				log.error("Unparseable response from batc: " + doc);
				return false;
			}

			int okay = 0;
			int bad = 0;
			
			for (Element track : tracks) {
				Metadata md = processTrack(track);
				if (md != null) {
					list.add(md);
					okay ++;
				} else {
					bad ++;
				}
			}

			log.info("Processed " + okay + " good and " + bad + " bad tracks");

			return true;
		
		} catch (Exception ex) {
			throw new SystemException(ex);
		}
		
		
	}

	private Metadata processTrack(Element track) {
		
		String line = track.text();
		String html = track.html();
		log.debug("Parsing " + html);

		// Interprete ROSSI, RAFAEL Album: VALSES, RANCHERAS Y PASODOBLES Track: 15 Tema: LAMBETE JUANCHO QUE ESTAS HUEVO
		
		String album = null;
		String num = null;
		String title = null;
		String artist = null;
		
		Pattern pattern = Pattern.compile("Interprete (.*) Album: (.*) Track: (.*) Tema: (.*)");
		
		Matcher m = pattern.matcher(line);
		if (m.matches()) {
			artist = m.group(1);
			album = m.group(2);
			num = m.group(3);
			title = m.group(4);
			
		} else {
			log.error("Unparsable line: " + line);
		}
		
		Metadata md = new Metadata();
		md.artist = unescape(artist);
		md.album = unescape(album);
		md.title = unescape(title);
		md.num = unescape(num);

		log.debug("Parsed " + line + " to " + md.toString());
		
		return md;
		
	}
	
	private static String unescape(String text) {
		if (text == null) {
			return null;
		}
		String out = Jsoup.parse(text).text();
		out = out.replaceAll("\"", "");
		return out;
	}
	
}
