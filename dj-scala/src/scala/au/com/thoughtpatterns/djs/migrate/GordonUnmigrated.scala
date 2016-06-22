package au.com.thoughtpatterns.djs.migrate

import java.io.File
import java.io.PrintWriter
import scala.Option.option2Iterable
import au.com.thoughtpatterns.core.util.CsvUtils
import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.disco.Types._
import au.com.thoughtpatterns.djs.util.RecordingDate
import au.com.thoughtpatterns.djs.disco.Types.SpanishWord
import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.lib.Library

object GordonUnmigrated {

  def main(args: Array[String]) {

    val l = Library.load(new File("jamie.djs"))
    val disco = l.ourDisco
   
    val g = Disco.Gordon
    
    println("Gordon track: " + g.gordonTracks.size)
    println("Gordon performances: " + g.performances.size)
    
    println("Disco performances: " + disco.performances.size)
    
    def inSrc(src: Disco.Source) = disco.performances filter {
      p => {
          val srces = disco.getEquivSourced(p) map { _.src }
          srces.contains(src)
      }
    }
    
    val inGordon = inSrc(Disco.Gordon)
    val inLib = inSrc(disco.libSrc)

    println("Gordon performances in disco: " + inGordon.size)
    println("Lib performances in disco: " + inLib.size)

    val unmigrated = (inGordon -- inLib) filter { p => p.rep.name != null && p.rep.name.contains("El amor de los troveros") }
    
    println("Unmigrated in disco: " + unmigrated.size)
    
    val byArtist = unmigrated groupBy(p => p.rep.performer.orchestra)
    
    val artists = byArtist.keys.toList.sortBy(p => byArtist.getOrElse(p, Set.empty).size)
    
    for (p <- artists; u = byArtist.getOrElse(p, Set.empty); if (u.size > 0)) {
      println(p + " : " + u.size)
      for (v <- u) {
        println("  " + v.rep)
      }
    }
    
    
  } 

}