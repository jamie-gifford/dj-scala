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
import au.com.thoughtpatterns.djs.model.PerformanceIdentifier

object Library_Test {

  def main(args: Array[String]) {

    val l = Library.load(new File("/home/djs/replica-dj/library13.djs"))

    val played = l.playlists.path("TALC").indirectContents;

    for (x <- played) {
      
      val f = x.file.toString();
      val m =  x.md.getOrElse(null);
      val line = List(f,m.title, m.genre, m.artist, m.year);
      
      def fix = (x: String) => x.replaceAll("'", "''");
      
      // println("\"" + line.mkString("\",\"") + "\"");
      
      var s = "update talc_recordings set artist = '" + fix(m.artist) + "', recording_date='" + m.year + "' where title = '" + m.title + "' and genre = '" + m.genre + "';"
      s = s.replace(", recording_date='null'", "");    
      
      println(s);
      
    }
    
    
    
  }
}