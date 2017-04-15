package au.com.thoughtpatterns.dj.disco.tangoinfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.helper.DataUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Resources;
import au.com.thoughtpatterns.core.util.SystemException;
import au.com.thoughtpatterns.core.util.Util;

/**
 * Interface with https://tango.info
 */
public class TangoInfo {

	private static final Logger log = Logger.get(TangoInfo.class);

	private static final String BASE_URL = "https://tango.info/tracktagger/1.4.0";

	private static final String SEPARATOR = ";";

	private static final int MAX_TRACKS = 20;
	
	private static final String[] FIELDS = new String[] { "in_count", "tinp",
			"album", "discnumber", 
			"title", "artist", "genre", "date", "tracknumber" };

	public static class TINT {
		String tinp;
		int discNo;
		int trackNo;
		
		public TINT(String tinp, int discNo, int trackNo) {
			this.tinp = tinp;
			this.discNo = discNo;
			this.trackNo = trackNo;
		}
		
		public static TINT parse(String str) {
			String[] bits = str.split("-");
			String tinp = bits[0];
			int discNo = Integer.parseInt(bits[1]);
			int trackNo = Integer.parseInt(bits[2]);
			return new TINT(tinp, discNo, trackNo);
		}
		
		public String toString() {
			return tinp + "-" + discNo + "-" + trackNo;
		}
		
		public boolean equals(Object other) {
			if (!(other instanceof TINT)) {
				return false;
			}
			TINT t = (TINT) other;
			return tinp.equals(t.tinp) && discNo == t.discNo && trackNo == t.trackNo;
		}
		
		public int hashCode() {
			return tinp.hashCode() + discNo * 17 + trackNo * 41;
		}
	}
	
	public static class Metadata {
		public String tinp;
		public String album;
		public int discNumber;
		public int trackNumber;
		public String title;
		public String artist;
		public String genre;
		public String date;

		public TINT key() {
			return new TINT(tinp, discNumber, trackNumber);
		}
	}

	// Map from "tin;discno;trackno" to map of field->data
	private Map<TINT, Metadata> data = new HashMap<>();
	
	private Set<String> tinsFetched = new HashSet<>();

	// Tints pending to fetch
	private Set<TINT> tintsPending = new HashSet<>();

	// TINS pending to fetch
	private Set<String> tinsPending = new HashSet<>();
	
	public void fetchTIN(String tin) {

		if (tinsFetched.contains(tin)) {
			return;
		}

		tinsPending.add(tin);
	}
	
	public void fetchTINT(String tin, int discNo, int trackNo) {
		TINT tint = new TINT(tin, discNo, trackNo);
		tintsPending.add(tint);
	}
	
	
	private void loadPending() {
		
		if (tintsPending.size() + tinsPending.size() == 0) {
			return;
		}

		List<String> list = new ArrayList<>(tinsPending);
		for (TINT tint : tintsPending) {
			if (tinsPending.contains(tint.tinp) || tinsFetched.contains(tint.tinp)) {
				continue;
			}
			list.add(tint.toString());
		}
		
		// Load in sensible blocks
		List<String> accum = new ArrayList<>();

		int size = 0;
		
		for (String item : list) {
			accum.add(item);
			
			if (item.contains("-")) {
				size += 1;
			} else {
				size += 20;
			}
			
			if (size > MAX_TRACKS) {
				loadList(accum);
				accum.clear();
				size = 0;
			}
		}
		
		loadList(accum);
	}
	
	private void loadList(List<String> list) {
		
		if (list.size() == 0) {
			return;
		}
		
		String refs = Util.join("\r\n", list);
		
		log.debug("Loading refs\n" + refs);
		
		String format = "%" + Util.join("%;%", FIELDS) + "%";

		try {
			String url = BASE_URL;
			
			Connection conn = Jsoup.connect(url);
			
			conn.method(Method.POST);
			conn.data("track_references", refs);
			conn.data("format_string", format);
			
			Document doc = conn.get();

			// log.debug("Read " + doc);
			
			Elements textareas = doc.select("legend + br + textarea");

			if (textareas.size() < 1) {
				log.error("Unparseable response from tango.info: " + doc);
				return;
			}
			
			Element element = textareas.get(0);

			FormattingVisitor formatter = new FormattingVisitor();
			NodeTraversor traversor = new NodeTraversor(formatter);
			traversor.traverse(element);

			String out = formatter.toString();

			String[] records = out.split("\n");

			// log.debug("Parsing " + records.length + " records received from tango.info");
			
			for (String record : records) {
				processRecord(record);
			}

		} catch (Exception ex) {
			throw new SystemException(ex);
		}

		tintsPending.clear();
		tinsPending.clear();
		
	}

	private void processRecord(String record) {
		if (record == null) {
			return;
		}
		String[] fields = record.split(SEPARATOR);

		if (fields.length != FIELDS.length) {
			log.error("Unparseable record with " + fields.length
					+ " records instead of " + FIELDS.length + ":\n" + record);
			return;
		}

		Metadata d = new Metadata();
		for (int i = 0; i < FIELDS.length; i++) {
			String field = FIELDS[i];
			String value = fields[i];

			switch (field) {
			case "tinp":
				d.tinp = value;
				break;
			case "album":
				d.album = value;
				break;
			case "discnumber":
				d.discNumber = Integer.parseInt(value);
				break;
			case "tracknumber":
				d.trackNumber = Integer.parseInt(value);
				break;
			case "title":
				d.title = value;
				break;
			case "artist":
				d.artist = value;
				break;
			case "genre":
				d.genre = value;
				break;
			case "date":
				d.date = value;
				break;
			default:
			}
		}

		TINT key = d.key();
		data.put(key, d);
	}
	
