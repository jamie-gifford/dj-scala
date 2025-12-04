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

    val l = Library.load(new File("/media/djs/Orange/orange-library2.djs"));
    val tandas = l.p.path("Jamie");
    
    tandas.repl("/media/djs/Orange/Music", "/media/djs/Orange/replica");
    
    
  }
}