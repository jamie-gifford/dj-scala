package au.com.thoughtpatterns.dj.disco.tangoinfo;

import java.io.File;

import org.junit.Test;

import au.com.thoughtpatterns.core.util.Logger;


public class Tracks_Test {

    private static final Logger log = Logger.get(Tracks_Test.class);
    
    @Test
    public void load() throws Exception {
        
        Tracks ps = Tracks.loadFromWeb(new File("ti-tracks.csv"));
        //Tracks ps = Tracks.load(new File("ti-tracks.csv"));
        
    }

    /*
    @Test
    public void loadL() throws Exception {
        
        List<Track> ts = Tracks.loadFromWeb("L");
        
        for (Track t : ts) {
            
            if ("00099441337028-1-1".equals(t.tint)) {
                log.info("Got " + t);
            }
            
        }
        
    }
    */
    
}
