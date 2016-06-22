package au.com.thoughtpatterns.djs.util

import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import au.com.thoughtpatterns.djs.tag.TagFactory
import java.io.FileInputStream
import java.io.FileOutputStream

@SerialVersionUID(1L)
abstract class FileMetaCache[T](cacheFile: File) extends Serializable {

  private var cache : Map[File, Container[T]] = read()
  
  var dirty: Int = 0
  
  private def read() = {
    try {
      val i = new ObjectInputStream(new FileInputStream(cacheFile))
      val o = i.readObject()
      o match {
        case x: Map[File, Container[T]] @unchecked => x
        case _ => Map[File, Container[T]]()
      }
    } catch {
      case x: Exception => Map[File, Container[T]]()
    }
  }

  def get(f: File): Option[T] = {
    if (f.exists()) {
      cache.get(f) match {
        case Some(md) if (md.ts == f.lastModified()) => Some(md.data)
        case _ => {
          val md = read(f);
          cache = cache + (f -> md)
          dirty += 1
          checkpoint()
          Some(md.data)
        }
      }
    } else {
      None
    }
  }
  
  def dirty(f: File) : Unit = {
    cache = cache - f
  }

  private def read(file: File) = {
    val ts = file.lastModified();
    val data = readData(file)

    new Container(data, ts);
  }

  def readData(file: File) : T 
  
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
  
  def keys = cache.keys
  
}

@SerialVersionUID(1L)
case class Container[T](data: T, ts: Long) extends Serializable
  