	public List<Work> fetchWorks() {
		List<Work> out = new ArrayList<>();
		for (char letter = 'A'; letter <= 'Z'; letter++) {
			fetchWorks(Character.toString(letter), out);
		}
		return out;
	}
	
	private void fetchWorks(String letter, List<Work> out) {
		
		log.debug("Fetching works for " + letter);
		
		String url0 = "https://tango.info/works";
		
		try {
			/*
			String url = url0 + "/" + letter;
			Connection conn = Jsoup.connect(url).header("Accept-Encoding", "gzip");
			conn.method(Method.POST);
			Document doc = conn.get();
			*/
			
			URL f = new URL("file:tangoinfo/works/" + letter);
			Document doc = loadDoc(f);
			
			// log.debug("Read " + doc);
			
			Elements rows = doc.select("#works tbody tr");

			if (rows.size() < 1) {
				log.error("Unparseable response from tango.info: " + doc);
				return;
			}
			
			for (Element row : rows) {
				
				Elements tds = row.select("td");
				
				if (tds.size() == 12) {
					
					Work w = new Work();
					
					w.title = tds.get(0).text();
					w.genre = tds.get(2).text();
					w.composer = tds.get(3).text();
					w.letrista = tds.get(4).text();
					w.tiwc = tds.get(6).text();

					out.add(w);
				}
			}

		} catch (Exception ex) {
			throw new SystemException(ex);
		}

	}
	
	public static class Work {
		
		String title;
		String genre;
		String composer;
		String letrista;
		String tiwc;
		
		public String toString() {
			return title + " - " + composer + ", lyr. " + letrista;
		}
		
	}
	
	public List<Performance> fetchPerformances() {
		List<Performance> out = new ArrayList<>();
		for (char letter = 'A'; letter <= 'Z'; letter++) {
			fetchPerformances(Character.toString(letter), out);
		}
		return out;
	}
	
	private void fetchPerformances(String letter, List<Performance> out) {
		
		try {
			URL f = new URL("file:tangoinfo/performances/" + letter);
			Document doc = loadDoc(f);

			// log.debug("Read " + doc.outerHtml());
			
			Elements rows = doc.select("tr.performance");

			log.debug("Fetching works for " + letter + " : " + rows.size() + " rows");

			if (rows.size() < 1) {
				log.info("Unparseable response from tango.info");
				return;
			}

			
			for (Element row : rows) {
				
				Elements tds = row.select("td");
				
				Performance w = new Performance();
				
				w.title = tds.get(0).text();
				w.tiwc = tds.get(1).text();
				w.genre = tds.get(2).text();
				w.orchestra = tds.get(3).text();
				w.vocalist = tds.get(4).text();
				w.date = tds.get(6).text();
				w.duration = tds.get(7).text();

				out.add(w);
			}

		} catch (Exception ex) {
			throw new SystemException(ex);
		}

	}

	public List<Track> fetchTracks() {
		List<Track> out = new ArrayList<>();
		for (char letter = 'A'; letter <= 'Z'; letter++) {
			fetchTracks(Character.toString(letter), out);
		}
		return out;
	}
	
	private void fetchTracks(String letter, List<Track> out) {
		
		try {
			URL f = new URL("file:tangoinfo/tracks/" + letter);
			Document doc = loadDoc(f);

			// log.debug("Read " + doc.outerHtml());
			
			Elements rows = doc.select("table.listing.sortable tbody tr");

			log.debug("Fetching tracks for " + letter + " : " + rows.size() + " rows");

			if (rows.size() < 1) {
				log.info("Unparseable response from tango.info");
				return;
			}

			
			for (Element row : rows) {
				
				Elements tds = row.select("td");
				
				Performance w = new Performance();
				
				w.title = tds.get(1).text();
				w.tiwc = tds.get(2).text();
				w.genre = tds.get(3).text();
				w.orchestra = tds.get(4).text();
				w.vocalist = tds.get(5).text();
				w.date = tds.get(7).text();
				w.duration = tds.get(8).text();

				Track t = new Track();
				t.perf = w;
				t.tint = TINT.parse(tds.get(9).text());
				
				out.add(t);
			}

		} catch (Exception ex) {
			throw new SystemException(ex);
		}

	}

	private Document loadDoc(URL url) throws IOException {
		InputStream is = url.openStream();
		Document doc = DataUtil.load(is, "utf8", url.toString());
		return doc;
	}
	
	
	public static class Performance {
		
		String title;
		String tiwc;
		String genre;
		String orchestra;
		String vocalist;
		String date;
		String duration;
		
	}

	public static class Track {
		
		Performance perf;
		TINT tint;
	}

	
	public Metadata getMetadata(String tin, int discno, int trackno) {
		
		loadPending();
		
		TINT tint = new TINT(tin, discno, trackno);
		Metadata d = data.get(tint);
		return d;
	}
	
	private class FormattingVisitor implements NodeVisitor {

		private StringBuilder accum = new StringBuilder();

		public void head(Node node, int depth) {
			if (node instanceof TextNode) {
				String text = ((TextNode) node).getWholeText();
				append(text);
			}
		}

		public void tail(Node node, int depth) {
		}

		private void append(String text) {
			accum.append(text);
		}

		public String toString() {
			return accum.toString();
		}
	}

}
