package au.com.thoughtpatterns.djs.clementine;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mortbay.log.Log;

import au.com.thoughtpatterns.core.util.Resources;
import au.com.thoughtpatterns.core.util.SystemException;
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
	
	public URL getCurrentTrack() {
		
		try {
			Integer i = getCurrentIndex();
			if (i == null) {
				return null;
			}
			return getTrackLocation(i);
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

}
