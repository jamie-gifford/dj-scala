package au.com.thoughtpatterns.djs.lib

import java.io.File
import au.com.thoughtpatterns.core.util.CsvUtils
import java.io.PrintWriter
import scala.util.Sorting

trait ManagedPerformances extends Iterable[Performance] with Formatter {

  def lib: Library

  private type T = ManagedPerformances

  private def make(iter: Iterable[Performance]) = ManagedPerformances.apply(lib, iter)

  // Transformations

  def filtre(f: Performance => Boolean): T = {
    val x = for (
      t <- iterator;
      if (try { f(t) } catch { case e: Exception => false })
    ) yield { t }
    make(x.toList)
  }

  // Convenient special cases of filtre
  def artist(str: String) = filtre(_.artist.contains(str))
  
  def years(from: Int, to: Int) = filtre(p => p.year != null && p.year.inRange(from, to))
  
  def genre(str: String) = filtre(_.genre == str)
  
  def chunk(from: Int, size: Int) = make(slice(from, from + size))

  /**
   * Set difference
   */
  def \(p: ManagedPerformances) = {
    val s = p.toSet
    make(for (p <- this; if !s.contains(p)) yield p)
  }

  // -----------------------------
  // Sorting
  
  def srt(lt : (Performance, Performance) => Boolean) = {
    val sorted = Sorting.stableSort(this.toSeq, lt)
    make(sorted)
  }
  
  def byTitle = srt( (x,y) => x.title < y.title )
  def byArtist = srt( (x,y) => x.artist < y.artist )
  def byYear = srt( (x,y) => 
    if (x.year != null) {
      if (y.year != null) {
        x.year < y.year
      } else {
        true
      }
    } else {
      false
    })


  // -----------------------
  // Output

  def toCsv(f: File) {
    val utils = new CsvUtils

    def format(p: Performance): Array[String] =
      List(p.genre,
        p.artist,
        p.title,
        if (p.year != null) p.year.toString else null).toArray

    val lines = (for (p <- this) yield format(p)).toArray

    utils.toCsv(lines)
    val formatted = utils.getFormattedString()

    val pr = new PrintWriter(f)
    try { pr.print(utils.getFormattedString()) } finally { pr.close() }
  }
  
  def toCsv(name: String) { toCsv(new File(name)) }

  def print {
    println((for (p <- this) yield {
      format1(p)
    }).mkString("\n"))
  }

}

object ManagedPerformances {
  def apply(lib0: Library, i: Iterable[Performance]) = {
    new ManagedPerformances() {
      def iterator = i.iterator
      def lib = lib0
    }
  }
}
