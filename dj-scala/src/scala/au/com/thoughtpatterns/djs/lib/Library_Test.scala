package au.com.thoughtpatterns.djs.lib

import java.io.File
import java.io.PrintWriter
import scala.Option.option2Iterable
import au.com.thoughtpatterns.core.util.CsvUtils
import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.disco.Types._
import au.com.thoughtpatterns.djs.util.RecordingDate
import au.com.thoughtpatterns.djs.disco.Types.SpanishWord
import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.clementine.Player
import java.util.Date
import au.com.thoughtpatterns.djs.clementine.Clementine
import au.com.thoughtpatterns.djs.clementine.Analyzer
import au.com.thoughtpatterns.djs.disco.tangotunes.TangoTunes

object Library_Test {

  def main(args: Array[String]) {

    // TangoTunes.dump()
    
    val l = Library.load(new File("/home/djs/replica-dj/library.djs"))
    
    val tt0 = TangoTunes.tangoTunesTracks
    
    val spanish = java.text.Collator.getInstance(java.util.Locale.forLanguageTag("es"));
    spanish.setStrength(java.text.Collator.PRIMARY)
    
    
    def ifnull(x: String) = {
      if (x != null) { x } else { "" }
    }
    
    val tt = tt0.sortBy { p => ifnull(p.genre) } sortWith((a, b) => { spanish.compare(ifnull(a.artist), ifnull(b.artist)) == -1 })
     
    //val a = l.m map { t => t -> t.toApproxPerformance } toMap
    
    // approximate performances mapped to music files
    //val ours = a.groupBy(_._2).mapValues(_.keys)
    
    val prefix = "Music/ogg/dj/"
    
    val milongas = l.p.path("public")
    val tandas = l.p.path("Jamie")
    
    l.m.toSet.groupBy { f: MusicFile => f.md.get.group }
        
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
          val tandaCount = milongas.containing(mm).size
          
          println("  LIB: " + md.genre + "; " + md.artist + "; " + md.title + "; " + md.year + "; rating=" + rating + "; milongas=" + milongaCount + "; tandas=" + tandaCount + "; " + filename)
        }
        
      }
      
    }
    
     //val l = Library.load(new File("/home/djs/tmp/sound.djs"))

     //l.m.print
     
     //l.m.fixTitles
     
     //val clem = new Clementine();
     
     //val tracks = clem.getTracks();
     
     //println(tracks);
     
    
      //val l = Library.load(new File("/media/djs/Orange-mirror/Music/ogg.djs"))

      //l.add(new File("/media/djs/Orange-mirror/Music/ogg"))
      
     /*
     val tvm = Set("tango", "vals", "milonga")
     
     val interesting = l.m.require(x => x.comment == null).require(x => x != null && tvm.contains(x.genre)).path("Jerry")
 
     val old = 1l;
     val now = new Date().getTime

     println("Got " + interesting.size + " to process")
     
     for (m <- interesting) {
       println(m.file)
       m.file.setLastModified(now)
     }
      
     */
     
//     val q = new Player(l)
     
//     q.q.synchronise(1)
     
  }
}