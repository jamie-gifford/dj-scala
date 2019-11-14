package au.com.thoughtpatterns.djs.lib

import java.io.File
import au.com.thoughtpatterns.djs.clementine.Clementine
import scala.util.Sorting
import au.com.thoughtpatterns.djs.tag.TagFactory
import au.com.thoughtpatterns.djs.disco.tangoinfo.Synchroniser
import au.com.thoughtpatterns.djs.util.RecordingDate
import scala.collection.AbstractIterator
import au.com.thoughtpatterns.djs.disco.Disco.Source
import au.com.thoughtpatterns.core.util.CsvUtils
import java.io.PrintWriter
import au.com.thoughtpatterns.djs.clementine.PlayerInterface
import au.com.thoughtpatterns.djs.clementine.PlayerInterfaceFactory
import au.com.thoughtpatterns.djs.webapp.DJSession
import au.com.thoughtpatterns.djs.webapp.Desktop
import java.io.FileWriter
import au.com.thoughtpatterns.djs.util.Log
import scala.collection.mutable.MutableList
import com.tutego.jrtf.Rtf
import com.tutego.jrtf.RtfText
import com.tutego.jrtf.RtfPara
import scala.collection.JavaConverters._
import com.tutego.jrtf.RtfHeader
import au.com.thoughtpatterns.djs.model.PerformanceIdentifier
import au.com.thoughtpatterns.djs.clementine.Analyzer
import au.com.thoughtpatterns.djs.disco.Disco

trait Managed[T <: MusicContainer, S <: Managed[T, S]] extends Iterable[T] with Formatter {

  def lib: Library

  def maker: ManagedMaker[T, S]

  protected def make(i: Iterable[T]) = maker.make(i)

  private def musicIterable =
    (this filter {
      x => x match { case y: MusicFile => true case _ => false }
    }).asInstanceOf[Iterable[MusicFile]]

  private def playlistIterable =
    (this filter {
      x => x match { case y: PlaylistFile => true case _ => false }
    }).asInstanceOf[Iterable[PlaylistFile]]

  // Idiomatic?
  def music = ManagedMusic(lib, musicIterable)
  def m = music

  def playlists = ManagedPlaylists(lib, playlistIterable)
  def p = playlists

  // --------------------------------
  // Filters

  def path(p: String) = filtre(_.file.getAbsolutePath().contains(p))

  def chunk(from: Int, size: Int) = make(slice(from, from + size))

  // --------------------------------
  // Operations

  /**
   * Union
   */
  def ||(a: Managed[T, S]) = combine(a, { _ ++ _ })

  /**
   * Difference
   */
  def \(a: Managed[T, S]) = combine(a, { _.toSet diff _.toSet })

  /**
   * Intersection
   */
  def &&(a: Managed[T, S]) = combine(a, { _.toSet intersect _.toSet })

  def indirectContents: ManagedMusic = {
    val p =
      for (pl <- playlists; tr <- pl.indirectContents) yield { lib.resolve(tr) }

    val set = music.toSet union p.toSet
    ManagedMusic(lib, set)
  }

  private def combine(a: Managed[T, S], combinor: (List[T], List[T]) => Iterable[T]): Managed[T, S] = {
    def lib0 = lib

    def list1 = iterator.toList
    def list2 = a.iterator.toList

    def list0 = combinor(list1, list2)

    make(list0)
  }

  def filtre(f: T => Boolean): S = {
    val x = for (t <- iterator; if (f(t))) yield { t }
    make(x.toList)
  }

  import MusicFormat._

  /**
   * Given a from path (eg "/media/Orange/Music") and a to path (eg "/media/mp3-player/Music")
   * replicate the structures, transcoding to given format
   */
  def replicate(from: String, to: String, strategy: ReplicationStrategy) = {
    val f = new File(from);
    val t = new File(to);
    if (!f.exists()) throw new IllegalArgumentException("No file " + f)
    if (!t.exists()) throw new IllegalArgumentException("No file " + t)
    for (m <- this)
      m.replicate(strategy)
    this
  }

  def repl(from: String, to: String) = {
    val x = new ReplicationStrategy.Ogg(new File(from).toPath(), new File(to).toPath())
    replicate(from, to, x)
    this
  }

