package au.com.thoughtpatterns.djs.lib

import java.io.File
import au.com.thoughtpatterns.djs.tag.TagFactory
import au.com.thoughtpatterns.djs.clementine.PlayerInterfaceFactory
import java.nio.file.Path
import au.com.thoughtpatterns.djs.util.ProcessExec
import au.com.thoughtpatterns.core.json.JsonyObject
import au.com.thoughtpatterns.core.json.AJsonyObject
import au.com.thoughtpatterns.djs.util.Log
import au.com.thoughtpatterns.djs.disco.tangoinfo.Data
import au.com.thoughtpatterns.dj.disco.tangoinfo.Letras

@SerialVersionUID(3L)
class MusicFile(private val file0: File, val lib: Library) extends MusicContainer with DesktopCompat with Serializable {

  val file = file0.getCanonicalFile()
  
  val mdFile = MusicFile.fileToMdFile(file)
  
  override def fileAge = {
    val age1 = if (file.exists) file.lastModified() else 0
    val age2 = if (mdFile.exists) mdFile.lastModified() else Long.MaxValue;
    Math.max(age1, age2)
  }

  var md: Option[Metadata] = None

  def name = if (md.isDefined) md.get.title else file.getAbsolutePath()

  override def metadata = if (md.isDefined) {
    def fmt(s: Any) = if (s != null) s.toString else ""
    val m = md.get
    List("Title" -> m.title, "Artist" -> m.artist, "Composer" -> m.composer, "Date" -> fmt(m.year), "Genre" -> m.genre, "Rating" -> m.rating.getOrElse(0d).toString)
  } else {
    List("file" -> file.getAbsolutePath())
  }

  def size = 1

  class Play extends Operation("Play") {
    def exec() {
      PlayerInterfaceFactory.getPlayer().addTrack(MusicFile.this)
    }
  }

  override def ops = List(new Play)

  def endSilence: Option[Double] = {
    FileMetadataCache.get(file) match {
      case Some(m) => m.endSilenceSeconds
      case _ => None
    }
  }

  def shortEnd: Boolean = {
    endSilence match {
      case Some(x) if (x < 2) => true
      case _ => false
    }
  }

  def extension : String = {
    val path = file.getAbsolutePath
    val i = path.lastIndexOf('.')
    return path.substring(i)
  }
  
  def padEndSilence(endSilence: Double): Unit = {
    
    val path = file.getAbsolutePath
    val tmpFile = File.createTempFile("dj-tmp-", extension);
    tmpFile.deleteOnExit();

    // sox 12_-_Catamarca.flac tmp.flac pad 0 1.3 

    val cmd = List(
      "sox",
      path,
      tmpFile.getAbsolutePath(),
      "pad",
      "0",
      endSilence.toString
      )

    if (ProcessExec.exec(cmd).resultCode == 0) {
      val cmd2 = List(
        "cp",
        tmpFile.getAbsolutePath(),
        path)

      val d = ProcessExec.exec(cmd2)
    }
    
  }

  def replaygain(): Unit = {
    val path = file.getAbsolutePath
    val ext = extension
    ext match {
      case ".ogg" =>
        ProcessExec.exec(List(
            "vorbisgain",
            path
            ))
        update()
      case ".flac" =>
        ProcessExec.exec(List(
            "metaflac",
            "--add-replay-gain",
            path
            ))
        update()
      case _ => 
    }
  }

  /**
   * "Passive" method which will use locally available letras only.
   */
  def letra : Option[String] = {
    
    val out = md match {
      case Some(m) => {
        if (m.composer == null) {
          None 
        } else {
          val title = m.title.replaceFirst(" +\\[.*", "");  
          val filename = title + ", com. " + m.composer;
          val l = MusicFile.letras.getLetraFile(filename);
          if (l == null) None else Some(l)
        }
      }
      case None => {
        None
      }
    }
      
    return out;
  }
  
  def hasLetra : Boolean = letra.isDefined 
  
  /**
   * "Active" method which will attempt to fetch from tango.info and todotango
   */
  def fetchLetra : Option[String] = {
    
    val out = md match {
      case Some(m) => {
        if (m.composer == null) {
          None 
        } else {
          val bits = m.composer.split(", let\\. ");
          val composer = bits(0);
          val letrista = if (bits.length >= 2) bits(1) else null;
          val l = MusicFile.letras.getLetra(m.title, m.genre, composer, null)
          if (l == null) None else Some(l)
        }
      }
      case None => {
        None
      }
    }
    return out;
  }
  
