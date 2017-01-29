package au.com.thoughtpatterns.djs.lib

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import au.com.thoughtpatterns.djs.util.ProcessExec
import java.io.File

object SoxiCache {

  val cacheFile = new File(".soxi-metadata");

  @SerialVersionUID(2L)
  case class SoxiMetadata(
      filetype: Option[String], 
      rate: Option[Double],
      channels: Option[Integer],
      samples: Option[Integer],
      bits: Option[Integer],
      encoding: Option[String]
  ) extends Serializable
  
  @SerialVersionUID(1L)
  case class CachedMetadata(m: SoxiMetadata, ts: Long) extends Serializable

  var cache: Map[File, CachedMetadata] = read()

  var dirty: Int = 0
  
  var checkpointSize: Int = 10

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

  def get(f: File): Option[SoxiMetadata] = {
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

    def measure(opt: String) : Option[String] = {
      val cmd2 = List(
        "soxi",
        "-" + opt,
        file.getAbsolutePath()
      )
      
      val d = ProcessExec.exec(cmd2)
      if (d.resultCode == 0) {
        Some(d.stdout)
      } else {
        None
      }
    }

    def toInt(v: Option[String]) : Option[Integer] = v map { _.toDouble.intValue() }
    def toDouble(v: Option[String]) : Option[Double] = v map { _.toDouble }
    
    def i(opt: String) = toInt(measure(opt))
    def d(opt: String) = toDouble(measure(opt))
    def s(opt: String) = measure(opt)
    
    CachedMetadata(SoxiMetadata(s("t"), d("r"), i("c"), i("s"), i("b"), s("e")), ts)
  }
  
  private def checkpoint() {
    if (dirty > checkpointSize) {
      write()
      checkpointSize *= 2
    }
  }

  def write() = {
    if (dirty > 0) {
      val o = new ObjectOutputStream(new FileOutputStream(cacheFile))
      o.writeObject(cache)
      o.close()
      dirty = 0
    }
  }
  
    
}