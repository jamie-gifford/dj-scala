package au.com.thoughtpatterns.dj.disco.tangoinfo;

import java.io.Serializable;

import au.com.thoughtpatterns.core.util.Util;
import au.com.thoughtpatterns.djs.lib.Performance;
import au.com.thoughtpatterns.djs.util.RecordingDate;


public class Track implements Serializable {

    private static final long serialVersionUID = 1L;

    public String title, tiwc, genre, orchestra, vocalists, perfDate, duration, tint;
    
    private RecordingDate year;
    
    public String getArtist() {
        
        if (! Util.empty(vocalists) && ! vocalists.equals("-")) {
            return orchestra + ", voc. " + vocalists.trim(); 
        } else {
            return orchestra;
        }
        
    }

    public RecordingDate getYear() {
        if (year == null) {
            year = RecordingDate.parse(perfDate);
        }
        return year;
    }
    
    public Performance toSong() {
    	Performance s = new Performance(title, getArtist(), genre, getYear());
        return s;
    }
    
}
