package au.com.thoughtpatterns.djs.lib

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import au.com.thoughtpatterns.djs.tag.TagFactory
import au.com.thoughtpatterns.djs.util.Log
import au.com.thoughtpatterns.core.json.AJsonyObject
import java.io.FileWriter
import au.com.thoughtpatterns.core.json.JsonyParser
import java.io.FileReader
import au.com.thoughtpatterns.core.json.JsonyObject
import au.com.thoughtpatterns.djs.util.RecordingDate
import java.util.Date

/**
 * An accurate metadata (tag) cache for music files.
 * The primary interface is the {@link get} method, which returns the (optional) metadata for the file.
 * 
 * Tag reading is potentially slow so the cache saves read tag data in an internal cache, either automatically 
 * every now and then, or when {@link write} is explicitly called.
 * 
 * Tag data is read from one of two possible sources: the music file itself, or a like-named "md" file in the same directory.
 * Of these two files, the newest one has priority. In case that the two files don't have the same timestamp, the older one 
 * is updated with the contents of the newer one as a side effect of reading.
 */
object MetadataCache {

  val cacheFile = new File(".metadata2");

  @SerialVersionUID(1L)
  case class CachedMetadata(m: Metadata, ts: Long) extends Serializable

  var cache: Map[File, CachedMetadata] = read()

  var dirty0: Double = 0

  private def read() = {
    if (cacheFile.exists()) {
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
    } else {
      Map[File, CachedMetadata]()  
    }
  }

  /**
   * Accurate, cached metadata get. Accuracy is determined by timestamps.
   */
  def get(f: File): Option[Metadata] = {
    if (f.exists()) {
      cache.get(f) match {
        case Some(md) if (md.ts == lastModified(f)) => Some(md.m)
        case _ => {
          val md = read(f);
          cache = cache + (f -> md)
          checkpoint()
          Some(md.m)
        }
      }
    } else {
      None
    }
  }

  def lastModified(file: File) {
    val ts = file.lastModified();
    val mdFile = MusicFile.fileToMdFile(file)
    val mdTs = mdFile.lastModified
    Math.max(ts, mdTs)    
  }
  
  /**
   * Pre-cache read, direct from filesystem. 
   * Note that there are two possible sources - the file itself or its md equivalent.
   * If these are out of synch, they are synchronized as a side effect of this function.
   */
  private def read(file: File) = {
    
    dirty0 += 0.001
    
    val ts = file.lastModified();
    val mdFile = MusicFile.fileToMdFile(file)
    val mdTs = mdFile.lastModified
    
    var md : Metadata = null
    
    val diff = mdTs - ts
    
    def rg(g: String, p: String) : Option[ReplayGainData] = {
      if (g != null && p != null) {
        Some(ReplayGainData(g, p))
      } else {
        None
      }
    }
    
    if (mdTs >= ts) {
      // MD file is up to date
      
      val r = new FileReader(mdFile);
      
      (new JsonyParser()).parse(r) match {
        case json : JsonyObject => 

          md = Metadata(
            json.getCast("title", classOf[String]),
            json.getCast("artist", classOf[String]),
            json.getCast("album", classOf[String]),
            RecordingDate.parse(json.getCast("date", classOf[String])),
            json.getCast("comment", classOf[String]),
            json.getCast("genre", classOf[String]),
            Option(json.getCast("track", classOf[Long])).getOrElse(0l).intValue(),
            Option(json.getCast("rating", classOf[Double])),
            json.getCast("bpm", classOf[Double]) match { case x if (x > 0) => Some(x) case _ => None },
            rg(json.getCast("rg_gain", classOf[String]), json.getCast("rg_peak", classOf[String])),
            json.getCast("composer", classOf[String]),
            json.getCast("group", classOf[String])
          )

        case _ =>
      }
      
      r.close();
      
    } else {
      val tag = new TagFactory().getTag(file);
      dirty0 += 1
      md = Metadata(
      tag.getTitle(),
      tag.getArtist(),
      tag.getAlbum(),
      tag.getYear(),
      tag.getComment(),
      tag.getGenre(),
      tag.getTrack(),
      tag.getRating() match { case x if (x > 0) => Some(x) case _ => None },
      tag.getBPM() match { case x if x != null => Some(x) case _ => None},
      rg(tag.getRGGain(), tag.getRGPeak()),
      tag.getComposer,
      tag.getGroup()
      )
    }
    
    if (mdTs > ts && md.title != null) {
      // File is out of date so update
      // But refuse to do so if we are missing a title (as a safety net in case lltag has failed)
      
      val tag = new TagFactory().getTag(file);

      // Tag write - to be kept in synch with Metadata fields
      // ----------------------------
      
      //  title: String,
      tag.setTitle(md.title)
      //  artist: String,
      tag.setArtist(md.artist)
      //  album: String,
      tag.setAlbum(md.album)
      //  year: RecordingDate,
      tag.setYear(md.year)
      //  comment: String,
      tag.setComment(md.comment)
      //  genre: String,
      tag.setGenre(md.genre)
      //  track: Int,
      tag.setTrack(md.track)
      //  rating: Option[Double],
      md.rating match { case Some(x) => tag.setRating(x) case _ =>  }
      //  bpm:  Option[Double] = None,
      md.bpm match { case Some(x) => tag.setBPM(x) case _ =>  }
      //  rg: Option[ReplayGainData] = None,
      md.rg match { case Some(x) => { tag.setRGGain(x.gain); tag.setRGPeak(x.peak); } case _ => }
      //  composer: String = null
      tag.setComposer(md.composer)
      //  group: String = null
      tag.setGroup(md.group)

      // ------------------------------
      
      tag.write()
      file.setLastModified(mdTs)
    }
      
    val out = CachedMetadata(md, ts);

    if (! mdFile.exists() || mdTs < ts) {
      Log.info("MD file is out of date: mdTs = " + new Date(mdTs) + "; file ts = " + new Date(ts));
      // Metadata file is out of date so update
      writeMdFile(out, mdFile)
    }

    out
  }
  
