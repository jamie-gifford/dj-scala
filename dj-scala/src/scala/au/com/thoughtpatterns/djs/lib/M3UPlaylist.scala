package au.com.thoughtpatterns.djs.lib

import scala.io.Source
import scala.collection.mutable.ListBuffer
import java.nio.file.InvalidPathException
import javax.sound.midi.Track
import java.nio.file.Paths
import java.nio.file.Path
import java.io.File
import java.io.PrintWriter
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream
import au.com.thoughtpatterns.djs.lib.MusicFormat._

@SerialVersionUID(1L)
class M3UPlaylist(file0: File) extends PlaylistFile(file0) {

  def read = load

  def load() {
    if (!file.exists()) {
      tracks = Nil
      return
    }

    val lines = for (line <- Source.fromFile(file).getLines()) yield { line.trim }

    var absolute = false

    var b = new ListBuffer[MusicFile]()

    val basePath = file.getParentFile().toPath();

    for (
      line <- lines;
      if (!(line == "" || line.startsWith("#")))
    ) {
      var p = Paths.get(line);
      if (p.isAbsolute()) {
        absolute = true;
      } else {
        p = basePath.resolve(p);
      }

      val f = p.toFile();
      val m = new MusicFile(f);
      b.append(m)
    }

    relative = !absolute
    tracks = b.toList
  }

  def saveToString = {
    val base = file.getParentFile().toPath();
    def f(file: File): String = {
      val pf = try { file.toPath().toRealPath() } catch { case _: Exception => file.toPath() }
      if (relative)
        base.relativize(pf).toString()
      else
        pf.toString()
    }

    formatToString(f, "\n")
  }

  private def formatToString(f: File => String, separator: String) = {
    val list = for (t <- tracks) yield {
      f(t.file)
    }
    list.mkString(separator)
  }

  /*
  override def replicate(destFile: File, format: MusicFormat) {
    destFile.getParentFile().mkdirs()

    val suffix = format match {
      case OggVorbis => ".ogg"
      case MP3 => ".mp3"
      case _ => throw new IllegalArgumentException("Unknown format " + format)
    }

    val base = file.getParentFile().toPath();
    def f(file: File): String = {
      val pf = file.toPath().toRealPath()
      base.relativize(pf).toString().replaceAll("\\.flac$", suffix)
    }

    val contents = formatToString(f)

    val wo = new OutputStreamWriter(new FileOutputStream(destFile), "utf8");

    val writer = new PrintWriter(new BufferedWriter(wo));
    try {
      writer.print(contents);
    } finally {
      writer.close();
    }
    destFile.setLastModified(file.lastModified)
  }
  */

  override def replicate(strategy: ReplicationStrategy) {

    val src = file
    
    for (target <- strategy.dirtyTarget(src)) {

      val targetFile = target.file
      targetFile.getParentFile().mkdirs()

      val strategy = target.strategy

      def formatter(content: File): String = {
        val transcodedContent = strategy.target(content) match {
          case Some(ReplicationStrategy.Target(f, s)) => f
          case _ => content
        }
        
        val transcodedPath = transcodedContent.toPath()
        val rel = targetFile.getParentFile().toPath().relativize(transcodedPath)
        rel.toString()
      }

      val contents = formatToString(formatter, "\n")

      val wo = new OutputStreamWriter(new FileOutputStream(targetFile), "utf8");

      val writer = new PrintWriter(new BufferedWriter(wo));
      try {
        writer.print(contents);
      } finally {
        writer.close();
      }
      targetFile.setLastModified(file.lastModified)

      def quote(s: String) = if (s != null) s.replaceAllLiterally("\"", "\\\"") else ""
      
      def jformatter(content: File): String = {
        val transcodedContent = strategy.target(content) match {
          case Some(ReplicationStrategy.Target(f, s)) => f
          case _ => content
        }
        
        val rel = strategy.relativize(transcodedContent)
        rel.toString()
      }
        
      def quoter(content: File) : String = "\"" + quote(jformatter(content)) + "\""
      
      val qf = quote(strategy.relativize(targetFile).toString)
      val content= formatToString(quoter, ",\n")
      
      val json = " { \"file\": \"" + qf + "\", \n\"content\": [ " + content + " ] } "; 
      strategy.json.put(src, json)
      
    }
  }

}
