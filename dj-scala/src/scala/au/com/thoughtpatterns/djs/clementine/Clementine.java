package au.com.thoughtpatterns.djs.clementine;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.thoughtpatterns.core.util.Resources;
import au.com.thoughtpatterns.core.util.SystemException;
import au.com.thoughtpatterns.core.util.Util;
import au.com.thoughtpatterns.djs.lib.MusicFile;
import au.com.thoughtpatterns.djs.lib.PlaylistFile;

public class Clementine implements PlayerInterface {

	public void addTrack(MusicFile m) {

		try {
			String location = m.file().toURI().toURL().toExternalForm();

			dbus("/TrackList", "AddTrack", location, "true");

			int length = getLength();
			
			dbus("/TrackList", "PlayTrack", "" + (length - 1));

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void addTrack(PlaylistFile m) {

		
		try {
			File file = m.file();
			String location = file.toURI().toURL().toExternalForm();

			dbus("/TrackList", "AddTrack", location, "true");

		} catch (Exception ex) {
		}

	}

	public URL getTrackLocation(int i) throws Exception {
                String md = dbus("/TrackList", "GetMetadata", "" + i);
		
		//Pattern loc = Pattern.compile("^location: (.+)$", Pattern.MULTILINE);
		Pattern loc = Pattern.compile(".*^location: ([^\\n]+)$.*", Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = loc.matcher(md);
		
		if (m.matches()) {
			String location = m.group(1);
			
			URL url = new URL(location);
			return url;
		}
		
		return null;
	}		

	public String getMetadata(int i) throws Exception {
		String md = dbus("/TrackList", "GetMetadata", "" + i);
		return md;
	}		

	public int getLength() throws Exception {
		String length = dbus("/TrackList", "GetLength");
		if (length == null) {
			return 0;
		}
		length = length.trim();
		int i = Integer.parseInt(length);
		return i;
	}

	public Integer getCurrentIndex() throws Exception {
		String index = dbus("/TrackList", "GetCurrentTrack");
		if (index == null) {
			return null;
		}
		index = index.trim();
		int i = Integer.parseInt(index);
		return i;
	}
	
	@Override
	public void setCurrentIndex(int i) throws Exception {
		dbus("/TrackList", "PlayTrack", "" + i);
	}
	
	@Override
	public void setCurrentIndex(int i, URL checkUrl) throws Exception {
		
		long start = System.currentTimeMillis();
		while (true) {
			
			boolean okay = Util.equals(getCurrentIndex(), i) && Util.equals(checkUrl, getCurrentTrack());
			if (okay) {
				break;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				
			}
			
			setCurrentIndex(i);
			
			long now = System.currentTimeMillis();
			if (now - start > 5000) {
				break;
			}
			
		}
		
	}
	
        public URL getCurrentTrack() {
            try {
                String md = dbus("/org/mpris/MediaPlayer2", "org.mpris.MediaPlayer2.Player.Metadata");

                Pattern loc = Pattern.compile(".*^xesam:url: ([^\\n]+)$.*", Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = loc.matcher(md);
		
		if (m.matches()) {
                    String location = m.group(1);

                    URL url = new URL(location);
                    return url;
                } else {
                    return null;
                }


            } catch (Exception ex) {
                throw new SystemException(ex);
            }
        }

	public static String dbus(String... args) throws Exception {

		List<String> cmd = new ArrayList<>();
		cmd.add("qdbus");
		cmd.add("org.mpris.MediaPlayer2.clementine");

		for (String s : args) {
			cmd.add(s);
		}

		ProcessBuilder b = new ProcessBuilder().command(cmd);
		Process p = b.start();

		InputStream stdout = p.getInputStream();

		p.waitFor();

		byte[] data = Resources.readByteArray(stdout);

		String out = new String(data);
		
		return out;
	}
	
	public static class Song {
		
		public String url;
		public String artist;
		public String title;
		public String album;
		public Integer track;
		public Integer seconds;	
		
		public String toString() {
			return title + ", " + artist + ", " + album;
		}
		
	}
	
	public List<Song> getTracks() throws Exception {
		
		int length = getLength();
		List<Song> out = new ArrayList<>();
		
		for (int i = 0; i < length; i++) {
			String md = getMetadata(i);
			
			Map<String, String> meta = metadata(md);
			
			Song s = new Song();
			s.album = meta.get("album");
			s.artist = meta.get("artist");
			s.title = meta.get("title");
			s.track = parseInt(meta.get("tracknumber"));
			s.seconds = parseInt(meta.get("time"));
			s.url = meta.get("location");
					
			out.add(s);
		}
		
		return out;
	}
	
	private Map<String, String> metadata(String md) {
		String[] lines = md.split("\n");
		Pattern p = Pattern.compile("^([^:]+): (.*)");
		Map<String, String> out = new HashMap<>();
		for (String line : lines) {
			Matcher m = p.matcher(line);
			if (m.matches()) {
				String key = m.group(1);
				String val = m.group(2);
				out.put(key,  val);
			}
		}
		return out;
	}
	
	private Integer parseInt(String in) {
		if (Util.empty(in)) {
			return null;
		}
		return Integer.parseInt(in);
	}

}