  def replIdentity(from: String, to: String) = {
    val x = new ReplicationStrategy.Identity(new File(from).toPath(), new File(to).toPath())
    replicate(from, to, x)
    this
  }

  /**
   * Replicate library, compressing to Ogg anthing with either no rating or rating less than 2 stars
   */
  def replDJ(from: String, to:String) = {
    val okay = m.require(md => {
      var z = md.rating match { case Some(x) if x >= 0.4 => true case _ => false }
      z
    })
    val good = m.minRating(0.4)
    
    /*
    val bad = m \ m.minRating(0.3) \ m.path("cortina")
    val weird = m \ m.tvm \ m.path("cortina") \ m.artist("Oscar Aleman")
		*/
    // val compressHard = m \ m.path("cortina") \ m.unrated \ m.minRating(0.3)

    val compressHard = m.title("DUMMY NOT SURE WHAT TO COMPRESS HARD")
    
    val goodFiles = (good.map { x => x.file }).toSet
    val compressHardFiles = (compressHard.map { x => x.file }).toSet
    
    val x = new ReplicationStrategy.DJ(new File(from).toPath(), new File(to).toPath(), goodFiles, compressHardFiles)
    
    replicate(from, to, x)
    this
  }
  
  /**
   * Replicate music files in library, renaming to artist/title and transcoding to MP3
   */
  def replShare(from: String, to: String) = {
    val x = new ReplicationStrategy.Share(lib, new File(from).toPath(), new File(to).toPath())
    replicate(from, to, x)
    this
  }
  

}

abstract trait DesktopCompat {

  abstract class Operation(val name: String) {

    def exec(): Unit

  }

  def size: Int

  def name: String

  def metadata: List[Pair[String, String]] = List()

  def ops: List[Operation] = List()

  private def quote(s: String): String = if (s != null) s.replaceAllLiterally("\"", "\\\"") else ""

  private def qname = quote(name)

  def desk(linkTo: Set[DesktopCompat] = Set()) {
    val old = DJSession.session.desk
    DJSession.session.desk = new Desktop(Some(this), Some(old), linkTo)
  }

  def toDeskJson(index: Int): String = {

    val qmd = (for ((key, value) <- metadata) yield {
      val qkey = quote(key)
      val qvalue = quote(value)
      s"""
      | { "key": "$qkey", "value": "$qvalue" }
      |""".stripMargin
    }).mkString(",")

    val qops = (for (op <- ops) yield ("\"" + quote(op.name) + "\"")).mkString(",")

    s"""
      |   {"name":"$qname", "objId": $index, "size": $size,
      |    "ops": [ $qops ], 
      |     "md": [ $qmd ]
      |   }
      |""".stripMargin
  }

  def execute(opName: String): Unit = {

    for (op <- ops if op.name == opName) op.exec

  }

}

abstract class ManagedContainers extends Managed[MusicContainer, ManagedContainers] {

  type M = ManagedMaker[MusicContainer, ManagedContainers]

  def maker: M = new M() {
    val lib0 = lib
    def make(i: Iterable[MusicContainer]): ManagedContainers = new ManagedContainers {
      def iterator = i.iterator
      def lib = lib0
    }
  }

}

object ManagedContainers {
  def apply(lib0: Library, i: Iterable[MusicContainer]) = {
    new ManagedContainers() {
      def iterator = i.iterator
      def lib = lib0
    }
  }
}

