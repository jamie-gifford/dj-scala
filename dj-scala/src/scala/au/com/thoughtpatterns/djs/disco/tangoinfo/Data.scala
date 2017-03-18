package au.com.thoughtpatterns.djs.disco.tangoinfo

import au.com.thoughtpatterns.core.util.CsvUtils
import java.io.FileReader
import java.io.InputStreamReader
import java.io.File
import scala.collection.JavaConversions._
import au.com.thoughtpatterns.djs.disco.Types.SpanishWord

object Data {
  
  private def reader(resource: String) = new InputStreamReader(getClass.getResourceAsStream(resource))
  
  private val u1 = new CsvUtils();
  private val works = u1.fromCsv(reader("tangoworks.csv")).toSeq.map(toWork _);
  
  private val u2 = new CsvUtils();
  private val performances = u2.fromCsv(reader("tangoperformances.csv")).toSeq.map(toPerformance _);

  private val workMap = works.groupBy { x => x.title }
  
  private val worksByTiwc = works.groupBy { x => x.tiwc }

  private val performancesByTiwc = performances.groupBy { x => x.tiwc }
  
  private val perfIndex = performances.groupBy { x => x.toIndex }
  
  private def esp(x: String) = new SpanishWord(x)
  
  def composer(title: String, artist: String, genre: String) : Option[String] = {
    val orq = artist.replaceAll(", voc..*", "");
    val instrumental = orq == artist;
    
    val title0 = title.replaceAll("\\|.*", "");
    
    val idx = new PerfIndex(esp(title0), esp(orq))
    
    val candidates = for (
        x <- perfIndex.get(idx).toSeq; 
        p <- x; 
        tiwc = p.tiwc; 
        works <- worksByTiwc.get(tiwc).toSeq; 
        work <- works
        ) yield work
        
    def extract(work: Work) = 
      if (instrumental) {
        work.composer
      } else {
        work.composer + ", let. " + work.lyricist
      }
    
    val names = candidates map { extract(_) } toSet
    
    if (names.size == 1) {
      return Some(names.iterator.next().toString())
    } else {
      return None
    }
    
  }
  
  def main(args: Array[String]) {
    
    println(composer("Tango triste | Mi tango triste", "Aníbal Troilo, voc. Alberto Marino", "tango"))
    
  }
  
  case class Work (title: SpanishWord, genre: String, tiwc: String, composer: SpanishWord, lyricist: SpanishWord)
  
  private def toWork(x: Array[String]) = new Work(esp(x(0)), x(1), x(2), esp(x(3)), esp(x(4)))

  //case class Performance (title: String, tiwc: String, genre: String, orchestra: String, vocalist: String, date: String, duration: String) {
  case class Performance (title: SpanishWord, tiwc: String, genre: String, orchestra: SpanishWord, vocalist: String, date: String, duration: String) {
    
    def toIndex = new PerfIndex(title, orchestra)
    
  }
  
  private def toPerformance(x: Array[String]) = new Performance(esp(x(0)), x(1), x(2), esp(x(3)), x(4), x(5), x(6))
  
  case class PerfIndex ( title: SpanishWord, orchestra: SpanishWord )
  
}