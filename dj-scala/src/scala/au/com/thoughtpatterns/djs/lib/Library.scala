package au.com.thoughtpatterns.djs.lib

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.PrintWriter
import au.com.thoughtpatterns.core.util.CsvUtils
import au.com.thoughtpatterns.djs.disco.Fakebook
import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.disco.Disco.Source
import au.com.thoughtpatterns.djs.util.Log
import au.com.thoughtpatterns.djs.model.Bayes
import au.com.thoughtpatterns.djs.disco.Types._
import java.nio.file.Path
import au.com.thoughtpatterns.djs.disco.importer.Importer
import java.io.FileWriter
import java.io.LineNumberReader
import java.io.FileReader

@SerialVersionUID(1L)
class Library(val libFile: Option[File]) extends ManagedContainers with DesktopCompat with Serializable {

  private var contents: Map[File, MusicContainer] = Map()

  import au.com.thoughtpatterns.djs.disco.Types.SpanishWord
  
  def lib = this

  override def name = "Music library" 
  
  lazy val ourDisco = new Disco(Some(this))
  
  Log.info("Constructing library, disco = " + ourDisco)

  def iterator = contents.values.iterator
  
  private var initCommands : List[String] = null

  private var dirty0 : Boolean = false
  
  def markDirty() {
    dirty0 = true
  }

  def dumpContents : Unit = {
    for (c <- contents.keys) {
      Log.info(c.toString());
    }
  }
  
  def add(f: File): Unit = {
    contents.get(f) match {
      case None =>
        {
          val c = if (f.isDirectory())
            Some(LibraryDir(f, this))
          else {
            val bits = f.getName().toLowerCase().split("\\.")
            if (List("ogg", "flac").contains(bits.last)) {
              Some(new MusicFile(f, this))
            } else if ("m3u" == bits.last) {
              Some(PlaylistFile(f, this))
            } else None
          }
          for (c0 <- c) {
            contents = contents + (f -> c0)
            markDirty()
            c0.update()
          }
        }
      case Some(already) => {}
    }
  }

  /**
   * Bring entire library up to date with respect to filesystem.
   */
  def update() {
    val toDelete = contents map {_._1} filter {!_.exists()}
    contents = contents -- toDelete
    if (toDelete.size > 0) markDirty()
    update0()
  }

  /**
   * Quick version of {@link refresh} which only checks dirty directories.
   */
  def quick() {

    val dirtyParents = (for (
      (f, c) <- contents;
      if (f.isDirectory() && c.check)
    ) yield { f }).iterator.toSet

    Log.info("Quick check has " + dirtyParents.size + " dirty directories")

    if (dirtyParents.size > 0) {
      for ((f, c) <- contents) {
        val dir = f.getParentFile()
        if (dirtyParents.contains(dir)) {
          c.check()
        }
      }
      updateDirty()
    }
  }

  private def update0() {
    for ((f, c) <- contents; if c.check) { c.update() }
    for ((f, c) <- contents; if ! c.exists) {
      contents = contents - f
      markDirty()
    }
  }

  def updateDirty() {
    for ((f, c) <- contents) { c.updateIfDirty() }
  }

  @SerialVersionUID(1L)
  case class LibraryDir(val file: File, val lib: Library) extends MusicContainer with Serializable {

    def read() = {
      if (file.exists() && file.isDirectory()) {
        for (f <- file.listFiles()) {
          add(f)
        }
      }
    }
    
    override def toString = file.toString()
  }

  def containers = contents.values

  def write() {
    if (dirty0) {
      dirty0 = false
      write0()
    }
  }
  
  def write0() {
    MetadataCache.write()
    FileMetadataCache.write()
    SoxiCache.write()
    for (file <- libFile) {
      Log.info("Saving library " + file)
      val o = new ObjectOutputStream(new FileOutputStream(file))
      o.writeObject(this)
      o.close()
    }
  }
  
  def dump(out: File) : Unit = {
    
    def s(x: Any) = if (x != null) x.toString else null;
    
    def f(f: File, m : Metadata) : Array[String] = {
      Array(
        m.genre,
        m.title,
        m.artist,
        s(m.year),
        s(m.rating.getOrElse(null)),
        m.album,
        s(m.track),
        f.getPath(),
        m.comment
      )
    }
    
    val lines = for (m <- music; md <- m.md) yield f(m.file, md)
    
    val size = lines.size
    
    val arr = new Array[Array[String]](size)
    
    lines.copyToArray(arr)
    
    val utils = new CsvUtils();
    utils.toCsv(arr)
    
    val str = utils.getFormattedString()
    
    val p = new PrintWriter(new FileWriter(out))
    p.write(str)
    p.close()
  }

