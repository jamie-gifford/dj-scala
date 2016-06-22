package au.com.thoughtpatterns.dj.disco.tangodjat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TangoDJAt_Test {

	@Test
	public void testArtist() throws IOException {
		
		TangoDJAt a = new TangoDJAt();
		
		//a.fetchArtists();
		a.fetchArtist("Brunswick");
		
		a.dump(new File("/tmp/tango-at.csv"));
		
	}
	
}
