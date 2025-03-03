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

  def str(x: Object) : String = if (x == null) { "" } else { x.toString() }

  def main(args: Array[String]) {

    val l = Library.load(new File("/home/djs/replica-dj/library13.djs"))

    //val played = l.playlists.path("TALC").indirectContents;
    val p = l.playlists.path("tvm-2star");
    val played = p.indirectContents;

    val lines = new Array[Array[String]](played.size);
      
    val utils = new CsvUtils();

    
    var i = -1;
    for (x <- played) {
      
      i += 1;
      
      val f = x.file.toString();
      val m =  x.md.getOrElse(null);

            
      
      
      val line = Array(str(f),str(m.title), str(m.genre), str(m.artist), str(m.year));

      lines(i) = line;
      
    }
    
    utils.toCsv(lines);
    println(utils.getFormattedString);
    
    
  }
}