package au.com.thoughtpatterns.djs.disco.tangotunes

import au.com.thoughtpatterns.djs.disco.Disco.Source
import au.com.thoughtpatterns.dj.disco.tangoinfo.TangoInfo.TINT
import au.com.thoughtpatterns.core.util.CsvUtils
import au.com.thoughtpatterns.dj.disco.tangoinfo.TangoInfo
import au.com.thoughtpatterns.djs.util.RecordingDate
import au.com.thoughtpatterns.djs.disco.CollectionsUtil
import java.io.FileReader
import java.io.InputStreamReader
import java.io.File
import au.com.thoughtpatterns.djs.lib.Performance
import scala.collection.JavaConversions._
import au.com.thoughtpatterns.djs.model.PerformanceIdentifier
import au.com.thoughtpatterns.djs.lib.Library

object TangoTunes {
  
  val name = "tangotunes"

  val identifier = new PerformanceIdentifier(None)
  
  case class TangoTunesTrack(
    genre: String,
    artist: String,
    name: String,
    date: RecordingDate) {
    
    def toPerf = Performance(name, artist, genre, date)
  }

  lazy val tangoTunesTracks = {
    val reader = new InputStreamReader(TangoTunes.getClass.getResourceAsStream("tangotunes.csv"))
    val rows = (new CsvUtils()).fromCsv(reader)
    for {
      row <- rows
    } yield {
      TangoTunesTrack(row(0), row(1), row(2), RecordingDate.parse(row(3)))
    }
  }

  def identify(track: TangoTunesTrack) : List[Performance] = {
    
    val perf = track.toPerf
    
    // Candidates
    val perfs0 = identifier.identify(perf)

    // Filter by year if defined
    val perfs = if (track.date != null) {
      
      perfs0 filter {
        p => {
          
          if (p.year == null) {
            true;
          } else {
            track.date.refines(p.year) || p.year.refines(track.date)
          }
          
        }
      }
      
    } else {
      perfs0
    }

    val candidates = perfs.take(5)
    return candidates
  }
  
  def dump() {
    for (track <- tangoTunesTracks) {
      val candidates = identify(track)
      println("TangoTunes: " + track);
      for (c <- candidates) {
        println("-- " + c);
      }
    }
  }

  def main(args: Array[String]) {

    val l = Library.load(new File("/home/djs/replica-dj/library.djs"))
    
    val tt0 = TangoTunes.tangoTunesTracks
    
    val spanish = java.text.Collator.getInstance(java.util.Locale.forLanguageTag("es"));
    spanish.setStrength(java.text.Collator.PRIMARY)
    
    def ifnull(x: String) = {
      if (x != null) { x } else { "" }
    }
    
    val tt = tt0.sortBy { p => ifnull(p.genre) } sortWith((a, b) => { spanish.compare(ifnull(a.artist), ifnull(b.artist)) == -1 })
     
    val prefix = "Music/ogg/dj/"
    
    val milongas = l.p.path("public")
    val tandas = l.p.path("Jamie")
        
    println("Checking " + tt.size + " TangoInfo tracks against " + l.m.size + " library tracks")
    
    for (t <- tt filter { p => p.artist != null } ) {
      println("\n================================")
      println(t.genre + "; " + t.artist + "; " + t.name + "; " + t.date)
      
      val candidates = TangoTunes.identify(t)
      
      for (c <- candidates) {
        
        println(" TI: " + c.genre + "; " + c.artist + "; " + c.title + "; " + c.year);
        
        val ourFiles = l.m.filtre { f => f.toApproxPerformance.toSpanishPerformance.equals(c.toApproxPerformance.toSpanishPerformance) } take(5)
        
        for (m <- ourFiles; md <- m.md) {
          
          val rating = md.rating
          
          val filename0 = m.file.getAbsolutePath
          val idx = filename0.indexOf(prefix)

          val filename = if (idx >= 0) { filename0.substring(idx + prefix.length()) } else { filename0 }
          
          val mm = l.m.filtre { x => x == m }
          
          val milongaCount = milongas.containing(mm).size
          val tandaCount = tandas.containing(mm).size
          
          println("  LIB: " + md.genre + "; " + md.artist + "; " + md.title + "; " + md.year + "; rating=" + rating + "; milongas=" + milongaCount + "; tandas=" + tandaCount + "; " + filename)
        }
        
      }
      
    }
     
  }
  
  
}
