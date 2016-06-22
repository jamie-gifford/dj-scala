package au.com.thoughtpatterns.djs.disco

import au.com.thoughtpatterns.core.util.CsvUtils
import java.io.FileWriter
import java.io.File
import au.com.thoughtpatterns.djs.disco.importer.Importer
import au.com.thoughtpatterns.djs.disco.Types.TiArtist
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.util.RecordingDate
import java.text.CollationKey

object ImporterTest2 {

  val spanish = java.text.Collator.getInstance(java.util.Locale.forLanguageTag("es"));
  spanish.setStrength(java.text.Collator.PRIMARY)

  val nonword = "[^\\p{L}]".r
  val trim = " +".r

  def canonical(s: String) = if (s != null) trim.replaceAllIn(nonword.replaceAllIn(s, " "), " ") else s

  def main(args: Array[String]) {
    val lib = Library.load(new File("jamie.djs"))

    val root = new File("/media/Orange/Gordon/tmp")
    def accept(file: File): Boolean = {
      val n = file.getAbsolutePath()
      n.toLowerCase().endsWith(".mp3")
    }
    val g = new Importer(lib, root, accept)

    val disco = new Disco(Some(lib))

    val lp = disco.libPerformances

    def inSrc(src: Disco.Source) = lp filter {
      p =>
        {
          val srces = disco.getEquivSourced(p.perf) map { _.src }
          srces.contains(src)
        }
    } map { _.perf }

    val inLib = inSrc(disco.libSrc)

    println("Lib performances in disco: " + inLib.size)
    println("Disco performances: " + disco.performances.size)

    val identified = g.identifiedFiles

    // We want a wider net than disco.getEquivSourced.
    // We also want to include anything where the dates look like they might overlap.
    // eg  - if the given performance has no date, then any date
    //     - dates that are within 2 years of each other
    def getPlausibleEquivs(p: Disco.Performance): Set[Disco.SourcedPerformance] = {

      import Disco.Performance

      val nameKey = spanish.getCollationKey(canonical(p.name))
      val performer = TiArtist(p.performer.orchestra, null)
      
      def okay(s: Disco.SourcedPerformance) : Boolean = {
        
        if (s.perf.performer == null || s.perf.performer.orchestra == null) {
          return false
        }
        
        val performer0 = TiArtist(s.perf.performer.orchestra, null)
        if (performer0 != performer) {
          return false
        }
        
        if (s.perf.date != null && p.date != null) {
          if (! s.perf.date.refines(p.date) && ! p.date.refines(s.perf.date)) {
            return false
          } 
        }
        
        val nameKey0 = spanish.getCollationKey(canonical(s.perf.name))
        
        if (nameKey0 != nameKey) {
          return false 
        }

        return true
      }

      var equivs = for (
        s <- disco.libPerformances if (okay(s))
      ) yield s

      equivs.toSet
    }

    val newFiles = for (
      (f, Pair(p, a)) <- identified.toSeq;
      perf <- Set(p.perf, p.perf.stripVocalist);
      srces = getPlausibleEquivs(perf) map { _.src };
      _ = { println("---> Looked at " + perf + " and got " + getPlausibleEquivs(p.perf)) } if (!srces.contains(disco.libSrc))
    ) yield f

    println("Identified new files: " + newFiles.size)

    for (f <- newFiles; p <- identified.get(f); m <- g.metadataCache.get(f)) {
      println(f)
      println("From disco:  " + p._1)
      println("From album:  " + p._2)
      println("From file:   " + m)
      println
    }

  }

}