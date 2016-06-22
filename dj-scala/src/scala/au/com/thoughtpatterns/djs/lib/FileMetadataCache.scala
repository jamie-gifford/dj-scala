package au.com.thoughtpatterns.djs.lib

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import au.com.thoughtpatterns.djs.tag.TagFactory
import au.com.thoughtpatterns.djs.util.Log
import au.com.thoughtpatterns.djs.util.ProcessExec
import java.io.InputStream
import au.com.thoughtpatterns.dj.DJBeat

object FileMetadataCache {

  val cacheFile = new File(".file-metadata");

  @SerialVersionUID(2L)
  case class FileMetadata(
    endSilenceSeconds : Option[Double],
    bpm : Option[Double]
  ) extends Serializable
  
  @SerialVersionUID(1L)
  case class CachedMetadata(m: FileMetadata, ts: Long) extends Serializable

  var cache: Map[File, CachedMetadata] = read()

  var dirty: Int = 0

  private def read() = {
    try {
      val i = new ObjectInputStream(new FileInputStream(cacheFile))
      val o = i.readObject()
      o match {
        case x: Map[File, CachedMetadata] @unchecked => x
        case _ => Map[File, CachedMetadata]()
      }
    } catch {
      case _: Exception => Map[File, CachedMetadata]()
    }
  }

  def get(f: File): Option[FileMetadata] = {
    if (f.exists()) {
      cache.get(f) match {
        case Some(md) if (md.ts == f.lastModified()) => Some(md.m)
        case _ => {
          val md = read(f);
          cache = cache + (f -> md)
          dirty += 1
          checkpoint()
          Some(md.m)
        }
      }
    } else {
      None
    }
  }

  private def read(file: File) = {
    val ts = file.lastModified();
    val silence = measureSilence(file);
    val bpm = None;
    CachedMetadata(FileMetadata(silence, bpm), ts)
  }

  private def checkpoint() {
    if (dirty > 50) write()
  }

  def write() = {
    if (dirty > 0) {
      val o = new ObjectOutputStream(new FileOutputStream(cacheFile))
      o.writeObject(cache)
      o.close()
      dirty = 0
    }
  }
  
  private def measureSilence(file: File) : Option[Double] = {
    
    val tmpFile = File.createTempFile("dj-tmp-", ".flac");
    tmpFile.deleteOnExit();
    
    // sox 12_-_Catamarca.flac tmp.flac reverse trim 0 10 silence 1 0.2 5% reverse 
    
    val cmd = List(
        "sox", 
        file.getAbsolutePath(), 
        tmpFile.getAbsolutePath(), 
        "reverse", 
        "trim", 
        "0", 
        "10", 
        "silence",
        "1",
        "0.2",
        "5%",
        "reverse")
        
    if (ProcessExec.exec(cmd).resultCode == 0) {

      val cmd2 = List(
        "soxi",
        "-D",
        tmpFile.getAbsolutePath()
      )
      
      val d = ProcessExec.exec(cmd2)
      if (d.resultCode == 0) {
        
        Some(10 - d.stdout.toDouble)
      
      } else {
        None
      }

    } else {
      None
    }
  }
    
}
