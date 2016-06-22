package au.com.thoughtpatterns.djs.disco.gordon

import java.io.File
import au.com.thoughtpatterns.core.util.CsvUtils
import java.io.FileWriter

object OrangeTest {

  def main(args: Array[String]) {
    val root = new File("/media/Orange/Gordon")
    val g = new GordonM4A(root)
    //g.load()

    def findM4A(dir: File): Iterable[File] = {
      val f = dir.listFiles()
      f.filter(_.getName().toLowerCase().endsWith("m4a")) ++ f.filter(_.isDirectory()).flatMap(findM4A(_))
    }

    val files = findM4A(root)

    val data = (for (f <- files; md <- g.cache.get(f)) yield {
      List(
          md.title, 
          md.artist,
          if (md.year != null) md.year.toString else null,
          md.genre, 
          md.album, 
          "" + md.track, 
          f.getAbsoluteFile().toString()
      ).toArray
    }).toArray
    
    val utils = new CsvUtils
    utils.toCsv(data)
    
    val out = new File("gordon.csv")
    val pw = new FileWriter(out)
    pw.write(utils.getFormattedString())
    pw.close()

  }

}