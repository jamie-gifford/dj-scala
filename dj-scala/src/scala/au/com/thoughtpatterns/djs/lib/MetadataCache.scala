package au.com.thoughtpatterns.djs.lib

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import au.com.thoughtpatterns.djs.tag.TagFactory
import au.com.thoughtpatterns.djs.util.Log

object MetadataCache {

  val cacheFile = new File(".metadata");

  @SerialVersionUID(1L)
  case class CachedMetadata(m: Metadata, ts: Long) extends Serializable

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
      case ex: Exception => {
        Log.error(ex)
        Map[File, CachedMetadata]()  
      }
    }
  }

  def get(f: File): Option[Metadata] = {
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
    val tag = new TagFactory().getTag(file);

    CachedMetadata(Metadata(
      tag.getTitle(),
      tag.getArtist(),
      tag.getAlbum(),
      tag.getYear(),
      tag.getComment(),
      tag.getGenre(),
      tag.getTrack(),
      tag.getRating() match { case x if (x > 0) => Some(x) case _ => None },
      tag.getBPM() match { case x if x != null => Some(x) case _ => None}), ts);
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
  
  def gc() : Unit = {
    Log.info("Before gc have " + cache.keySet.size + " cache entries")
    var cacheCopy = Map[File, CachedMetadata]()
    for (f <- cache.keys; c <- cache.get(f)) {
      if (c.m.title != null) {
        cacheCopy = cacheCopy + (f -> c)  
      } else {
        dirty += 1
        Log.info("Deleting " + f + " from metadata cache")
      }
    }
    cache = cacheCopy
    Log.info("After gc have " + cache.keySet.size + " cache entries")
    write()
  }
}
