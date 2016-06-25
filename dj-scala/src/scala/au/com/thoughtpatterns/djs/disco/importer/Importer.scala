package au.com.thoughtpatterns.djs.disco.importer

import java.io.File
import scala.sys.process.ProcessIO
import scala.sys.process.Process
import scala.collection.mutable
import au.com.thoughtpatterns.djs.util.FileMetaCache
import au.com.thoughtpatterns.djs.util.RecordingDate
import au.com.thoughtpatterns.djs.lib.Metadata
import au.com.thoughtpatterns.djs.util.Log
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.disco.Types.TiAlbumSide
import au.com.thoughtpatterns.djs.model.TIModel
import au.com.thoughtpatterns.djs.lib.Performance
import au.com.thoughtpatterns.djs.disco.Disco.TangoInfo.TiTrack
import au.com.thoughtpatterns.dj.disco.tangoinfo.TangoInfo.TINT
import au.com.thoughtpatterns.core.util.CsvUtils
import au.com.thoughtpatterns.dj.disco.tangoinfo.TangoInfo
import au.com.thoughtpatterns.djs.disco.CollectionsUtil
import au.com.thoughtpatterns.djs.lib.MusicContainer
import au.com.thoughtpatterns.djs.lib.Managed
import au.com.thoughtpatterns.djs.lib.MusicFile
import au.com.thoughtpatterns.djs.lib.ManagedMusic
import au.com.thoughtpatterns.djs.lib.ManagedMaker

class Importer(lib: Library, root: File, accept: File => Boolean) extends Disco.EquivalenceFactory {

  lazy val files = findFiles(root)

  lazy val model = TIModel();

  val metadataCache = new FileMetaCache[Metadata](new File(".import-data")) {
    def readData(file: File) = {

      Log.info("Reading data from " + file)

      val cmd = List("exiftool", file.getAbsolutePath())
      val (out, err) = exec(cmd)

      def find(tag: String) = {

        val r = ("^" + tag + " +: .+").r

        (for (line <- out; found <- r.findFirstIn(line)) yield {
          Log.info("[" + line + "]")
          // Remove tag and ": " padding
          line.substring(tag.length()).trim().substring(2)
        }).toList match {
          case (head :: tail) => head
          case _ => null
        }
      }

      val title = find("Title")
      val artist = find("Artist")
      val album = find("Album")
      val year = RecordingDate.parse(find("Year"))
      val genre = find("Genre")

      def or[A](args: List[A]): Option[A] = {
        args match {
          case (null :: tail) => or(tail)
          case (head :: tail) => Some(head)
          case _ => None
        }
      }

      val tn = or(List(find("Track Number"), find("Track"), find("Number")))

      val cleanTrack = if (tn.isDefined) "[^0-9].*".r.replaceFirstIn(tn.get, "") else ""

      val track = (for (
        t <- tn;
        s <- "^[0-9]+".r.findFirstIn(t)
      ) yield s.toInt).getOrElse(0)

      Metadata(title, artist, album, year, null, genre, track, None, None)
    }
  }

  val identifyCache = new FileMetaCache[Performance](new File(".import-perf")) {
    def readData(file: File) = {
      metadataCache.get(file) match {
        case Some(m) => {
          model.identify(m.title, m.artist) match {
            case (head :: tail) => head
            case _ => null
          }

        }
        case _ => null
      }
    }
  }

  def findFiles(dir: File): Iterable[File] = {
    val f = dir.listFiles()
    val accepted = f.filter(x => !x.isDirectory() && accept(x)) ++ f.filter(_.isDirectory()).flatMap(findFiles(_))
    accepted
  }

  def load() {
    Log.info("Got " + files.size + " files")
    for (file <- files) {
      metadataCache.get(file) match {
        case Some(md) => Log.info(md.title)
        case _ => ()
      }
    }
    metadataCache.write
  }

  private def exec(command: Seq[String]) = {
    val process = Process(command)

    var out: List[String] = Nil
    var err: List[String] = Nil

    val io = new ProcessIO(
      stdin => (),
      stdout => { out = scala.io.Source.fromInputStream(stdout).getLines.toList },
      stderr => { err = scala.io.Source.fromInputStream(stderr).getLines.toList })
    process.run(io).exitValue

    Pair(out, err)
  }

  /**
   * Remove any cached metadata where there is no title (good for dealing with corrupt loads)
   */
  def clean() {
    val dirty = for (f <- metadataCache.keys; m <- metadataCache.get(f); if (m.title == null)) yield f
    for (f <- dirty) {
      Log.info("Cleaning " + f)
      metadataCache.dirty(f)
    }
  }

  lazy val importAlbums = {
    val filesWithAlbum = files.filter(f => metadataCache.get(f).isDefined && metadataCache.get(f).get.album != null).toList
    filesWithAlbum.groupBy(f => metadataCache.get(f).get.album).toMap
  }

  lazy val ti = Disco.TangoInfo

  type IndexedTracks = Map[Int, TiTrack]

