package au.com.thoughtpatterns.djs.model

import java.text.Collator
import au.com.thoughtpatterns.core.util.CsvUtils
import java.util.Locale
import scala.collection.JavaConversions._
import au.com.thoughtpatterns.djs.util.Log
import java.io.FileReader
import au.com.thoughtpatterns.djs.disco.Types.TiAlbumSide
import au.com.thoughtpatterns.djs.disco.Types.TINT

class BatcModel {
  
  case class Metadata(artist: String, title: String, album: String, trackNo: Integer)

  lazy val batcAlbums = {
    val utils = new CsvUtils();
    val rows = utils.fromCsv(new FileReader("batc.csv"))
    val metas = (for (row <- rows) yield Metadata(row(0), row(1), row(2), Integer.parseInt(row(3)))).toList.sortBy({ _.trackNo })
    metas.groupBy({ _.album })
  }

  lazy val identify: Map[String, TiAlbumSide] = {
    val total = batcAlbums.keys.size
    Log.info("Identifying " + total + " albums")

    val collator = Collator.getInstance(Locale.forLanguageTag("es"));
    collator.setStrength(Collator.PRIMARY)

    val albumNames = batcAlbums.keys.toList.sorted

    (for (albumName <- albumNames) yield {

      val TMP = batcAlbums.getOrElse(albumName, List.empty)

      Log.info("Identifying " + albumName + " with " + TMP.size + " tracks");

      val items = for (meta <- batcAlbums.getOrElse(albumName, List.empty)) 
        yield Item(Some(meta.trackNo), meta.title, meta.artist)
      
      val id = new AlbumIdentifier(items)
      
      val x = id.identify
      
      if (x.size > 0) {
        val side = x.get(0);
      
        for (t <- TMP) {
          val tint = TINT(side.tin, side.side, t.trackNo)
          val track = id.model.getTrack(tint);
          for (i <- track) {
            Log.info("IDENTIFIED: " + t + " => " + i);
          }
        }
        
      }
      
      (1 -> 2)

    }).toMap

    null
  }

}
