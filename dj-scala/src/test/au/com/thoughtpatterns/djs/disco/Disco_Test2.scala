package au.com.thoughtpatterns.djs.disco

import au.com.thoughtpatterns.djs.disco.Types._
import au.com.thoughtpatterns.djs.disco.Disco.TangoInfo.TiTrack
import au.com.thoughtpatterns.djs.disco.Disco.Gordon.GordonTrack
import java.text.Collator
import java.util.Locale
import au.com.thoughtpatterns.core.util.CsvUtils
import java.io.FileWriter
import java.io.File
import java.io.PrintWriter

object Disco_Test2 {

  def main(args: Array[String]) = {

    case class Track(name: String, track: Int)

    val tiAlbums = Disco.TangoInfo.tiTracks.groupBy(x => TiAlbumSide(x.tint.tin, x.tint.side))
    val gordonAlbums = Disco.GordonMp3.gordonTracks.groupBy(x => GordonMp3Album(x.album))

    val tiAlbums1 = tiAlbums.map({ case (album, tracks) => (album -> tracks.map({ t => (t.tint.track -> t.name) }).toMap) })
    val gordonAlbums1 = gordonAlbums.map({ case (album, tracks) => (album -> tracks.map({ t => (t.track -> t.name) }).toMap) })

    def calc(a: TiAlbumSide, b: GordonMp3Album): Int = {
      
      def clean(s: String) = {
        s.replaceAll("[,\\|\\(\\)\\.!?]", " ").replaceAll(" +", " ").trim();
      }
      
      val collator = Collator.getInstance(Locale.forLanguageTag("es"));
      collator.setStrength(Collator.PRIMARY);
      
      var count = 0
      for (
        ta <- tiAlbums1.get(a);
        tb <- gordonAlbums1.get(b);
        n <- ta.keys;
        titleA <- ta.get(n);
        titleB <- tb.get(n);
        if (titleA != null && titleB != null && collator.compare(titleA, titleB) == 0)
      ) { count = count + 1 }
      return -count
    }

    def identify(b: GordonMp3Album) : Option[TiAlbumSide] = {
      val calcs = for ((a,l) <- tiAlbums) yield (a -> calc(a, b))
      val sorted = calcs.keys.toList.sortBy(calcs.get(_))
      val best = sorted.head
      val size = gordonAlbums1.getOrElse(b, null).keys.size
      val score = -1 * calcs.getOrElse(best, 100)
      if (score > 2 * size / 3) {
        Some(best)
      } else {
        None
      }
    }
    
    
    var count = 0
    val total = gordonAlbums1.keys.size
    
    var gordonTiAlbumMap = Map[GordonMp3Album, TiAlbumSide]()
    
    for (g <- gordonAlbums1.keys) {
      identify(g) match {
        case Some(a) => {
          println("(" + count + "/" + total + ") " + g.name + " => " + a.tin + " with score " + -calc(a, g) + " of " + 
              gordonAlbums1.getOrElse(g, null).size)
          gordonTiAlbumMap = gordonTiAlbumMap ++ Map(g -> a) 
        }
        case None => {
          println("(" + count + "/" + total + ") " + g.name + " unidentified")
        }
      }
      count = count + 1
    }
    
    val data = (for ((g, a) <- gordonTiAlbumMap) yield {
      List(g.name, a.tin, "" + a.side).toArray
    }).toArray
    
    val utils = new CsvUtils
    utils.toCsv(data)
    
    val out = new File("gordon-mp3-ti-album-map.csv")
    val pw = new FileWriter(out)
    pw.write(utils.getFormattedString)
    pw.close()

    /*
    val g = GordonAlbum("Tangos Sentimentales - Reliquias")
    val best = identify(g)
    
    for (a <- best; 
         l <- tiAlbums1.get(a);
         m <- gordonAlbums1.get(g); 
         n <- l.keys.toList.sorted; 
         tl <- l.get(n);
         tm <- m.get(n)) {
      println(n + " " + tl + " || " + tm)
    }
    
    */
  }

}