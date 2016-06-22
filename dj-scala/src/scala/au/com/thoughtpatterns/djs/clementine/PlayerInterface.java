package au.com.thoughtpatterns.djs.clementine;

import java.net.URL;

import au.com.thoughtpatterns.djs.lib.MusicFile;
import au.com.thoughtpatterns.djs.lib.PlaylistFile;

public interface PlayerInterface {

	public void addTrack(MusicFile m);
	public void addTrack(PlaylistFile m);
	
	public URL getTrackLocation(int i) throws Exception;	
	public int getLength() throws Exception;
	public Integer getCurrentIndex()  throws Exception;
	
	public URL getCurrentTrack();
}