abstract class ManagedMusic(
  val lib: Library) extends Managed[MusicFile, ManagedMusic] with DesktopCompat {

  // TODO figure out how to get this conversion visible to users of ManagedMusic.
  implicit def iterableToManagedMusic(i: Iterable[MusicFile]): ManagedMusic =
    ManagedMusic(lib, i)

  override def name = "Music: " + size + " item(s)"

  val explodeOp = new Operation("Explode") {
    def exec() {
      for (e <- ManagedMusic.this.music) e.desk(Set(ManagedMusic.this))
    }
  }

  override def ops = List(explodeOp)

  def maker = new ManagedMaker[MusicFile, ManagedMusic]() {
    def make(i: Iterable[MusicFile]) = new ManagedMusic(lib) {
      def iterator = i.iterator
    }
  }

  def require(f: => Metadata => Boolean) = filtre {
    m =>
      m.md match {
        case Some(metadata) => { try { f(metadata) } catch { case _: Exception => false } }
        case None => false
      }
  }

  def artist(artist: String) = require(_.artist.contains(artist))
  def genre(genre: String) = require(_.genre == genre)
  def title(title: String) = require(_.title.toLowerCase.contains(title.toLowerCase))
  def composer(composer: String) = require(_.composer.toLowerCase.contains(composer.toLowerCase))
  
  def yearRange(from: Int, to: Int) = require(
    x =>
      RecordingDate.year(from - 1) < x.year &&
        x.year < RecordingDate.year(to + 1))

  def tvm = require(md => List("tango", "vals", "milonga").toSet.contains(md.genre))

  def minRating(r: Double) = require(_.rating.getOrElse(0d) >= r)

  def unrated = require(_.rating.getOrElse(-1d) == -1)

  def rated = require(_.rating.getOrElse(-1d) != -1)
  
  def keyed = require(_.group != null)  
    


  /**
   * Find duplicates inside this list
   */
  def dups = {
    // Map performances to list of MusicFiles
    val perfs = this groupBy { _.toApproxPerformance }

    // Find those with more than one MusicFile
    val dupPerfs = perfs filter {
      _ match {
        case Pair(p, l) => l.size > 1
      }
    }

    // The corresponding MusicFiles
    val dups = dupPerfs flatMap { _._2 }

    make(dups)
  }

  /**
   * Intersection with "like"
   */
  def ~(m: ManagedMusic) = {
    val perfs = (m map { _.toApproxPerformance.toSpanishPerformance }).iterator.toSet
    filtre({ x => perfs.contains(x.toApproxPerformance.toSpanishPerformance) })
  }
  
  /**
   * Difference with "like"
   */
  def \~(m: ManagedMusic) = {
    val perfs = (m map { _.toApproxPerformance.toSpanishPerformance }).iterator.toSet
    filtre({ x => ! perfs.contains(x.toApproxPerformance.toSpanishPerformance) })
  }
  

  /**
   * Send contents to clementine
   */
  def q() {
    val list = for (m <- this) yield m
    class TmpM3u(file: File) extends M3UPlaylist(file, lib) {
      override def load {
        tracks = list.toList
        relative = false
        setReadDirty(false)
      }
    }
    val file = File.createTempFile("dj-playlist", ".m3u")
    val pl = new TmpM3u(file)
    pl.read()
    pl.save()

    PlayerInterfaceFactory.getPlayer().addTrack(pl)
  }

  /**
   * Send contents to clementine but maintain current track if it is present 
   */
  def q2() {
    val player = PlayerInterfaceFactory.getPlayer()
    val current = player.getCurrentTrack
    var idx = -1;
    var i = 0;
    
    for (m <- this) {
      val u = m.file.toURL
      if (idx < 0 && current != null && u.toString == current.toString) {
        idx = i;
      }
      i = i + 1;      
    }
    
    q()

    if (idx > 0) {
      player.setCurrentIndex(idx, current)
    }
  }
  
  /**
   * Make suggestions based on given set of playlists (tandas)
   */
  def suggest(tandas: ManagedPlaylists) = (tandas.containing(this).indirectContents \ this).music

  /**
   * Closure based on suggestions
   */
  def closure(tandas: ManagedPlaylists, depth: Integer = 100): ManagedMusic = {
    if (depth <= 0) {
      return this;
    }
    val next = this.suggest(tandas) || this;
    if (next.size == this.size) {
      return this;
    }
    return next.closure(tandas, depth - 1);
  }

  /**
   * Json of connection coefficients according to tandas
   */
  def json(tandas: ManagedPlaylists): String = {

    val map = (for (m <- iterator.zipWithIndex) yield (m._1.toPerformance -> m._2)).toMap
    val out = new StringBuilder();

    out.append("{\"nodes\":[");

    def perfName(p: Performance) = {
      //if (p.year != null) p.year.from.year + ": " + p.title else p.title
      p.toString().replaceFirst("Performance", "").replaceAll("\"", "\\\"")
    }

    val nodes = (for (m <- iterator) yield "{\"name\":\"" + perfName(m.toPerformance) + "\"}")
    out.append(nodes.mkString(",\n"))

    out.append("],\n \"links\":[");

    val links = collection.mutable.Set.empty[Tuple2[Integer, Integer]]

    def process(songs: List[MusicFile]) {
      songs match {
        case a :: tail => {
          tail match {
            case b :: rest => {
              val x = map.get(a.toPerformance)
              val y = map.get(b.toPerformance)
              for (u <- x; v <- y) {
                links.add(Tuple2(u, v))
              }
            }
            case _ => Unit
          }
          process(tail)
        }
        case _ => Unit
      }
    }

    for (p <- tandas) {
      val tanda = p.indirectContents.toList.map(lib.resolve(_))

      process(tanda)
    }

    val ls = for (link <- links.toSeq)
      yield "{\"source\": " + link._1 + ",\"target\":" + link._2 + "}"

    out.append(ls.mkString(",\n"))

    out.append("]}");

    return out.toString
  }

  /**
   * Group according to tandas (closure of equivalence), sort by average year,
   * separate with separator.
   */
  def group(tandas: ManagedPlaylists, separator: ManagedMusic) = {

    def sep = separator.head;

    // Connected components
    type Component = Set[MusicFile]

    type Components = Set[Component]

    // Current components
    var components: Components = Set.empty

    // Map of music to music in tandas (first level)
    val siblingMap: Map[MusicFile, Set[MusicFile]] = {
      val tm = for (
        t <- tandas;
        c0 = t.indirectContents.toSet;
        c = c0 map { lib.resolve(_) };
        m <- c
      ) yield (m -> c)
      tm.groupBy(_._1).flatMap(_._2)
    }

    // Current map of music files to current components
    var compMap: Map[MusicFile, Component] = Map.empty

    // Add the music file to the components. This can involve joining two components
    def join(m: MusicFile) {

      // Find other music related directly to this
      val siblings = siblingMap.getOrElse(m, Set.empty)

      // Find other current components with siblings
      val siblingComponents = siblings collect {
        case m if (compMap.contains(m)) => compMap(m)
      }

      // Replace these components with a new, joint component, plus the new music
      val jointComponent =
        if (siblingComponents.isEmpty)
          Set(m)
        else
          (siblingComponents reduce ((x, y) => x union y)) + m

      val tmp = components -- siblingComponents + jointComponent

      components = tmp

      val newMap = (for (z <- jointComponent) yield (z -> jointComponent)).toMap
      val tmp2 = compMap -- siblings ++ newMap

      compMap = tmp2
    }

    for (m <- iterator) {
      join(m)
    }

    // Now we have a bunch of components. Order by average year
    def calcYear(c: Component): Double = {
      val dates = for (m <- c; md <- m.md; d = md.year; if (d != null)) yield d.from.year
      val size = dates.toSeq.size
      val average = if (size > 0) dates.reduce(_ + _) / size else 1940d
      average
    }

    val ages = (components map (x => x -> calcYear(x))).toMap

    val sortedComponents = components.toSeq.sortBy(x => ages.getOrElse(x, 1940d))

    // Create new ManagedMusic by joining the components and separating with separator.

    def title(m: MusicFile) = m.md match { case Some(md) => md.title case _ => "ZZZZZ" }

    def concat(seq: Seq[Component]): List[MusicFile] = {
      seq match {
        case Nil => Nil
        case _ => {

          val c = seq.head

          val contents = (for (m <- c) yield m).toList.sortBy(title)
          contents ++ List(sep) ++ concat(seq.tail)
        }
      }
    }

    val out = concat(sortedComponents)
    make(out)
  }

  /**
   * Format for printing (TODO generalise formatting)
   */
  def print = {
    println((for (m <- this) yield {
      m.md match {
        case Some(md) => format(md)
        case None => m.file.toString()
      }
    }).mkString("\n"))
  }

  /**
   * Get equivalent ManagedPerformances (approximate ones)
   */
  def approxPerfs = {
    val me = this
    new ManagedPerformances {
      val lib = me.lib
      def iterator = (for (m <- me) yield m.toApproxPerformance).iterator
    }
  }

  /**
   * List all approx performances from given discography and mark those which we have
   */
  def markDisco(disco: Iterable[Pair[Performance, Set[Source]]]): Iterable[Triple[Performance, Boolean, Set[Source]]] = {
    val ours = approxPerfs.toSet
    for ((d, src) <- disco) yield {
      Triple(d, false, src)
    }
  }

  // -------------------
  // Sorting

  def srt(lt: (Metadata, Metadata) => Boolean) = {
    def f(a: MusicFile, b: MusicFile): Boolean = {
      b.md match {
        case None => true
        case Some(bmd) => a.md match {
          case Some(amd) => lt(amd, bmd)
          case None => false
        }
      }
    }
    val sorted = Sorting.stableSort(this.toSeq, f _)
    make(sorted)
  }

  private def nn(in: String) = if (in == null) "" else in

  def byTitle = srt((x, y) => nn(x.title) < nn(y.title))
  def byArtist = srt((x, y) => nn(x.artist) < nn(y.artist))
  def byYear = srt((x, y) =>
    if (x.year != null) {
      if (y.year != null) {
        x.year < y.year
      } else {
        true
      }
    } else {
      false
    })

  def harmonyCmp(key: MusicKey) : (Metadata, Metadata) => Boolean = {
    def dist(z: Metadata) = {
      if (z.group == null) 99 else MusicKey(z.group).fifths2(key);
    }

    return (x, y) => {
      val dx = dist(x)
      val dy = dist(y)
      if (dx - dy < 0) false else true
    }
  }
    
  def byHarmony(key: MusicKey, padding: ManagedMusic) : ManagedMusic = {
    
    val list = new MutableList[MusicFile]
    
    val sorted = srt(harmonyCmp(key))
    
    var current = 0;
    for (m <- sorted) {
      val z = m.md.get;
      val n : Integer = if (z.group == null) 99 else MusicKey(z.group).fifths(key);
      
      // System.out.println(key + " cmp " + z.group + " has n=" + n + " on " + z.title) 
      
      if (current != n) {
        for (tr <- padding.indirectContents) {
          val m = lib.resolve(tr); 
          list += m;
        }
      }
      current = n;
      list += m;
    }
    
    ManagedMusic(lib, list)
  }
    
  def byRating = srt((x, y) => x.rating.getOrElse(0d) < y.rating.getOrElse(0d))
  
  def byBpm = srt((x, y) => x.bpm.getOrElse(0d) < y.bpm.getOrElse(0d))


  // -------------------------
  // Refactorings

  /**
   * Use this contents in preference to equivalent music in the given playlists.
   * Also, transfer ratings to this music if nececessary and prune the dups in the entire library like this.
   */
  def exchange(pl: ManagedPlaylists) = {

    // Refactor playlists
    pl.prefer(this)

    // Transfer ratings if necessary
    for (m <- this) {
      m.md match {
        case Some(md) => {
          if (md.rating.getOrElse(0) == 0) {
            val rating = lib.rate(m)

            if (rating > 0) {
              // Apply rating to MusicFile
              val tag = new TagFactory().getTag(m.file)
              tag.setRating(rating)
              tag.write()
              m.update()
            }

          }
        }
        case None => {}
      }
    }

    // Apply "pruned2" to others
    val d = lib.m.dups ~ this
    val others = (d \ this).m
    others.prune
    this
  }

  def reburn = {
    for (m <- this) {
      m.md match {
        case Some(md) => {
          if (md.bpm.getOrElse(0) == 0) {
            val bpm = lib.findBPM(m)

            if (bpm > 0) {
              // Apply rating to MusicFile
              val tag = new TagFactory().getTag(m.file)
              tag.setBPM(bpm)
              tag.write()
              m.update()
            }

          }
        }
        case None => {}
      }
    }
    
  }
  
  def prune = {
    for (m <- this; if m.file.exists) {
      m.md match {
        case Some(md) => {
          if (!md.genre.startsWith("pruned")) {
            val tag = new TagFactory().getTag(m.file)
            val g = tag.getGenre()
            val g2 = "pruned2-" + g
            tag.setGenre(g2)
            tag.write()
            m.update()
          }
        }
        case None => {}
      }
    }

    this
  }

  def synchronise: ManagedMusic = {
    synchronise(180)
  }

  def synchronise(days: Int): ManagedMusic = {
    val s = new Synchroniser(lib, this)
    s.maxdays = days
    s.update()
    this
  }

  def synchroniseComposer = {
    
    for (m <- this; md <- m.md; if (md.composer == null)) {
      val c = m.lookupComposer
      for (composer <- c) {
        
        val tag = new TagFactory().getTag(m.file)
        tag.setComposer(composer)
        tag.write()
        m.update()
        
      }
    }
    
    this
  }
  
  def fixShortEnds(minEnd: Double): ManagedMusic = {
    val shorts = this.filtre { m => m.endSilence.isDefined && m.endSilence.get < minEnd - 0.2d}
    
    for (m <- shorts) m.padEndSilence(minEnd - m.endSilence.get)
    
    shorts
  }
  
  def replaygain: ManagedMusic = {
    val empty = this.require(x => {
      x.rg == null || x.rg.isEmpty
    })
    
    for (m <- empty) m.replaygain()
    empty
  }
  
  def hasRG : ManagedMusic = this.require(x => {
      x.rg != null && ! x.rg.isEmpty
  })
  
  def noRG : ManagedMusic = this.require(x => {
      x.rg == null || x.rg.isEmpty
  })
  
  def previewTitles : ManagedMusic = {
    val fixer = new NameFixer(this)
    fixer.preview
  }
  def fixTitles : ManagedMusic = {
    val fixer = new NameFixer(this)
    fixer.rename
  }

  def previewGuesses : ManagedMusic = {
    val fixer = new NameGuesser(this)
    fixer.preview
  }
  def fixGuesses : ManagedMusic = {
    val fixer = new NameGuesser(this)
    fixer.rename
  }

  
  // ---------------
  // Dump to CSV

  def toCsv(file: File) = {
    val utils = new CsvUtils

    val lines = (
      for (
        music <- this;
        md <- music.md
      ) yield List(if (md.year != null) md.year.toString else null, md.title, md.artist, music.file.toString(), md.genre).toArray).toArray

    utils.toCsv(lines)

    val pw = new PrintWriter(file)
    pw.print(utils.getFormattedString())
    pw.close()

  }

  // ---------------
  // Model-based identification
  
  
  def identify(artist: Option[String]) : ManagedMusic = {
    
    val identifier = new PerformanceIdentifier(artist)
    
    val out = new File("/tmp/identified.csv")
    
    identifier.save(this, out)
    
    Log.info("Wrote data to " + out)
    
    this
  }
  
  def applyIdentify() : ManagedMusic = {
    
    val identifier = new PerformanceIdentifier(None)
    val in = new File("/tmp/identified.csv")
    
    val map = identifier.load(in)
    
    for (m <- iterator; file = m.file; if (map.keySet.contains(file))) {
      val perf = map.getOrElse(file, null)
      val tag = new TagFactory().getTag(m.file)
      tag.setArtist(perf.artist.toString)
      tag.setTitle(perf.title)
      tag.setGenre(perf.genre)
      if (perf.year != null) {
        tag.setYear(perf.year)
      }
      tag.write()
      m.update()
      
    } 
    
    this
  }
  
  def newToXSPF : ManagedMusic = {
    val anal = new Analyzer(lib)
    val xspf = anal.newToXSPF
    val file = new File("/tmp/new.xspf")
    val w = new FileWriter(file)
    w.write(xspf)
    w.close()
    Log.info("Wrote to " + file)
    this
  }
  
  def wrong : ManagedMusic = {
    
    val info = Disco.TangoInfo;
    val m = info.tiTracks.groupBy { x => x.toPerformance.perf.toLibPerformance }

    val tvm = Set("tango", "vals", "milonga")

    val availTvm = m.keySet filter { p => p.artist != null && !p.artist.startsWith("null") && tvm.contains(p.genre) }

    val already = this.approxPerfs.toSet

    val wrong = already -- availTvm
    
    this.filter { x => wrong.contains(x.toPerformance.toApproxPerformance) }

  }
  
  // -------------
  // Misc
  
  def deleteMdFile() = {
    for (music <- this) {
      music.deleteMdFile()
    }
    this
  }

}