  private def rel(root: File, f: File) : File = {

    var base = root.getAbsoluteFile
    if (! base.isDirectory) {
      base = base.getParentFile.getAbsoluteFile
    }

    return base.toPath.relativize(f.toPath).toFile
  }

  /**
   * Given a MusicContainer t, return the library-managed version if exists, otherwise
   * return t itself.
   */
  def resolve[T <: MusicContainer](t: T): T = {
    val f = libFile.get match {
      case base: File => rel(base, t.file)
      case _ => t.file
    }

    val r = contents.get(f) match {
      case Some(x: T @unchecked) => x
      case _ => t
    }
    r
  }
  
  /**
   * Given a File, return the corresponding MusicContainer 
   */
  def resolve(f0 : File) : Option[MusicContainer] = {
    val f = libFile.get match {
      case base: File => rel(base, f0)
      case _ => f0
    }
    contents.get(f)
  }

  /**
   * Given a music file, rate it according to equivalent music in library
   */
  def rate(m: MusicFile) = {

    def rating(m: MusicFile): Double = m.md match {
      case Some(md) => md.rating match { case Some(r) => r case _ => 0 }
      case _ => 0
    }

    val mine = rating(lib.resolve(m))

    val exact = music groupBy { _.toPerformance }
    val exacts = exact.getOrElse(m.toPerformance, Nil).map(rating(_))
    val exactRating = exacts.toList.max

    val approx = music groupBy { _.toApproxPerformance }
    val approxes = approx.getOrElse(m.toApproxPerformance, Nil).map(rating(_))
    val approxRating = approxes.toList.max

    if (mine > 0) mine else if (exactRating > 0) exactRating else approxRating
  }
  
  /**
   * Given a music file, get BPM if available (might be from other instance of same performance)
   */
  def findBPM(m: MusicFile) = {
    def b(m: MusicFile): Double = m.md match {
      case Some(md) => md.bpm match { case Some(r) => r case _ => 0 }
      case _ => 0
    }

    val mine = b(lib.resolve(m))

    val exact = music groupBy { _.toPerformance }
    val exacts = exact.getOrElse(m.toPerformance, Nil).map(b(_))
    val exactBpm = exacts.toList.max

    if (mine > 0) mine else if (exactBpm > 0) exactBpm else 0
  }

  /**
   * Get the "best" discography known (using approximate performances)
   */
  def disco: Iterable[Pair[Performance, Set[Source]]] = {

    val dd = ourDisco;

    val d = dd.performances
    val out = for (
      coset <- d;
      p = coset.rep;
      srces = dd.getEquivSourced(p)
    ) yield Pair(p.toLibPerformance, srces map { _.src })

    out
  }

  /**
   * Get discography as ManagedPerformances
   */
  def disco2: ManagedPerformances = {
    val d = ourDisco.performances
    val perfs = for (coset <- d; p = coset.rep)
      yield p.toLibPerformance
    ManagedPerformances.apply(this, perfs)
  }

  /**
   * Write CSV data to the given file indicating missing/present entries in the discography.
   */
  def writeMissing(f: File) {
    val utils = new CsvUtils

    val genres = Set("tango", "vals", "milonga")

    val dd = ourDisco;

    val d = dd.performances
    val out = for (
      p <- d;
      srces = dd.getEquivSourced(p)
    ) yield Pair(p.rep.toLibPerformance, srces map { _.src })

    def s(x: AnyRef) = if (x != null) x.toString() else null
    
    val lines = (
      for (
        coset <- dd.performances;
        p = coset.rep;
        srces = dd.getEquivSourced(p) map { _.src };
        in = srces.contains(dd.libSrc);
        if (p.genre != null && genres.contains(p.genre))
      ) yield List(if (in) "1" else "0", s(p.date), p.name, s(p.performer), srces.toList.sortBy({_.toString}).mkString(","), p.genre).toArray).toArray

    utils.toCsv(lines)

    val pw = new PrintWriter(f)
    pw.print(utils.getFormattedString())
    pw.close()
  }

  def tokenise(str: String) = if (str != null) str.split("(?U)\\W").map(new SpanishWord(_)).toSeq else Seq()

