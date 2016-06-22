package au.com.thoughtpatterns.djs.disco

import scala.collection.JavaConversions._
import java.io.FileReader
import au.com.thoughtpatterns.core.util.CsvUtils
import au.com.thoughtpatterns.core.util.CsvUtils

object CollectionsUtil {

  /**
   * "Commutes" List[(A,B)] => Map[A, List[B]]
   */
  def group[A,B](x: List[(A,B)]) : Map[A, List[B]] = {

    def strip(y : List[(A,B)]) : List[B] = {
      y map { _ match { case (a,b) => b } }
    }
    
    val y = x groupBy( _._1 ) 
    y map { z => (z._1, strip(z._2)) }
  }
  
  /**
   * Invert a map Map[A,B] to Map[B,Set[A]]
   */
  def invert[A,B](x : Map[A, B]) : Map[B, Set[A]] = {
    x groupBy { _._2 } map { case(a, b) => (a, b.unzip._1.toSet) }
  }
 
  def readCsv[U, V](filename: String, parser: (Array[String] => (U, V))): Map[U, V] = {
    val rows = (new CsvUtils()).fromCsv(new FileReader(filename))
    (for { row <- rows.toList } yield { parser(row) }).toMap
  }

}