object ManagedMusic {

  def apply(lib: Library, i: Iterable[MusicFile]) = new ManagedMusic(lib) {
    def iterator = i.iterator
  }

  def flatApply(lib: Library, i: Iterable[ManagedMusic]) = new ManagedMusic(lib) {
    def x = i.iterator.toList flatMap (z => z)
    def iterator = x.iterator
  }

}

abstract class ManagedPlaylists(val lib: Library)
  extends Managed[PlaylistFile, ManagedPlaylists] with DesktopCompat {

  def absolute = filtre(!_.relative)

  override def name = "Playlists: " + size + " item(s)"

  def maker = new ManagedMaker[PlaylistFile, ManagedPlaylists]() {
    def make(i: Iterable[PlaylistFile]) = new ManagedPlaylists(lib) {
      def iterator = i.iterator
    }
  }

  // ----------------
  // Operators

  def containing(m: ManagedMusic) = {
    def file(mf: MusicFile) = mf.file

    val files = (m.toSet) map file
    filtre {
      pl =>
        {
          val plFiles = (pl.indirectContents map file).iterator.toSet
          val intersect = (files intersect plFiles)
          !(intersect.isEmpty)
        }
    }
  }

  def broken = filtre(_.broken)

  // -----------------
  // Refactorings

  def adjust() : Boolean = {
    var changed = false
    for (t <- iterator) {
      
      if (t.adjust()) {
        val ts = t.file.lastModified
        t.save
        t.file.setLastModified(ts)
        changed = true
      }
    }
    if (changed) {
      lib.markDirty()
    }
    return changed
  }
  
  def relativize() = {
    var changed = false
    for (t <- iterator; if !t.relative) {
      t.relative = true
      t.save
      changed = true
    }
    if (changed) {
      lib.quick()
      lib.markDirty()
    }
  }

  def prefer(music: ManagedMusic) = {
    for (m <- music) {
      for (t <- this) {
        t.prefer(lib, m)
      }
    }
    this
  }

  override def repl(from: String, to: String) = {
    val x = new ReplicationStrategy.Ogg(new File(from).toPath(), new File(to).toPath())
    replicate(from, to, x)
    indirectContents.replicate(from, to, x)
    val db = new File(to, "db.json")
    toJson(x, db)
    this
  }
  
  /**
   * JSON representation
   */
  def toJson(strategy: ReplicationStrategy) : String = {
    val p = new StringBuffer() 
    
    def f(file: File) = strategy.json.getOrElse(file, null)
    
    val pl = (for (m <- this) yield f(m.file)).mkString(", \n")
    val music = (for (m <- indirectContents) yield f(m.file)).mkString(", \n")

    p
    .append(" { \"playlists\": [ ").append(pl).append(" ], \n")
    .append(" \"music\": [ ").append(music).append(" ] }\n");
    
    p.toString
    
  }  

  def toJson(strategy: ReplicationStrategy, file: File) {
    val w = new PrintWriter(new FileWriter(file))
    w.print(toJson(strategy))
    w.close()
  }
  
  // ---------------------------

  /**
   * Format for printing (TODO generalise formatting)
   */
  def print {
    println((for (m <- this) yield {
      m.file.toString()
    }).mkString("\n"))
  }
  
  def prettyFormat : String = {
    // Newline at each genre change
    
    val buff = new StringBuffer
    
    var lastGenre : Option[String] = None
    
    val contents = for (pl <- playlists; tr <- pl.tracks) yield { lib.resolve(tr) }
    
    val mds = for (tr <- contents; md <- tr.md) yield { md }
    
    def toYear(d: RecordingDate) = { if (d != null) d.toYear else "?" }
    
    for (md <- mds) {
      
      val genre = Some(md.genre)
      val skip = ! genre.equals(lastGenre)
      lastGenre = genre
      
      if (skip) {
        buff.append("\n")
      }

      val tvm = List("tango", "vals", "milonga").toSet.contains(md.genre)
      if (tvm) {
        buff
          .append(md.artist)
          .append(" - ")
          .append(md.title)
          .append(" - ")
          .append(toYear(md.year))
          .append(" - ")
          .append(md.genre)
          .append("\n")
      } else {
        buff
          .append(md.artist)
          .append(" - ")
          .append(md.title)
          .append("\n")
      }
      
    }
    
    return buff.toString()
  }
  
  def prettyFormat(file: File) {
    val w = new PrintWriter(new FileWriter(file))
    w.print(prettyFormat)
    w.close()
  }
  
  def prettyFormat(filename: String) {
    prettyFormat(new File(filename))
  }

  def prettyRTF(filename : String, title: Option[String]) {
    // Newline at each genre change
    
    val rtf = Rtf.rtf()
    
    rtf.header(RtfHeader.font("Bitstream Charter").at(0))
    
    val accum = new MutableList[RtfText]
    
    val paras = new MutableList[RtfPara]

    for (t <- title) {
      paras += RtfPara.p(RtfText.bold(t))
    }

    def flush() {
      val arr = accum.toArray
      val p = RtfPara.p(arr:_*)
      paras += p
      accum.clear()
    }
    
    def text(txt: String) = RtfText.font(0, txt)
    
    var lastGenre : Option[String] = None
    
    val contents = for (pl <- playlists; tr <- pl.tracks) yield { lib.resolve(tr) }
    
    val mds = for (tr <- contents; md <- tr.md) yield { md }
    
    def toYear(d: RecordingDate) = { if (d != null) d.toYear else "?" }
    
    for (md <- mds) {
      
      val genre = Some(md.genre)
      val skip = ! genre.equals(lastGenre)
      lastGenre = genre
      
      if (skip) {
        flush()
      }

      val tvm = List("tango", "vals", "milonga").toSet.contains(md.genre)
      if (tvm) {
        
        val line = md.artist + " - " + md.title + " - " + toYear(md.year) + " - " + md.genre
        
        accum += text(line)
        accum += RtfText.lineBreak()
        
      } else {
        val line = md.artist + " - " + md.title
        accum += RtfText.italic(text(line))
        accum += RtfText.lineBreak()
      }
      
    }
    
    flush()
    rtf.section(paras.asJava)
    
    rtf.out(new FileWriter(new File(filename)))
  }
  
  def transcribeToRTF {
    for (m <- this) {
      val single = this.filtre { x => x == m }
      val filename = m.file.getAbsolutePath.replaceAll("\\.[^\\.]+", ".rtf")
      val title = m.file.getName.replaceAll("\\.[^\\.]+", "")
      
      val dest = new File(filename)
      if (! dest.exists()) {
        single.prettyRTF(filename, Some(title))
      }
      
    }
  }
  
  
  def toTandas(root: File) {
    for (m <- this) {
      val path = m.toTandaFile(lib)

      val loc = root.toPath().resolve(path).toAbsolutePath().toFile()

      println("Save " + m.file + " to " + loc)

      val p = new M3UPlaylist(loc, lib)
      p.tracks = m.tracks
      p.relative = true

      p.save()
    }
  }

  def expand(padding: ManagedMusic): ManagedMusic = {
    
    val list = new MutableList[MusicFile]
    val size = playlists.indirectContents.size
    
    for (pl <- playlists) {
      for (tr <- pl.indirectContents) {
        val m = lib.resolve(tr); 
        list += m;
      }
      for (tr <- padding.indirectContents) {
        val m = lib.resolve(tr); 
        list += m;
      }
    } 
    
    ManagedMusic(lib, list)
  }
  
}

object ManagedPlaylists {
  def apply(lib: Library, i: Iterable[PlaylistFile]) = new ManagedPlaylists(lib) {
    def iterator = i.iterator
  }
}

trait ManagedMaker[T <: MusicContainer, S <: Managed[T, S]] {
  def make(i: Iterable[T]): S
}
