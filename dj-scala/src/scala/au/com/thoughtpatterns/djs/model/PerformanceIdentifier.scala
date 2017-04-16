package au.com.thoughtpatterns.djs.model

import au.com.thoughtpatterns.djs.util.Log
import au.com.thoughtpatterns.djs.lib.Performance
import au.com.thoughtpatterns.dj.disco.tangoinfo.TangoInfo.TINT
import java.io.File
import au.com.thoughtpatterns.core.util.CsvUtils
import au.com.thoughtpatterns.djs.lib.ManagedMusic
import java.io.FileWriter
import java.io.FileReader
import collection.JavaConverters._
import au.com.thoughtpatterns.djs.disco.Types
import au.com.thoughtpatterns.djs.util.RecordingDate

/**
 * Given a performance, match it to the most probable tango.info performances.
 */
class PerformanceIdentifier(artist: Option[String]) {

  lazy val model = TIModel()

  lazy val perfs = model.perfs.filter {
    p =>
      if (artist.isDefined) {
        p.artist != null && p.artist.contains(artist.get)
      } else {
        true;
      }
  };

  def scoreData(query: Performance) = {
    (for (perf <- perfs) yield {
      val z = model.calcRatio(query.title, query.artist, perf.title, perf.artist)
      perf -> z
    }).toMap
  }

  def identify(query: Performance) : List[Performance] = {
    val data = scoreData(query)
    perfs.toSeq.sortBy(p => - data.getOrElse(p, 0d)).toList
  }

  def save(music: ManagedMusic, file: File) {
    
    val csv = new CsvUtils()
    
    var data = scala.collection.mutable.ListBuffer.empty[Array[String]]
    
    def perfToLine(perf: Performance) : Array[String] = {
      val line = new Array[String](6)
      line(1) = perf.genre
      line(2) = perf.title
      line(3) = perf.artist
      line(4) = if (perf.year != null) perf.year.toString else null
      return line
    }

    def blank() {
      data.append(new Array[String](6))
    }
    
    for (m <- music; md <- m.md) {
     
      Log.info("Identifying " + m.file)
      
      val perf = md.toApproxPerformance
      
      val line = perfToLine(perf)
      line(0) = m.file.getName
      line(5) = m.file.getAbsolutePath
      
      data.append(line)
      
      blank()

      for (p <- identify(perf).take(5)) {
        data.append(perfToLine(p))
      }

      blank()
      blank()
    }

    val arr = data.toArray
    
    csv.toCsv(arr)
    
    val contents = csv.getFormattedString
    
    val writer = new FileWriter(file)
    writer.write(contents)
    writer.close()
    
  }

  def load(file: File) : Map[File, Performance] = {
    
    val csv = new CsvUtils
    val reader = new FileReader(file)

    val data = csv.fromCsv(reader).asScala
    reader.close()
    
    var current : Option[File] = None
    
    val map : scala.collection.mutable.Map[File, Performance] = scala.collection.mutable.Map.empty
    
    def nonempty(s : String) = s != null && ! "".equals(s)
    
    def lineToPerf(line: Array[String]) = {
      val genre = line(1)
      val title = line(2)
      val artist = Types.parseArtist(line(3)).toString()
      val year = RecordingDate.parse(line(4))

      new Performance(title, artist, genre, year)
    }
    
    for (line <- data) {
      
      val f = line(5)
      if (nonempty(f)) {
        val ff = new File(f)
        current = Some(ff)
      } else {
        val selected = line(0)
        if (nonempty(selected) && current.isDefined) {
          val perf = lineToPerf(line)
          map.put(current.get, perf)
          current = None
        }
      }
      
    }
    
    map.toMap
  }
  
}
