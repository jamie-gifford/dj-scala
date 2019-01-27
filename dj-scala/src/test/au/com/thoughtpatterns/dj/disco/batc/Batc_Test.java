package au.com.thoughtpatterns.dj.disco.batc;

import java.io.File;

import org.junit.Test;

public class Batc_Test {

	@Test
	public void test() throws Exception {
		
		Batc t = new Batc();
		t.toCSV(new File("batc.csv"));
		
	}

}