  private def checkpoint() {
    if (dirty0 > 50) write()
  }

  def write() = {
    if (dirty0 > 0) {
      val o = new ObjectOutputStream(new FileOutputStream(cacheFile))
      o.writeObject(cache)
      o.close()
      dirty0 = 0
    }
  }
  
  /**
   * JSON write - to be kept in synch with Metadata fields.
   */
  def writeMdFile(cmd: CachedMetadata, mdFile: File): Unit = {
		val json = new AJsonyObject();
		val md = cmd.m

		// -----------------------------------
    //  title: String,
		json.set("title", md.title);

		//  artist: String,
    json.set("artist", md.artist);

    //  album: String,
    json.set("album", md.album);

    //  year: RecordingDate,
		if (md.year != null) {
			json.set("date", md.year.toString()); 
		}

		//  comment: String,
		json.set("comment", md.comment);

    //  genre: String,
		json.set("genre", md.genre);

		//  track: Int 
		
		ScalaBugHelper.setJsony(json, "track", md.track);
		
    //  rating: Option[Double],
		md.rating match {
		  case Some(x) => json.set("rating", x)
		  case _ => 
		}

		//  bpm:  Option[Double] = None,
    md.bpm match {
		  case Some(x) => json.set("bpm", x)
		  case _ => 
		}

    //  rg: Option[ReplayGainData] = None,
    md.rg match {
		  case Some(x) => {
		    json.set("rg_gain", x.gain)
		    json.set("rg_peak", x.peak)
		  }
		  case None =>
		}

    //  composer: String = null
    json.set("composer", md.composer);

		// -----------------------------------

		Log.info("Writing " + mdFile)
		val mdw = new FileWriter(mdFile);
		mdw.write(json.toJson());
		mdw.close();
		mdFile.setLastModified(cmd.ts)
	}
  
  def gc() : Unit = {
    Log.info("Before gc have " + cache.keySet.size + " cache entries")
    var cacheCopy = Map[File, CachedMetadata]()
    for (f <- cache.keys; c <- cache.get(f)) {
      if (f.exists && c.m.title != null) {
        cacheCopy = cacheCopy + (f -> c)  
      } else {
        dirty0 += 1
        Log.info("Deleting " + f + " from metadata cache")
      }
    }
    cache = cacheCopy
    Log.info("After gc have " + cache.keySet.size + " cache entries")
    write()
  }
  
}
