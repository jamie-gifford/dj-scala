package au.com.thoughtpatterns.dj.disco.tangotunes;

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

/**
 * Interface with TangoTunes
 * 
 *  https://www.tangotunes.com/catalogsearch/advanced/result/?limit=all&tt_genre[]=149&tt_genre[]=147&tt_genre[]=148
 *  
 *  limit=100&p=1
 */
public class TangoTunes {

	private static final Logger log = Logger.get(TangoTunes.class);

	private static final String BASE_URL = "https://www.tangotunes.com/catalogsearch/advanced/result/?tt_genre[]=149&tt_genre[]=147&tt_genre[]=148";

	public static class Metadata {
		public String title;
		public String artist;
		public String genre;
		public String date;
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
		for (Metadata md : list) {
			String[] line = new String[] {
			    md.genre,
			    md.artist,
			    md.title,
			    md.date
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
		
		int pageSize = 100;
		
		try {
			String url = BASE_URL + "&limit=" + pageSize + "&p=" + page;
			
			Document doc;
			
			File local = new File("tangotunes-" + page + ".html");
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
			
			Elements tracks = doc.select(".product-listing .product-description");

			log.info("Detected " + tracks.size() + " tracks in page " + page);
			
			if (tracks.size() < 1) {
				log.error("Unparseable response from tangotunes: " + doc);
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
			
			Element amt = doc.select(".advanced-search-amount").first();

			if (amt == null) {
				return false;
			}
			
			String amtStr = amt.text();
			// should start with number
			Pattern p = Pattern.compile("^([0-9]+).*");
			Matcher m = p.matcher(amtStr);
			if (! m.matches()) {
				return false;
			}
			int total = Integer.parseInt(m.group(1));
			log.info("Total tracks: " + total + " from " + amt.html());
			return total > page * pageSize;
		
		} catch (Exception ex) {
			throw new SystemException(ex);
		}
		
		
	}

	private Metadata processTrack(Element track) {
		
		log.debug("Parsing " + track.html());
		
		String title = null;
		String date = null;
		String artist = null;
		String genre = null;
		String singer = null;
		
		Elements titles = track.select("h4 a");
		if (titles.size() == 1) {
			title = titles.get(0).text();
		}

		Elements p = track.select("p");
		String str = p.html();
		
		int i = str.indexOf("<br />");
		if (i >= 0) {
			String bits = str.substring(0, i).trim();

			Pattern pattern = Pattern.compile("(.*), *(Tango|Milonga|Vals)(, *([-0-9]+))?");
			
			Matcher m = pattern.matcher(bits);
			if (m.matches()) {
				artist = m.group(1);
				genre = m.group(2);
				date = m.group(4);
			} else {
				log.error("Unparsable bits: " + bits);
			}
			
			singer = str.substring(i + 6).trim();
			if (! "Instrumental".equals(singer)) {
				artist = artist + ", voc. " + singer;
			}
		}
		
		Metadata md = new Metadata();
		md.artist = unescape(artist);
		md.date = unescape(date);
		md.title = unescape(title);
		md.genre = unescape(genre);
		
		return md;
		
	}
	
	private static String unescape(String text) {
		if (text == null) {
			return null;
		}
		return Jsoup.parse(text).text();
	}
	
}
