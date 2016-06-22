package au.com.thoughtpatterns.djs.examples

import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.disco.Disco._
import au.com.thoughtpatterns.djs.disco.Disco.TangoDJ._


import au.com.thoughtpatterns.core.util.CsvUtils
import java.io.PrintWriter
import au.com.thoughtpatterns.djs.lib.Library
import java.io.File

object AnalyseCdt {

  def main(args: Array[String]) {

    val l = Library.load(new File("jamie.djs"))

    val perfs = l.m.approxPerfs.toSet

    val tdjMap = (for (t <- tdjTracks) yield ( t -> mapTdj(t) )).toMap

    val cdt = (t: TdjTrack) => t.album.contains("CdT")

    val cdtTracks = tdjTracks.filter(cdt).sortBy(_.album)

    val ours = (for (
      t <- cdtTracks;
      perf <- tdjMap.get(t);
      p = perf.perf.toLibPerformance;
      have = perfs.contains(p)
    ) yield (t, have, p))

    val utils = new CsvUtils();

    // -1: none,
    // 0: not useable *
    // 10-50: not useable for DJ-ing a milonga * 
    // 60: useable **
    // 70: good, ***
    // 80-90: very good ****
    // 100: "best of" *****
    def rate(r: Int) = r match {
      case x if (x == -1) => ""
      case x if (x == 0) => "0"
      case x if (x < 60) => "*"
      case x if (x < 70) => "**"
      case x if (x < 80) => "***"
      case x if (x < 90) => "****"
      case x if (x <= 100) => "*****"
      case _ => ""
    }

    utils.toCsv((for ((t, have, p) <- ours) yield {
      List(
        if (have) "1" else "0",
        t.album,
        t.track.toString,
        t.genre,
        rate(t.rating),
        t.orchestra.name,
        t.name.toString(),
        if (t.date == null) "" else t.date.toString()).toArray
    }).toArray)

    val f = new java.io.File("test7.csv")
    val pw = new PrintWriter(f)
    pw.print(utils.getFormattedString())
    pw.close()
  }
}