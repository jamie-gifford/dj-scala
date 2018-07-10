package au.com.thoughtpatterns.dj.disco.tangotunes;

import java.io.File;

import org.junit.Test;

public class TangoTunes_test {

	@Test
	public void test() throws Exception {
		
		TangoTunes t = new TangoTunes();
		t.toCSV(new File("tangotunes.csv"));
		
	}
	
}