  type Props = Map[String, String]

  @transient
  private var _extraProps: Option[Props] = None

  def read() = {
    md = MetadataCache.get(file)
  }

  def write = {
    val tag = new TagFactory().getTag(file);
    for (m <- md) {
      import m._
      tag.setTitle(title)
      tag.setArtist(artist)
      tag.setAlbum(album)
      tag.setYear(year)
      tag.setComment(comment)
      tag.setGenre(genre)
      tag.setTrack(track)
      tag.setRating(rating match { case Some(x) => x case _ => 0 });
      tag.setComposer(composer)

      tag.write();

      // Touch parent dir...
      val dir = file.getParentFile();
      dir.setLastModified(System.currentTimeMillis());
    }
  }
  
  override def toJson: String = toJson(file.getAbsoluteFile.toPath)

  def toJson(p: Path): String = {
    
    var out = new AJsonyObject();

    def quote(s: String): String = if (s != null) s.replaceAllLiterally("\"", "\\\"") else ""

    for ((key, value) <- metadata) {
      out.set(key, value)
    }
    val qfile = quote(p.toString)
    out.set("file", qfile)
    out.toJson()
  }

  override def toString = md match { case Some(m) => m.toString case None => file.toString() }

  def toPerformance: Performance = md match {
    case Some(m) => m.toPerformance
    case None => Performance("none", "none", "none", null)
  }

  def toApproxPerformance: Performance = md match {
    case Some(m) => m.toApproxPerformance
    case None => Performance("none", "none", "none", null)
  }

  def extraProps = {
    (if (_extraProps != null) _extraProps else None) match {
      case Some(p) => p
      case None => {
        val p = MusicFile.CommentHelper.parseProps(
          md match { case Some(m) => m.comment case None => null })
        _extraProps = Some(p)
        p
      }
    }
  }

  override def replicate(strategy: ReplicationStrategy) {
    for (target <- strategy.dirtyTarget(file)) {
      strategy.transcode(this, file, target)
    }

    for (target <- strategy.target(file)) {
      val path = strategy.relativize(target.file)
      strategy.json.put(file, toJson(path))
    }

  }
  
  def lookupComposer : Option[String] = {
    val p = toApproxPerformance
    Data.composer(p.title, p.artist, p.genre)
  }
  
  // ------------------------
  //
  
  def bits : Option[Integer] = {
    SoxiCache.get(file) match {
      case Some(m) => m.bits
      case _ => None
    }
  }

  def channels : Option[Integer] = {
    SoxiCache.get(file) match {
      case Some(m) => m.channels
      case _ => None
    }
  }

  def sampleRate : Option[Double] = {
    SoxiCache.get(file) match {
      case Some(m) => m.rate
      case _ => None
    }
  }
  
  def encoding : Option[String] = {
    SoxiCache.get(file) match {
      case Some(m) => m.encoding
      case _ => None
    }
  }

  override def deleteMdFile() {
    if (mdFile.exists()) {
      mdFile.delete()
    }
  }

}

object MusicFile {
  
  val letras = new Letras();
  
  object CommentHelper {
    private val kv = "([-_a-zA-Z0-9]+)=([^; \\s]+);?(.*)".r;

    type Props = Map[String, String]

    def parseProps(comment: String): Props = {
      val c = if (comment != null) comment else ""
      comment match {
        case kv(key, value, tail) => Map(key -> value) ++ parseProps(tail)
        case _ => Map()
      }
    }

    def setExtraProp(c0: String, key: String, value: String): String = {
      val comment = if (c0 != null) c0 else ""
      val props = parseProps(comment)
      val hasKey = props.contains(key)
      if (hasKey) {
        val replacement = if (value != null) key + "=" + value else ""
        val old = key + "=" + props.getOrElse(key, "")
        return comment.replace(old, replacement)  
      } else if (value != null) {
        // Append
        return comment.trim() + ";" + key + "=" + value
      } else {
        return c0
      }
    }
  }

  def fileToMdFile(file: File) = {
    val fullname = file.getAbsolutePath();
    val index = fullname.lastIndexOf(".");
    val truncated = if (index >= 0) fullname.substring(0, index) else fullname
    new File(truncated + ".md");
  }

}