  lazy val titles = {
    val fullTitles = (disco map { _._1.title }).toSet
    fullTitles map { tokenise(_) }
  }

  lazy val titleModel = new Bayes[SpanishWord](titles)

  def mostSignificantWord(title: String) = {
    val tokens = tokenise(title).toSeq
    val ratios = (tokens map (w => (w -> titleModel.ratio(w)))).toMap

    val most = tokens.tail.foldLeft(tokens.head)((x, y) =>
      if (ratios.getOrElse(x, 0d) >= ratios.getOrElse(y, 0d)) x else y)
      
    most  
  }
  

  /**
   * Given a directory, list the immediate subdirectories that don't contain music by any of the rated artists in the library
   */
  def listUninterestingSubdirs(root: java.io.File, superroot: java.io.File) : List[Path] = {
    
    import java.io.File
    
    // Interesting is defined by artists that have been rated as 0.1 or better.
    
    val interestingArtists = (for (f <- m.rated; md <- f.md; rating <- md.rating; if (rating >= 0.1 && md.artist != null)) yield md.artist).toSet 

    def isTarget(f : MusicFile) = f.file.toPath().startsWith(root.toPath())
    
    val interestingFiles = //m filter isTarget filter map {_.file}
      for (f <- m; if (isTarget(f)); md <- f.md; if (interestingArtists.contains(md.artist))) yield f.file
    
    val tmp = interestingArtists.toList.sorted
    
    for (t <- tmp) println(t)
    
    def findRoot(f: File) : File = {
      if (f == null) null else if (f.getParentFile() == root) f else findRoot(f.getParentFile())
    } 
    
    val interestingSubdirs = (interestingFiles map findRoot filter {_ != null}).toSet
    val allSubdirs = (root.listFiles() filter {_.isDirectory}).toSet
    
    val uninterestingSubdirs = allSubdirs.diff(interestingSubdirs).toSet
    
    val superrootPath = superroot.toPath
    
    val uninterestingPaths = uninterestingSubdirs map { f => superrootPath.relativize(f.toPath) }
    
    return uninterestingPaths.toList.sorted
  }

  def importCmd(path: String) {
    initCommands = scala.io.Source.fromFile(new File(path)).getLines.toList
    write0
    Log.info("INIT commands imported from " + path)
    for (l <- getInitCommands) Log.info("INIT: " + l)
  }

  def getInitCommands = if (initCommands != null) initCommands else List()
  
  def exportInitCommands(path: String) {
    var p = new PrintWriter(new File(path))
    for (l <- getInitCommands) p.println(l)
    p.close
  }
  
  def im(path: String) : Importer = {
    
    val root = new File(path)
    def accept(f: File) : Boolean = true
    
    new Importer(this, root, accept)
  }
  
  /**
   * Two-way synch (based on modification times) of m3u and m0u files. Looks in directories containing playlists.
   */
  def syncM0U() {
    
    val dirs = contents.keySet.filter { x => x.isDirectory }
    var dirty = false
    
    for (dir <- dirs) {
      val d = syncM0U(dir)
      dirty = dirty || d
    }
    
    if (dirty) {
      for (p <- playlists) {
        // Force update of all playlists since we forge the modification time
        p.update()
      }
    }

    write()
  }
  
  def syncM0U(dir: File) : Boolean = {
    
    import scala.io.Source

    class Pair(val base: String) {
      
      var m3u : Option[File] = None
      var m0u : Option[File] = None
      
      def sync() : Boolean = {
        val ts3 = m3u.map { f => f.lastModified() } getOrElse(0L)
        val ts0 = m0u.map { f => f.lastModified() } getOrElse(0L)
        var dirty = false
        if (ts3 > ts0) {
          sync3to0()
          dirty = true
        }
        if (ts0 > ts3) {
          sync0to3()
          dirty = true
        }
        
        return dirty
      }
      
      def sync3to0() {
        val src = m3u.get
        val dest = new File(base + ".m0u")
        val writer = new PrintWriter(new FileWriter(dest))
        for (line <- Source.fromFile(src).getLines()) {
          val line0 = line.replaceAll("\\.[^\\.]+$", "")
          writer.println(line0)
        }
        writer.close()
        dest.setLastModified(src.lastModified())
        Log.info("Synched m0u " + dest)
      }
      
      def sync0to3() {
        val src = m0u.get
        val dest = new File(base + ".m3u")
        val writer = new PrintWriter(new FileWriter(dest))
        for (line <- Source.fromFile(m0u.get).getLines()) {
          // Append "flac". This might not be right but later the library will adjust it
          val line0 = line + ".flac"
          writer.println(line0)
        }
        writer.close()
        dest.setLastModified(src.lastModified())
        Log.info("Synched m3u " + dest)
      }
      
    }
    
    var map : Map[String, Pair] = Map()
    
    for (f <- dir.listFiles()) {
      
      def base(f: File) = {
        f.toString().replaceAll("\\.[^\\.]+$", "")
      }
      def ext(f: File) = {
        val s = f.toString()
        val idx = s.lastIndexOf('.')
        if (idx >= 0) s.substring(idx + 1) else ""
      }
      
      var b = base(f)
      val pair = map.getOrElse(b, new Pair(b))
      var e = ext(f).toLowerCase()
      e match {
        case "m3u" => pair.m3u = Some(f)
        case "m0u" => pair.m0u = Some(f)
        case _ => 
      }
      map = map + ( b -> pair )
    }
    
    var dirty = false
    for ((b, pair) <- map) {
      val d = pair.sync()
      dirty = dirty || d
    }
    
    return dirty
  }
  