  private def indexTracks(list: List[TiTrack]): IndexedTracks = {
    (for (tr <- list) yield { (tr.tint.track.intValue -> tr) }).toMap
  }

  lazy val tiAlbums = ti.albums.map({
    _ match {
      case (a, b) => (a -> indexTracks(b))
    }
  })

  def identifyAlbums: Map[String, TiAlbumSide] = {

    val out = mutable.Map[String, TiAlbumSide]()

    for ((a, files) <- importAlbums) {

      Log.info("Identifying")
      for (f <- files) {
        Log.info(f.toString())
      }

      val tracks = (for (f <- files; m <- metadataCache.get(f)) yield {
        (m.track -> f)
      }).toMap

      val max = tracks.keys.reduceLeft((x, y) => if (x > y) x else y)
      val albumMatches = for ((ta, tiTracks) <- tiAlbums) yield {

        val matches = for (
          i <- 1 to max;
          expected <- tiTracks.get(i);
          file <- tracks.get(i);
          got <- identifyCache.get(file);
          if (got.title == expected.name)
        ) yield expected

        (ta -> matches)
      }

      val orderedAlbumMatches = albumMatches.keys.toList.sortBy(a => -albumMatches.getOrElse(a, List()).size)

      for (a <- orderedAlbumMatches.slice(0, 2)) {
        val list = albumMatches.getOrElse(a, List())
        Log.info("Match length " + list.size + " for " + a)
        for (m <- list) {
          Log.info("  " + m)
        }
      }

      for (b <- orderedAlbumMatches.headOption; c <- albumMatches.get(b); if (c.size > 10)) {
        out.put(a, b)
      }

    }

    out.toMap
  }

  val identifiedFiles = {
    for (
      (a, b) <- identifyAlbums;
      files <- importAlbums.get(a).toSeq;
      file <- files;
      m <- metadataCache.get(file);
      i = m.track;
      tracks <- tiAlbums.get(b);
      track <- tracks.get(i);
      p = track.toPerformance
    ) yield {
      Log.info("*** Identified " + p + " with " + a)
      file -> Pair(p, a)
    }
  }

  object IdentifiedAlbumSource extends Disco.Source {

    val name = "imported-ti-albums"

    def performances: Iterable[Disco.SourcedPerformance] = {
      for (
        (f, p) <- identifiedFiles
      ) yield Disco.SourcedPerformance(p._1.perf, IdentifiedAlbumSource)
    }
  }

  def equivs: List[Set[Disco.Performance]] = {
    /*
    (for (
      (a, b) <- identifyAlbums;
      files <- importAlbums.get(a).toSeq;
      file <- files;
      m <- metadataCache.get(file);
      i = m.track;
      tracks <- tiAlbums.get(b);
      track <- tracks.get(i);
      p = track.toPerformance;
      q = p.perf
    ) yield (Set(q))).toList
    
    */
    // TODO figure out what this should be...
    List()
  }

  def contents: ManagedImports = ManagedImports(lib, files map (new ImportedFile(_, lib)))

  def perfToLib(p: Performance): ManagedMusic = {

    val dp = Disco.Performance(p);

    val x = for (
      q <- lib.ourDisco.getEquivSourced(dp);
      if (q.src.name == "lib");
      l = q.perf.toLibPerformance;
      w <- lib.m;
      if (w.toPerformance == l)
    ) yield {
      w
    }

    ManagedMusic(lib, x)
  }

  case class ImportedFile(f: File, lib: Library) extends MusicContainer {

    def id: Option[Disco.SourcedPerformance] = identifiedFiles.get(f) map (_._1)

    def idLib: ManagedMusic = {
      
      val y = for (
        p <- id.toList;
        _ = println("P = " + p);
        equivs = lib.ourDisco.getEquivSourced(p.perf) filter (_.src.name == "lib");
        _ = println("Got " + equivs.size + " equivs");
        x <- equivs;
        l = x.perf.toLibPerformance;
        w <- lib.m;
        if (w.toPerformance == l)
      ) yield {
        w
      }
      ManagedMusic(lib, y)
    }

    def what: ManagedMusic = {

      val x = for (
        m <- metadata.toList;
        p <- model.identify(m.title, m.artist)
      ) yield {
        perfToLib(p)
      }

      ManagedMusic.flatApply(lib, x.slice(0, 4))
    }

    def metadata = metadataCache.get(f)

    def file = f

    def read() {
      metadataCache.get(f)
    }

  }

  abstract class ManagedImports extends Managed[ImportedFile, ManagedImports] {

    def lib = Importer.this.lib

    def maker = new ManagedMaker[ImportedFile, ManagedImports]() {
      def make(i: Iterable[ImportedFile]) = new ManagedImports() {
        def iterator = i.iterator
      }
    }

    /**
     * Format for printing
     */
    def print = {
      println((for (f <- this) yield {
        f
      }).mkString("\n"))
    }

  }

  object ManagedImports {
    def apply(lib: Library, i: Iterable[ImportedFile]) = new ManagedImports {
      def iterator = i.iterator
    }
  }

}

