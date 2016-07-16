package au.com.thoughtpatterns.djs.lib

import au.com.thoughtpatterns.djs.util.RecordingDate
import au.com.thoughtpatterns.djs.tag.Lltag
import au.com.thoughtpatterns.djs.util.Log
import java.io.File
import java.nio.file.Path
import java.io.FileWriter
import au.com.thoughtpatterns.core.json.Jsony
import au.com.thoughtpatterns.core.util.Resources
import java.io.FileReader
import java.io.IOException

abstract class ReplicationStrategy(from: Path, to: Path) {

  import ReplicationStrategy.Target

  /**
   * Map src file to JSON representation
   */
  val json = collection.mutable.Map.empty[File, String] 
  
  /**
   * Calculate the image (before transcoding) of the given file according to the given from and to paths
   */
  def image(src: File): Option[File] = {

    val path = src.toPath()

    if (!path.startsWith(from)) {
      return None
    }

    val rel = from.relativize(path)
    val dest = to.resolve(rel)

    val destFile = dest.toFile()

    Some(destFile)
  }

  /**
   * Given a "src" file (eg, /a/tune.flac), return the "target" file
   * corresponding to the replication strategy (eg, /b/tune.ogg).
   */
  def target(src: File): Option[Target] = {
    image(src) map { target(src, _) }
  }

  def dirty(src: File, file: File) = ! file.exists() || Math.abs(file.lastModified() - src.lastModified()) >= 2000;
    
  def metadata(file: File) = MusicFile.fileToMdFile(file)
  
  def dirtyTarget(src: File): Option[Target] = {
    image(src) map { target(src, _) } filter { 
      t => {
          val d = (dirty(src, t.file) || dirty(src, metadata(t.file))) && src.length() > 0
          /* debugging
          if (d) {
            Log.info("Dirty: " + src + " length " + src.length())
            Log.info("src ts =    " + src.lastModified() + " on " + src)
            Log.info("t.file ts = " + t.file.lastModified() + " on " + t.file)
            Log.info("t.file ts = " + metadata(t.file).lastModified() + " on " + metadata(t.file))
          }
          
          */
          d
        }
      }
  }

  protected def target(src: File, dest: File) : Target

  /**
   * Replicate the src file to the dest file. This will involve calculating the target file from the dest
   * file and then transcoding the contents of the src file to the target.
   */
  def transcode(m: MusicFile, src: File, target: Target) {
    if (dirty(src, target.file)) transcodeData(m, src, target)
    transcodeMetadata(m, src, target)
  }  
  
  def transcodeData(m: MusicFile, src: File, target: Target) {

    val targetFile = target.file;
    
    targetFile.getParentFile().mkdirs()

    val cmd =
      if (suffix(targetFile) != suffix(src))
        if (suffix(targetFile).toLowerCase() == "ogg")
//          List("avconv", "-y", "-i", src.getAbsolutePath(), "-c", "libvorbis", "-q", "5", targetFile.getAbsolutePath())
          List("ffmpeg", "-y", "-i", src.getAbsolutePath(), "-acodec", "libvorbis", "-aq", "6", targetFile.getAbsolutePath())
        else 
          List("avconv", "-y", "-i", src.getAbsolutePath(), targetFile.getAbsolutePath())
      else
        List("cp", src.getAbsolutePath(), targetFile.getAbsolutePath())

    Log.info("execute " + cmd.mkString(" "));

    val l = java.util.Arrays.asList(cmd.toArray: _*)
    val b = new ProcessBuilder(l)
    b.inheritIO()

    val p = b.start();
    p.waitFor();

    if (suffix(targetFile).toLowerCase == "mp3" && suffix(src).toLowerCase != "mp3") {
      // Copy tags across since avconv seems to fail
      val srcTags = new Lltag(src)
      val destTags = new Lltag(targetFile)

      destTags.copyFrom(srcTags)
      val date0 = srcTags.getYear()
      val dateApprox = if (date0 != null) RecordingDate.year(date0.from.year) else null
      destTags.setYear(dateApprox)

      destTags.write()
    }

    targetFile.setLastModified(src.lastModified)
  }


  def transcodeMetadata(m: MusicFile, src: File, target: Target) {
    val mdsrc = metadata(src)
    val mddest = metadata(target.file)
    if (mdsrc.exists()) {
      val md = Resources.readString(new FileReader(mdsrc))
      val w = new FileWriter(mddest)
      w.write(md)
      w.close()
      val ts = mdsrc.lastModified()
      mddest.setLastModified(ts)
    }
  }
    
  /**
   * Rename a file with the given extension (eg "ogg"). No dot in the extension should be given.
   */
  protected def rename(dest: File, extension: String) = {
    val fullname = dest.getAbsolutePath();
    val index = fullname.lastIndexOf(".");
    val truncated = if (index >= 0) fullname.substring(0, index) else fullname
    new File(truncated + "." + extension);
  }

  protected def suffix(f: File) = {
    val name = f.getName()
    val index = name.lastIndexOf('.')
    if (index > -1) name.substring(index + 1) else ""
  }

  protected def isLossless(dest: File) = {
    dest.getName().toLowerCase().endsWith(".flac")
  }

  protected def isMusic(f: File) = Set("mp3", "flac", "ogg").contains(suffix(f).toLowerCase)
  
  def relativize(target: File) = to.relativize(target.toPath())
}

object ReplicationStrategy {

  case class Target(file: File, strategy: ReplicationStrategy)

  class Identity(from: Path, to: Path) extends ReplicationStrategy(from, to) {
    def target(src: File, dest: File) = Target(dest, this)
  }

  /**
   * Replication strategy that compresses everything to Ogg
   */
  class Ogg(from: Path, to: Path) extends ReplicationStrategy(from, to) {

    def target(src: File, dest: File) = {
      if (isLossless(dest))
        Target(rename(dest, "ogg"), this)
      else
        Target(dest, this)
    }

  }

  /**
   * Replication strategy that compresses everything to MP3
   */
  class MP3(from: Path, to: Path) extends ReplicationStrategy(from, to) {

    def target(src: File, dest: File) = {
      if (isMusic(dest))
        Target(rename(dest, "mp3"), this)
      else
        Target(dest, this)
    }

  }

  /**
   * Replication strategy that compresses everything to Ogg except stuff marked as noCompress
   */
  class DJ(from: Path, to: Path, noCompress: Set[File]) extends ReplicationStrategy(from, to) {

    def target(src: File, dest: File) = {
      if (isLossless(dest) && !noCompress.contains(src))
        Target(rename(dest, "ogg"), this)
      else
        Target(dest, this)
    }
    
    override def transcode(m: MusicFile, src: File, target: Target) {
      super.transcode(m, src, target);
      if (target.file.getName.endsWith(".flac")) {
        val ogg = rename(target.file, "ogg")
        if (ogg.exists() && target.file.exists()) {
          // Safety check: should be redundant
          if (! ogg.equals(target.file)) {
            ogg.delete()
            Log.info("Deleted " + ogg)
          }
        }
      }
    }
      
  }

}

