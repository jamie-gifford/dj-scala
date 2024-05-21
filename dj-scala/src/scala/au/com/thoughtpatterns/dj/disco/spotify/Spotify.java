package au.com.thoughtpatterns.dj.disco.spotify;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.thoughtpatterns.core.json.AJsonyObject;
import au.com.thoughtpatterns.core.json.Jsony;
import au.com.thoughtpatterns.core.json.JsonyArray;
import au.com.thoughtpatterns.core.json.JsonyObject;
import au.com.thoughtpatterns.core.json.JsonyParser;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.ProfilePoint;
import au.com.thoughtpatterns.core.util.Resources;
import au.com.thoughtpatterns.core.util.SystemException;
import au.com.thoughtpatterns.core.util.Util;

public class Spotify {

	private static final Logger log = Logger.get(Spotify.class);
	
    public static final String Rodolfo_Biagi = "Rodolfo Biagi";

    public static final String Miguel_Caló = "Miguel Caló";

    public static final String Francisco_Canaro = "Francisco Canaro";

    public static final String Adolfo_Carabelli = "Adolfo Carabelli";

    public static final String Alberto_Castillo = "Alberto Castillo";

    public static final String Angel_DAgostino = "Angel D'Agostino";

    public static final String Juan_DArienzo = "Juan D'Arienzo";

    public static final String Alfredo_De_Angelis = "Alfredo De Angelis";

    public static final String Julio_De_Caro = "Julio De Caro";

    public static final String Lucio_Demare = "Lucio Demare";

    public static final String Carlos_Di_Sarli = "Carlos Di Sarli";

    public static final String Edgardo_Donato = "Edgardo Donato";

    public static final String Roberto_Firpo = "Roberto Firpo";

    public static final String Francini_Pontier = "Francini - Pontier";

    public static final String Osvaldo_Fresedo = "Osvaldo Fresedo";

    public static final String Alfredo_Gobbi = "Alfredo Gobbi";

    public static final String Pedro_Laurenz = "Pedro Laurenz";

    public static final String Francisco_Lomuto = "Francisco Lomuto";

    public static final String Osmar_Maderna = "Osmar Maderna";

    public static final String Juan_Maglio = "Juan Maglio";

    public static final String Ricardo_Malerba = "Ricardo Malerba";

    public static final String Orquesta_Típica_Victor = "Orquesta Típica Victor";

    public static final String Astor_Piazzolla = "Astor Piazzolla";

    public static final String Osvaldo_Pugliese = "Osvaldo Pugliese";

    public static final String Enrique_Rodríguez = "Enrique Rodríguez";

    public static final String Francisco_Rotundo = "Francisco Rotundo";

    public static final String Horacio_Salgán = "Horacio Salgán";

    public static final String Florindo_Sassone = "Florindo Sassone";

    public static final String Ricardo_Tanturi = "Ricardo Tanturi";

    public static final String Aníbal_Troilo = "Aníbal Troilo";

    public static final String Héctor_Varela = "Héctor Varela";

    public static final String Miguel_Villasboas = "Miguel Villasboas";

    public static final String[] ARTISTS = { Rodolfo_Biagi, Miguel_Caló, Francisco_Canaro,
            Adolfo_Carabelli, Alberto_Castillo, Angel_DAgostino, Juan_DArienzo, Alfredo_De_Angelis,
            Julio_De_Caro, Lucio_Demare, Carlos_Di_Sarli, Edgardo_Donato, Roberto_Firpo,
            Francini_Pontier, Osvaldo_Fresedo, Alfredo_Gobbi, Pedro_Laurenz, Francisco_Lomuto,
            Osmar_Maderna, Juan_Maglio, Ricardo_Malerba, Orquesta_Típica_Victor, Astor_Piazzolla,
            Osvaldo_Pugliese, Enrique_Rodríguez, Francisco_Rotundo, Horacio_Salgán,
            Florindo_Sassone, Ricardo_Tanturi, Aníbal_Troilo, Héctor_Varela, Miguel_Villasboas };


	
	private File f = new File("spotify.json");

	private String accessToken;

	private static final String base = "https://api.spotify.com/v1/";
	
	private JsonyObject ids = null;

	public Spotify(String access) {
		accessToken = access;
	}

	class SObj {
		String id;
		String uri;
		Jsony jsony;
	}
	
	class Track extends SObj {
		String name;	
		
		Album album;
		List<Artist> artists = new ArrayList<>();
		
		public String toString() {
			
			return name + " from " + album.name + " by " + Util.join(", ", artists);
			
		}
		
	}
	
	class Album extends SObj {
		String name;
		String releaseDate;
		String releaseDatePrecision;
		int tracks;
	}
	
	class Artist extends SObj {
		String name;
		
		public String toString() {
			return name;
		}
	}
	
	Map<String, Album> albums = new HashMap<>();
	Map<String, Artist> artists = new HashMap<>();
	Map<String, Track> tracks = new HashMap<>();
	
	private void register(JsonyObject in) {
		if (ids == null) {
			loadIds();
		}
		String id = in.getCast("id", String.class);
		ids.set(id, in);
		
	}
	
	private void loadIds() {
		if (ids != null) {
			return;
		}
		try {
			if (f.exists()) {

				ProfilePoint p = new ProfilePoint("spotify", "load");
				p.start();
				Reader r = new FileReader(f);
				ids = (JsonyObject) new JsonyParser().parse(r);
				r.close();
				p.stop();
				
			}
		} catch (Exception ex) {
			log.error("Failed to load " + f, ex);
		}
		if (ids == null) {
			ids = new AJsonyObject();
		}
	}
	
