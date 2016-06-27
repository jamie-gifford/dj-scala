package au.com.thoughtpatterns.djs.lib

import au.com.thoughtpatterns.djs.util.Log
import java.io.PrintWriter
import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.file.Path
import java.io.File
import java.net.URL
import au.com.thoughtpatterns.djs.disco.Artists

@SerialVersionUID(1L)
abstract class PlaylistFile(private val file0: File, val lib : Library) extends MusicContainer with Playlist with Serializable {

  val file = file0.getCanonicalFile()

  var relative: Boolean = false

  private var tracks0: List[MusicFile] = Nil

  def indirectContents: MusicSet = MusicSet(tracks)

  def tracks = tracks0

  def tracks_=(t: List[MusicFile]): Unit = tracks0 = t

  def saveToString: String

  def save() {
    if (!file.exists()) {
      file.getParentFile().mkdirs()
      file.createNewFile();
    }
    val writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
    try {
      writer.print(saveToString);
    } finally {
      writer.close();
    }
    val parent = file.getParentFile();
    parent.setLastModified(System.currentTimeMillis());
  }

  /**
   * eg /media/Music-brick-0/Music/ogg to /home/james/dj/Music/ogg
   */
  def reroot(from: Path, to: Path): Boolean = {

    val root = new File("/").toPath();

    def replace(track: MusicFile): MusicFile = {
      val path = root.resolve(track.file.toPath())

      if (path.startsWith(from)) {
        val rel = from.relativize(path);
        val newpath = to.resolve(rel);
        val newFile = newpath.toFile();
        new MusicFile(newFile, lib);
      } else {
        track
      }
    }

    val rerooted = for (t <- tracks) yield { replace(t) }

    if (rerooted != tracks) {
      val old = saveToString
      tracks0 = rerooted
      Log.info("Rewrite playlist " + file + "\nfrom \n" + old + "to\n" + saveToString)
      true
    } else {
      false
    }
  }
  
  /**
   * Adjust ogg<->flac extensions 
   */
  def adjust() : Boolean = {
    
    def adj(track: MusicFile): MusicFile = {
      
      var out : Option[MusicFile] = None
      
      if (! track.file.exists()) {
        val p = track.file.getAbsolutePath
        val idx = p.lastIndexOf('.')
        val ext = p.substring(idx + 1)
        
        val ext2 = ext.toLowerCase match {
          case "flac" => Some("ogg")
          case "ogg" => Some("flac")
          case _ => None
        }
        
        for (ex <- ext2) {
          val p2 = p.substring(0, idx + 1) + ex
          val f2 = new File(p2)
          if (f2.exists) {
            out = Some(new MusicFile(f2, lib)) 
          }
        }
      }
      
      if (! out.isDefined) {
        out = Some(track)
      }
      
      out.get
    }

    val adjusted = for (t <- tracks) yield { adj(t) }

    if (adjusted != tracks) {
      val old = saveToString
      lib.dirty = true
      tracks0 = adjusted
      Log.info("Rewrite playlist from \n" + old + "\nto\n" + saveToString)
      return true
    } else {
      return false
    }
    
  }

  def prefer(lib: Library, m: MusicFile) {

    val p = m.toApproxPerformance
    val tracks1 =
      for (t <- tracks0) yield {
        if (m.file == t.file)
          t
        else {
          val q = lib.resolve(t).toApproxPerformance
          if (q == p)
            m
          else
            t
        }
      }

    val t0 = tracks0
    if (tracks1 != tracks0) {
      tracks0 = tracks1
      save()
      update()
    }

  }

  /**
   * @return a Path for representing this playlist as a tanda, if possible.
   * The format of the path is
   * <genre>/<base artist>/<vocalist> -  <year - year> - <rating> <titles>
   */
  def toTandaFile(lib: Library) : Path = { 
    
    val metas = for (t0 <- tracks; t = lib.resolve(t0); md <- t.md) yield { md }
    
    val meta = metas headOption
    val genre = meta.map({ _.genre }).headOption.getOrElse("no-genre")
    val artist = meta.map({ _.artist }).headOption.getOrElse("no-artist")
    
    val split = "(.+), voc. (.+)".r
    
    val abbrev = Artists.abbrev _
    
    def getBaseArtist(artist: String) = abbrev(artist match {
      case split(base, vocalist) => base
      case _ => artist
    })
    
    def getVocalist(artist: String) = abbrev(artist match {
      case split(base, vocalist) => vocalist
      case _ => null
    })
    
    val baseArtist = getBaseArtist(artist)
    
    val vocalists = 
      (for (md <- metas; voc = getVocalist(md.artist); if voc != null) yield { voc } ) flatMap { _.split(", +") } toSet

    val vocs = 
      if (vocalists.size > 0)
        if (vocalists.size <= 2)
         vocalists.toList.sorted.mkString(", ")
        else 
          "Various"
      else 
        "Instrumental"
      
    val signature = (for (md <- metas) yield { md.title }) map { lib.mostSignificantWord(_) } take 5
      
    val signatureString = signature.mkString(" | ")
    
    val years = (metas map {_.year}) filter { _ != null } map { _.approx } filter { _ != null } map { _.from.year }
    
    val date = {
      if (years.isEmpty) 
        "?"
      else {
	    val fromYear = years.min
	    val toYear = years.max
	    
	    if (fromYear != toYear)
	      fromYear + "-" + toYear
	    else
	      "" + fromYear
	    }
    }
    
    val rating = 
      if (metas.size > 0)
        (for (md <- metas) yield { md.rating.getOrElse(0d) }).foldLeft(0d)({ _ + _ }) / metas.size
      else 
        0d
        
    def createStars(stars: Int) : String = 
      if (stars <= 0) "" else "*" + createStars(stars - 1)
        
    val stars = createStars((5 * rating).round.toInt)  // TODO
        
    val components = List(genre, baseArtist, baseArtist + ", " + vocs + " - " + date + " - " + signatureString + " (" + stars + ")")

    val path = new java.io.File(components.mkString("/") + ".m3u").toPath()
    
    path
  }

  override def toJson : String = {

    def quote(s: String): String = if (s != null) s.replaceAllLiterally("\"", "\\\"") else ""
    
    val contents = (for (t <- tracks) yield {
      val qf = quote(t.file.getAbsolutePath)
      s""" "$qf" """
    }).mkString(",")

    val qfile = quote(file.getAbsolutePath)

    s"""
      |   {"file":"$qfile",
      |    "contents": [ $contents ]
      |   }
      |""".stripMargin
  }
  
}

object PlaylistFile {

  def apply(f: java.io.File, lib: Library): PlaylistFile = {
    new M3UPlaylist(f, lib)
  }

}
    
