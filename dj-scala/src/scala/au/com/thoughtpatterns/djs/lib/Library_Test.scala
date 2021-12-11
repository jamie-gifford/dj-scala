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

//    val l = Library.load(new File("/home/djs/replica-dj/library5.djs"))
    		

    val identifier = new PerformanceIdentifier(None)
    val in = new File("/tmp/identified.csv")

    val map = identifier.load(in)

    val file = new File("/home/djs/replica-dj/Music/ogg/dj/CracklingTunes/CTMM2108FLAC1648/Carlos Di Sarli _ Robert Gale_Di Sarli, Carlos [Orquesta]_Noche de locura (Tango) 03_02_1956 [CT1898-B1].flac");
    val perf = map.getOrElse(file, null)

    println(perf.artist);

  }
}