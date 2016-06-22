package au.com.thoughtpatterns.djs.disco

import au.com.thoughtpatterns.djs.disco.gordon.GordonM4A
import au.com.thoughtpatterns.core.util.CsvUtils
import java.io.FileWriter
import java.io.File
import au.com.thoughtpatterns.djs.disco.importer.Importer
import au.com.thoughtpatterns.djs.lib.Library

object ImporterTest {

  def main(args: Array[String]) {
    val root = new File("/media/Orange/Gordon/tmp")
    def accept(file: File) = file.getName().toLowerCase().endsWith(".mp3")
    val lib = new Library(Some(new File("jamie.djs")))
    val g = new Importer(lib, root, accept)
    g.clean()
    g.load()

    val data = (for (f <- g.files; md <- g.metadataCache.get(f)) yield {
      List(
        md.title,
        md.artist,
        if (md.year != null) md.year.toString else null,
        md.genre,
        md.album,
        "" + md.track,
        f.getAbsoluteFile().toString()).toArray
    }).toArray

    val utils = new CsvUtils
    utils.toCsv(data)

    val out = new File("importer.csv")
    val pw = new FileWriter(out)
    pw.write(utils.getFormattedString())
    pw.close()

  }

}