	public void save() {
		if (ids == null) {
			return;
		}
		try {
			ProfilePoint p = new ProfilePoint("spotify", "save");
			p.start();
			
			File tmp = File.createTempFile("spotify", ".json");
			
			PrintWriter w = new PrintWriter(new FileWriter(tmp));
			w.print(ids.toJson());
			w.close();
			
			f.delete();
			tmp.renameTo(f);
			
			p.stop();
			log.info("Saved " + ids.getPropertyNames().size() + " ids");
		} catch (Exception ex) {
			throw new SystemException("Failed to write " + f, ex);
		}
	}
	
	private Album toAlbum(JsonyObject in) {
		if (in == null) {
			return null;
		}
		String id = in.getCast("id", String.class);
		Album o = albums.get(id);
		if (o != null) {
			return o;
		}
		register(in);

		o = new Album();
		o.jsony = in;
		o.id = id;
		o.name = in.getCast("name", String.class);
		o.releaseDate = in.getCast("release_date", String.class);
		o.releaseDatePrecision = in.getCast("release_date_precision", String.class);
		albums.put(id, o);
		return o;
	}
	private Artist toArtist(JsonyObject in) {
		if (in == null) {
			return null;
		}
		String id = in.getCast("id", String.class);
		Artist o = artists.get(id);
		if (o != null) {
			return o;
		}
		register(in);
		o = new Artist();
		o.jsony = in;
		o.id = id;
		o.name = in.getCast("name", String.class);
		artists.put(id, o);
		return o;
	}
	private Track toTrack(JsonyObject in) {
		if (in == null) {
			return null;
		}
		String id = in.getCast("id", String.class);
		Track o = tracks.get(id);
		if (o != null) {
			return o;
		}
		register(in);
		o = new Track();
		o.jsony = in;
		o.id = id;
		o.name = in.getCast("name", String.class);
		tracks.put(id, o);
		
		JsonyArray<JsonyObject> artists = (JsonyArray) in.get("artists");
		if (artists != null) {
			for (JsonyObject a : artists) {
				o.artists.add(toArtist(a));
			}
		}
		
		JsonyObject album = (JsonyObject) in.get("album");
		o.album = toAlbum(album);
		
		return o;
	}
	
	public void fetchTracks(String artist) {
		
		Calendar cal = new GregorianCalendar();
		int year = cal.get(GregorianCalendar.YEAR);
		
		fetchTracks(artist, 1900, year + 1);
		
	}
	
	public void fetchTracks(String artist, int fromYearInc, int toYearEx) {

		int offset = 0;
		boolean done = false;
		int limit = 50;
	
		int MAX = 2000;
		
		if (toYearEx - fromYearInc <= 1) {
			return;
		}
		
		String range = fromYearInc + "-" + (toYearEx-1);
		
		while (! done) {

			String req = null;
			try {
				req = "search?limit=" + limit + "&offset=" + offset + "&type=track&q=genre:tango%20year:" + range + "%20" + URLEncoder.encode(artist, "UTF-8");
			} catch (Exception ignored) {}

			JsonyObject j = (JsonyObject) request(req);
			
			JsonyObject tracks = (JsonyObject) j.get("tracks");
			long total = tracks.getCast("total", Long.class);

			log.info("Fetch " + offset + " of " + total + " for " + artist + " in " + range);
			
			if (total > MAX) {
				if (toYearEx - fromYearInc > 1) {
					
					int mid = (toYearEx + fromYearInc) / 2;
					
					fetchTracks(artist, fromYearInc, mid);
					fetchTracks(artist, mid, toYearEx);
					
					return;
					
				}
			}
			
			offset += limit;
			done = total < offset;
			
			JsonyArray<JsonyObject> items = (JsonyArray) tracks.get("items");
			
			for (JsonyObject item : items) {
								
				Track t = toTrack(item);
				log.info(t.toString());
			}
		}
		
		save();
	}
	
	public Jsony request(String r) {
		
		Integer retrySecs = null;

		int cnt = 0;
		while (cnt < 10) {
			cnt ++;
			
			URL url = null;
			try {
				url = new URL(new URL(base), r);
			} catch (Exception ex) {
				throw new SystemException(ex);
			}
			
			log.debug(url.toExternalForm());
			
			try {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("Authorization", "Bearer " + accessToken);
				int status = conn.getResponseCode();
				
				if (status == 429) {
					// rate limiting	
					retrySecs = Integer.parseInt(conn.getHeaderField("retry-after"));
					
					try {
						Thread.sleep(retrySecs * 1000);
					} catch (InterruptedException ignore) {}
					
					continue;
				}
				
				InputStream in;
				try {
					in = conn.getInputStream();
				} catch (Exception ex) {
					in = conn.getErrorStream();
				}
				
				Reader reader = new InputStreamReader(in);
				
				String data = Resources.readString(reader);
				reader.close();
				
				if (status < 200 || status > 300) {
					throw new SystemException(status + ": " + data);
				}

				Jsony out = new JsonyParser().parse(data);
				reader.close();

				return out;
			} catch (Exception ex) {
				throw new SystemException(ex);
			}
		}

		return null;
	}
	
	public void dumpStats() {
		loadIds();
		
		Map<String, Integer> cnts = new HashMap<>();
		
		Set<String> names = ids.getPropertyNames();
		for (String id : names) {
			JsonyObject obj = (JsonyObject) ids.get(id);
			String type = obj.getCast("type", String.class);
			
			if (cnts.get(type) == null) {
				cnts.put(type, 1);
			} else {
				cnts.put(type, 1 + cnts.get(type));
			}
			
		}
		
		for (String type : cnts.keySet()) {
			log.info(type + ": " + cnts.get(type));
		}
	}
}
