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

object Library_Test {

  def main(args: Array[String]) {

    val l = Library.load(new File("/home/djs/replica-dj/library.djs"))
    
    l.update()

    val x = l.m.tvm

    var already = 0
    var total = 0
    var count = 0
    
    for (m <- x; md <- m.md; if (md.composer == null)) {
      val c = m.lookupComposer
      for (composer <- c) {
        count = count + 1
      }
    }

    for (m <- x; md <- m.md) {
      val c = m.lookupComposer
      for (composer <- c) {
        total = total + 1
      }
      if (md.composer != null) {
        already = already + 1
      }
      if (c.isDefined && md.composer == null) {
        println(md.genre + ": " + md.title + ", " + md.artist + " => " + c.get)
      }
    }

    println("Count " + count)
    println("Total " + total)
    println("Already " + already)

    x.synchroniseComposer
    
    l.write()
    
    /*
    var total = 0
    var okay = 0
    
    for (m <- x) {
      val c = m.lookupComposer
      println(m.toApproxPerformance + " => " + c)
      
      total = total + 1
      if (c.isDefined) { okay = okay + 1 }
      
    }
    
    println(okay + " of " + total)
    */
    
    /*
    val rumbo = l.m.title("Sin rumbo fijo")
    
    val r2 = rumbo map { x => x.toApproxPerformance.toSpanishPerformance }
    
    for (p <- r2) {
      println(p)
    }
    
    val perf1Set = Set.empty + r2.head
    
    val perf2 = r2.tail.head
    
    val b  = perf1Set.contains(perf2)
    
    println("B = " + b)
    */
    
    /*
    l.refresh()
    
    l.write()
	*/

    /*
    val silence = l.m.require(_.title.contains("silence"))
    val biagi = l.m.artist("Biagi")
    
    val tandas = l.p.path("Jamie")
    
    val groups = biagi.group(tandas, silence)
    
    groups.print
    */
    /*
    val l = Library.load(new File("jamie-before-gordon.djs"))
    val disco = l.ourDisco

    // Things migrated in phase 1 (already implemented)
    val toMigrate = disco.performances map { _.rep } filter {
      p =>
        {
          val srces = disco.getEquivSourced(p) map { _.src }
          srces.contains(Disco.Gordon) &&
            !srces.contains(disco.libSrc) &&
            p.date != null &&
            p.performer != null && 
            p.name != null && 
            p.name.contains("El amor de los troveros")
        }
    }

    println("Found " + toMigrate.size + " items to migrate")
*/
    
    
    /*

    val file = new File("/tmp/12_-_Catamarca.flac")
    
    for (data <- FileMetadataCache.get(file)) {
      println("silence " + data.endSilenceSeconds)
    }
    
    FileMetadataCache.write()
    */
    
    /*
    val artists = (((for (m <- l.p.path("Jamie").indirectContents && l.m.tvm; md <- m.md) 
      yield { md.artist.split("(, voc. )|(, )") }) flatMap { x=>x }).toSet).toList.sorted
    
    for (a <- artists) println(a)
    */
    /* 
    import java.io.File
    
    val superroot = new File("/media/Orange/Music");
    val gordon = new File("/media/Orange/Music/ogg/dj/Gordon-BA/Tango Organised By Album")
    
    val un = l.listUninterestingSubdirs(gordon, superroot)
    
    println("Unrated: " + un.size + " subdirs")
    
    for (f <- un) println("ignore = Path " + f)
    */
    
    
    /*
    val tandas = (l.playlists.path("Jamie/vals") || l.playlists.path("Jamie/tango") || l.playlists.path("Jamie/milonga")).playlists
    
    val root = new java.io.File("/tmp/playlists")
    
    tandas.toTandas(root)
    */
    
    /*
    l.writeMissing(new java.io.File("/tmp/2.csv"))
    
    val d = l.ourDisco
    
    val dch = Disco.Performance("Chique", "tango", TiArtist("Edgardo Donato", null), RecordingDate.year(1936))
    
    val in = d.hasPerformance(dch, d.libSrc)
    
    println("in = " + in)
    
    for (e <- d.performances; 
    		p = e.toLibPerformance
    		if (p.title != null && p.title.toLowerCase().equals("chique"));
    		if (p.artist != null && p.artist.contains("Donato"))) {
      println("P " + e)
    }
    
    println("done")
    */
    /*
    
    val perfs = l.m.approxPerfs.toSeq.take(30)
    
    for (p <- perfs) {
      val title = p.title
      val word = l.mostSignificantWord(title)
      
      println(title + " => " + word)
    }
    */
    
    
    /*
    val l = Library.load(new File("jamie.djs"))
    
    val perfs = l.m.approxPerfs.toSet
    
    val cdtTracks = Disco.cdtTracks.sortBy(_.album)
    
    val ours = (for (
        t <- cdtTracks;
        perf <- Disco.tdjMap.get(t);
        p = perf.toLibPerformance;
        have = perfs.contains(p)) yield (t, have, p))
    
    val utils = new CsvUtils();
        
    // -1: none,
    // 0: not useable *
    // 10-50: not useable for DJ-ing a milonga * 
    // 60: useable **
    // 70: good, ***
    // 80-90: very good ****
    // 100: "best of" *****
    def rate(r : Int) = r match {
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
        
    val f = new java.io.File("test6.csv")
    val pw = new PrintWriter(f)
    pw.print(utils.getFormattedString())
    pw.close()
    
    //l.path("reload.m3u").indirectContents.synchronise(-1)
    
    */
    /*
    implicit def iterableToManagedMusic(i: Iterable[MusicFile]): ManagedMusic =
    	ManagedMusic(l, i)

    
    l.m.tvm.tvm
    
    println(l.playlists.containing(l.path("reload.m3u").indirectContents.m).size)

    */
    //val dups = l.music.require(_.title == "Adiós, Chantecler").m.dups

    //l.rate(l.m.require(_.title == "Si se salva el pibe").m.require(_.artist.contains("Tanturi")).m.tail.head)
    /*
    val q = new Player(l)
    
    val a = q.qq.m
    
    val r = l.playlists
    
    //r.prefer(a)
    
    q.qq.exchange(l.playlists)
    */
    /*
    val p = l.path("chau").playlists.head
    val preferred = l.path("El").music.head
    p.prefer(l, preferred)
    */
    /*
    
    val jamie = l.path("Jamie").path("Donato")
    val cortinas = l.path("Jamie").path("cortina")
    val aldo = l.path("Jamie").path("Aldo")
    
    val t = (jamie \ cortinas \ aldo).playlists
    
    t.replicate("/media/Orange/Music/", "/media/Orange/test2")
    t.indirectContents.replicate("/media/Orange/Music/", "/media/Orange/test2")
    * */

  }

}