  def fakebook : ManagedMusic = {
    var x = m.tvm.title("---");
    for (t <- Fakebook.tfb) {
      x = (x || m.tvm.title(t)).m
    }
    return x
  }
  
  
  def help {
    
    print(s"""
Useful objects/functions:

Library

  l : library
  m = l.m : music

Managed collection functions

  ||, \\, && : combining operators

  repl      : replicate using "Ogg" strategy, good for phone. 
              eg tandas.repl("/media/Orange/djs/Music", "/media/Orange/replica")

  replRetune : replicate using "Ogg" strategy and retune to 442 Hz. 
              eg tandas.repl("/media/Orange/djs/Music", "/media/Orange/replica-retuned")

  replShare   : replicate using "MP3" strategy, renaming files to simple format and stripping tags. 
              Good for exports. 
              eg tandas.replShare("/media/djs/Orange/Music", "/tmp/export")

  replDJ    : replicate using "DJ" strategy (ie, compress to Ogg if less than two stars).
              Good for additional DJ rigs
              eg l.replDJ("/media/djs/Orange/Music", "/home/djs/replica-dj/Music")

  replDJRetune : replicate using "DJ" strategy (ie, compress to Ogg if less than two stars), *retuning*.
              Good for additional DJ rigs
              eg l.replDJRetune("/media/djs/Orange/Music", "/home/djs/replica-dj/Music")

  replUpstream : replicate missing music files using copy
              Good for propagating music files from downstream to upstream
              eg l.replUpstream("/home/djs/replica-dj/Music", "/media/Orange/Music")

Managed music functions

   require  : filter on metadata function
   artist, genre, title: filter on these metadata items
   yearRange: filter on years
   unrated, rated, minRating : filter on rating

   dups     : find dups within selection
   ~, \\~    : combination operators "like"
   suggest(tandas) : depth-1 suggestions from tandas
   closure(tandas) : depth-100 suggestions from tandas
   closure(tandas, n) : depth-n suggestions from tandas

Refactorings

   m.exchange(pl)

   * Use this contents in preference to equivalent music in the given playlists.
   * Also, transfer ratings to this music if nececessary and prune the dups in the entire library like this.

   m.fixShortEnds(minEnd: Double)

   * Ensure all selected music has silence of at least minEnd seconds at end

   m.replaygain

   * Ensure all selected music has the replaygain tags defined

   m.synchronise(n)
 
   * synchronise with tango.info to last n days (based on TIN=XXX;SIDE=S) comments

   m.previewTitles, m.fixTitles
 
   * update track titles to match case, accents from tango.info

   m.identify, m.applyIdentify

   * use Bayes model to identify tracks, write candidates to /tmp/identify.csv; or read /tmp/identify.csv with 
     marked accepted tracks and apply to files.

Playlist

   p.transcribeToRTF

   * write pretty formatted milonga playlist

     """.stripMargin)
    
  }
}

object Library {
  def load(file: File) : Library = {
    if (file.exists) {
      try {
        Log.info("Loading " + file + "...")
        val r = new ObjectInputStream(new FileInputStream(file)).readObject().asInstanceOf[Library]
        Log.info("... done loading " + file)
        return r
      } catch {
        case x: Exception => { Log.error(x); new Library(Some(file)) }
      }
    } else {
      new Library(Some(file))
    }
  }


}