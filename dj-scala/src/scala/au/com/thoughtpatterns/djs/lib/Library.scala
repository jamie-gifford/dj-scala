package au.com.thoughtpatterns.djs.lib

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.PrintWriter
import au.com.thoughtpatterns.core.util.CsvUtils
import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.disco.Disco.Source
import au.com.thoughtpatterns.djs.util.Log
import au.com.thoughtpatterns.djs.model.Bayes
import au.com.thoughtpatterns.djs.disco.Types._
import java.nio.file.Path
import au.com.thoughtpatterns.djs.disco.importer.Importer
import java.io.FileWriter

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

  var dirty : Boolean = false

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
            dirty = true
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
    if (toDelete.size > 0) dirty = true
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
      dirty = true
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
  }

  def containers = contents.values

  def write() {
    if (dirty) write0()
    dirty = false
  }
  
  def write0() {
    MetadataCache.write()
    FileMetadataCache.write()
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

  /**
   * Given a MusicContainer t, return the library-managed version if exists, otherwise
   * return t itself.
   */
  def resolve[T <: MusicContainer](t: T): T = {
    val r = contents.get(t.file) match {
      case Some(x: T @unchecked) => x
      case _ => t
    }
    r
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
    write
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