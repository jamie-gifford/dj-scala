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

object Disco_Test {

  def main(args: Array[String]) = {

    // TODO investigate
    // Why does 1938 El chamuyo, Donato, appear in the jamie-missing3.csv file as missing 
    
    val disco = new Disco(None)
    
    println("Have " + Disco.TangoInfo.tiTracks.size + " ti tracks")
    println("Have " + Disco.Gordon.gordonTracks.size + " gordon tracks")
    println("Have " + Disco.TangoDJ.tdjTracks.size + " tdj tracks")

    println("Have " + disco.rawPerformances.size + " raw performances")
    println("Have " + disco.performances.size + " performances")
    
    val data = (for (coset <- disco.performances; p = coset.rep; srcs = disco.perfToSources.getOrElse(p, Set())) yield {
      List(p.name, if (p.performer != null) p.performer.toString else null, if (p.date != null) p.date.toString else null, p.genre, srcs.mkString(",")).toArray
    }).toArray
    
    val utils = new CsvUtils
    utils.toCsv(data)
    
    new PrintWriter("disco.csv").print(utils.getFormattedString())
    
    println("Wrote " + data.size + " lines")
    
    /*
    case class Track(name: String, track: Int)

    val tiAlbums = Disco.TangoInfo.tiTracks.groupBy(x => TiAlbumSide(x.tint.tin, x.tint.side))
    val gordonAlbums = Disco.Gordon.gordonTracks.groupBy(x => GordonAlbum(x.album))

    val tiAlbums1 = tiAlbums.map({ case (album, tracks) => (album -> tracks.map({ t => (t.tint.track -> t.name) }).toMap) })
    val gordonAlbums1 = gordonAlbums.map({ case (album, tracks) => (album -> tracks.map({ t => (t.track -> t.name) }).toMap) })

    def calc(a: TiAlbumSide, b: GordonAlbum): Int = {
      
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
        if (collator.compare(titleA, titleB) == 0)
      ) { count = count + 1 }
      return -count
    }

    def identify(b: GordonAlbum) : Option[TiAlbumSide] = {
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
    
    var gordonTiAlbumMap = Map[GordonAlbum, TiAlbumSide]()
    
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
    
    val out = new File("gordon-ti-album-map.csv")
    val pw = new FileWriter(out)
    pw.write(utils.getFormattedString)
    pw.close()
    
    */
    
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