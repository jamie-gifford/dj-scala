package au.com.thoughtpatterns.djs.disco.gordon

import java.io.File
import au.com.thoughtpatterns.djs.util.FileMetaCache
import au.com.thoughtpatterns.djs.lib.Metadata
import scala.sys.process.ProcessIO
import scala.sys.process.Process
import au.com.thoughtpatterns.djs.util.RecordingDate
import au.com.thoughtpatterns.djs.util.Log

@SerialVersionUID(1L)
class GordonM4A(root: File) extends Serializable {

  val cache = new FileMetaCache[Metadata](new File(".gordon-data")) {
    def readData(file: File) = {

      Log.info("Reading data from " + file)
      
      val cmd = List("exiftool", file.getAbsolutePath())
      val (out, err) = exec(cmd)
      
      def find(tag: String) = {
        
        val r = ("^" + tag + " +:.*").r
        
        (for (line <- out; found <- r.findFirstIn(line)) yield {
          // Remove tag and ": " padding
          line.substring(tag.length()).trim().substring(2)
        }).toList match {
          case (head :: tail) => head
          case _ => null
        }
      }
      
      val title = find("Title")
      val artist = find("Artist")
      val album = find("Album")
      val year = RecordingDate.parse(find("Year"))
      val genre = find("Genre")
      
      val tn = find("Track Number")
      
      val track = if (tn != null) " .*".r.replaceFirstIn(tn, "").toInt else 0
      
      Metadata(title, artist, album, year, null, genre, track, None, None)
    }
  }
  
  def findM4A(dir : File) : Iterable[File] = {
    val f = dir.listFiles()
    f.filter(_.getName().toLowerCase().endsWith("m4a")) ++ f.filter(_.isDirectory()).flatMap(findM4A(_))
  } 
  
  def load() {
    val files = findM4A(root)
    Log.info("Got " + files.size + " files")
    for (file <-files) { 
      cache.get(file) match { 
        case Some(md) => println(md.title) 
        case _ => () 
      } 
    }
    cache.write
  }

  private def exec(command: Seq[String]) = {
    val process = Process(command)
    
    var out : List[String] = Nil
    var err : List[String] = Nil
    
    val io = new ProcessIO(
      stdin => (),
      stdout => { out = scala.io.Source.fromInputStream(stdout).getLines.toList },
      stderr => { err = scala.io.Source.fromInputStream(stderr).getLines.toList })
    process.run(io).exitValue
    
    Pair(out, err)
  }

}

