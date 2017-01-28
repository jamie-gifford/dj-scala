package au.com.thoughtpatterns.djs.lib

import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.tag.TagFactory

class NameFixer(music: ManagedMusic) {
  
  val sperformances = Disco.TangoInfo.performances
  
  val performances = sperformances map { _.perf.toLibPerformance.toSpanishPerformance }
  
  val perfMap = (performances map { x => ( x -> x ) }).toMap
  
  def find = {
    
    val result = for (m <- music; 
         p = m.toApproxPerformance.toSpanishPerformance;
         p2 = perfMap.get(p);
         q <- p2;
         if q.title.toString() != p.title.toString()
         ) yield ( m -> q )
        
    var i = 0;
    for (x <- result; md <- x._1.md) {
      i = i + 1
      println((i) + ": " + md.title + " => " + x._2.title + " (" + md.artist + ")")
      //fix(x._1, x._2)
    }
  }

  def fix(m: MusicFile, p: SpanishPerformance) = {
    if (m.file.exists) {
      m.md match {
        case Some(md) => {
          val tag = new TagFactory().getTag(m.file)
          tag.setTitle(p.title.toString())
          tag.write()
          m.update()
        }
        case None => {}
      }
    }

    this
  }

  
  
}