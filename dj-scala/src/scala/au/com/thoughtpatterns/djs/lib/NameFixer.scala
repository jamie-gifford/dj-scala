package au.com.thoughtpatterns.djs.lib

import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.tag.TagFactory

class NameFixer(music: ManagedMusic) {
  
  val sperformances = Disco.TangoInfo.performances
  
  val performances = sperformances map { _.perf.toLibPerformance.toSpanishPerformance }
  
  val perfMap = (performances map { x => ( x -> x ) }).toMap
  
  val find = (for (m <- music; 
         p = m.toApproxPerformance.toSpanishPerformance;
         p2 = perfMap.get(p);
         q <- p2;
         if q.title.toString() != p.title.toString()
         ) yield ( m -> q )).toSeq.toMap

  def preview : ManagedMusic = {
    var i = 0;
    for (x <- find.keys; md <- x.md; perf <- find.get(x)) {
      i = i + 1
      println((i) + ": " + md.title + " => " + perf.title + " (" + md.artist + ")")
    }
    music.filtre { x => find.keySet.contains(x) }
  }

  def rename : ManagedMusic = {
    for (x <- find; md <- x._1.md) {
      fix(x._1, x._2)
    }
    music.filtre { x => find.keySet.contains(x) }
  }
  
  private def fix(m: MusicFile, p: SpanishPerformance) = {
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