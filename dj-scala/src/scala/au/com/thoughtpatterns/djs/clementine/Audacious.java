package au.com.thoughtpatterns.djs.clementine;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import scala.collection.Iterator;
import au.com.thoughtpatterns.core.util.Resources;
import au.com.thoughtpatterns.core.util.SystemException;
import au.com.thoughtpatterns.djs.lib.MusicFile;
import au.com.thoughtpatterns.djs.lib.PlaylistFile;

public class Audacious implements PlayerInterface {

	@Override
	public URL getTrackLocation(int i) throws Exception {
		throw new SystemException("unimplemented");
	}

	@Override
	public int getLength() throws Exception {
		String index = audtool("playqueue-length");
		if (index == null) {
			return 0;
		}
		index = index.trim();
		int i = Integer.parseInt(index);
		return i;

	}

	@Override
	public Integer getCurrentIndex() throws Exception {
		throw new SystemException("unimplemented");
	}

	@Override
	public void setCurrentIndex(int i) throws Exception {
		throw new SystemException("unimplemented");
	}

	@Override
	public void setCurrentIndex(int i, URL checkUrl) throws Exception {
		throw new SystemException("unimplemented");
	}

	@Override
	public URL getCurrentTrack() {
		try {
			String filename = audtool("current-song-filename");
			File file = new File(filename.trim());
			return file.toURI().toURL();
		} catch (Exception ex) {
			throw new SystemException("Failed to get current track from audiacious", ex);
		}
	}
	
	@Override
	public void addTrack(MusicFile m) {
		try {
			String flename = m.file().getAbsolutePath();
			audtool("playlist-addurl", flename);
		} catch (Exception ex) {
			throw new SystemException(ex);
		}
	}

	@Override
	public void addTrack(PlaylistFile m) {
		Iterator<MusicFile> iter = m.tracks().iterator();
		while (iter.hasNext()) {
			addTrack(iter.next());
		}
	}
	

	public static String audtool(String... args) throws Exception {

		List<String> cmd = new ArrayList<>();
		cmd.add("audtool");

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
