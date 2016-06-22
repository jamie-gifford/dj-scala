package au.com.thoughtpatterns.djs.model

import au.com.thoughtpatterns.core.util.CsvUtils
import java.io.FileReader
import java.text.Collator
import java.util.Locale
import scala.collection.JavaConversions._
import au.com.thoughtpatterns.djs.disco.Types.TiAlbumSide
import au.com.thoughtpatterns.djs.util.Log

class TdjModel {

  case class Metadata(trackNo: Integer, title: String, artist: String, date: String, genre: String, album: String)

  lazy val atAlbums = {
    val utils = new CsvUtils();
    val rows = utils.fromCsv(new FileReader("tango-at.csv"))
    val metas = (for (row <- rows) yield Metadata(Integer.parseInt(row(1)), row(2), row(3), row(4), row(5), row(8))).toList.sortBy({ _.trackNo })
    metas.groupBy({ _.album })
  }

  lazy val identify: Map[String, TiAlbumSide] = {
    val total = atAlbums.keys.size
    Log.info("Identifying " + total + " albums")

    val collator = Collator.getInstance(Locale.forLanguageTag("es"));
    collator.setStrength(Collator.PRIMARY)

    val albumNames = atAlbums.keys.toList.sorted

    (for (albumName <- albumNames) yield {

      val TMP = atAlbums.getOrElse(albumName, List.empty)

      /*
      val items = for (metas <- atAlbums.getOrElse(albumName, List.empty); meta <- metas) 
        yield Item(Some(meta.trackNo), meta.title, meta.artist)
      
      val id = new AlbumIdentifier(items)
      */

      (1 -> 2)

    }).toMap

    null
  }

}
/*

// Experimental
public class Discography {
    
    private static final Logger log = Logger.get(Discography.class);

    private AlbumIdentifier identifier = new AlbumIdentifier();
    
    
    public Map<String, AlbumSide> identify() {
        int total = atAlbums.size();
        log.info("Identifying " + total + " albums");
        
        Map<String, AlbumSide> map = new HashMap<>();
        
        int done = 0;
        
        for (String albumName : albumNames) {
            
        	done ++;
        	
            if (Util.empty(albumName)) {
                continue;
            }
            
            log.info(String.format("Doing %d of %d", done, total));
            
            List<Metadata> list = atAlbums.get(albumName);
            
            Collections.sort(list, cmp);
            
            identifier.clearTracks();
            
            for (Metadata m : list) {
                
                identifier.addTrack(m.artist, m.title, m.trackNumber);
                
            }
            
            List<AlbumSide> identified = identifier.identify();
            
            String out = "";
                
            Album ab = identified.get(0).album;
            int side = identified.get(0).side;
            out += " " + ab.toString() + " side " + side;
            
            int diffCount = 0;
            
            for (Metadata m : list) {
                
                int track = m.trackNumber;
                Track t = ab.getTrack(side, track);
                String trackName = t != null ? t.title : null;
                
                int same = -1;
                if (trackName != null) {
                    
                    same = collator.compare(clean(m.title), clean(trackName));
                }
                
                String diff = trackName;
                if (same != 0) {
                    diff = m.title + " => " + trackName;
                    diffCount ++;
                } else {
                    diff = diff + " (" + m.artist + ", " + m.date + " => " + t.orchestra + ", " + t.perfDate + ")"; 
                }
                
                out += "  " + m.trackNumber + " " + diff + "\n";
                
            }
            
            if (diffCount > list.size() / 3) {
                out = "NO MATCH\n" + out;
            } else {
            	
            	map.put(albumName, identified.get(0));
            	
            }
            
            out = albumName + "\n" + diffCount + " differences\n" + out;
            
            log.info(out);
            
        }
        
        return map;
    }

    private String clean(String in) {
        return in.replaceAll("[,\\|\\(\\)\\.!?]", " ").replaceAll(" +", " ").trim();
    }
    
}
*/
