package au.com.thoughtpatterns;

import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.dj.disco.spotify.Spotify;

public class Spotify_Test {

	private static final Logger log = Logger.get(Spotify_Test.class);

	public static void main(String[] args) throws Exception {

		String access = "BQCZhDIM0RLXsgE-gakoVUXKx5QlS0dwzPtL4_--UYpuNllDhTrBkcJpSSIrYhTFgSaHDDfLrjn0xkvxvxg61L_L4bLjOtBYrTzKgfM62tFqPWcANgQTiIISLzdOrvaosjdRxox9K7RfQk-DKGL6aSRNKCvTRam7PIPTx1_A1wWu2trLSYrlx4yk5ZVcOeIdB-rutd4p1uJMPSNN";
		Spotify s = new Spotify(access);

		s.dumpStats();
		/*
		
		for (String artist : Spotify.ARTISTS) {
			s.fetchTracks(artist);
		}
		*/
		
		/*
		
		String req = "search?type=track&q=genre:tango%20" + URLEncoder.encode("Osvaldo Pugliese", "UTF-8");
		Jsony out = s.request(req);
		log.info(out.toJson());		*/

	}
	
}
