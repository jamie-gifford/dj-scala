package au.com.thoughtpatterns.djs.lib

import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.tag.TagFactory
import au.com.thoughtpatterns.djs.disco.Types.SpanishWord2

/**
 * For performances by modern orchestras which are not in the discography yet.
 * If the discography has an exact, unique match on the title, then assume that
 * the title and genre are correct, and import those tags. 
 */
class NameGuesser(music: ManagedMusic) {

  case class Title(title: SpanishWord2, genre: String)
  
  // Discography
  val sperformances = Disco.TangoInfo.performances
  
  val tvm = Set("tango", "vals", "milonga" );
  
  // Library-compatible performances
  val perf = sperformances map 
    { _.perf.toLibPerformance } filter
    { p => tvm.contains(p.genre) } map
    { p => Title(new SpanishWord2(p.title), p.genre) }
  
  // By title
  val inverse = perf groupBy { p => p.title};
  
  // By Title
  val inverse2 = perf groupBy { p => Title(p.title, p.genre) };
    
  def convert(w : String, genre: String) : Option[Title] = {
    val sp = new SpanishWord2(w);
    val candidates = inverse.getOrElse(sp, Set());
    val candidates2 = inverse2.getOrElse(Title(sp, genre), Set());
    
    println(sp + " against " + inverse.keys.size + " gives " + candidates.size + " candidates");
    println(sp + " against " + inverse.keys.size + " gives " + candidates2.size + " candidates2");

    for (c <- candidates) {
      println("-- 1 - " + c)
    }
    for (c <- candidates2) {
      println("-- 2 - " + c)
    }
    
    if (candidates2.size == 1) {
      return candidates2.headOption
    }
    
    if (candidates.size == 1) {
      return candidates.headOption
    } else {
      return None
    }
  }
  
  val find = (for (m <- music; 
         p = m.toApproxPerformance;
         w <- convert(p.title, p.genre)) 
    yield ( m -> w )).toSeq.toMap;

  def preview : ManagedMusic = {
    var i = 0;
    for (x <- find.keys; md <- x.md; title <- find.get(x)) {
      i = i + 1
      println((i) + ": " + md.title + " => " + title + " (" + md.artist + ")")
    }
    music.filtre { x => find.keySet.contains(x) }
  }

  def rename : ManagedMusic = {
    for (x <- find; md <- x._1.md) {
      fix(x._1, x._2)
    }
    music.filtre { x => find.keySet.contains(x) }
  }
  
  private def fix(m: MusicFile, perf: Title) = {
    if (m.file.exists) {
      m.md match {
        case Some(md) => {
          val tag = new TagFactory().getTag(m.file)
          tag.setTitle(perf.title.toString())
          tag.setGenre(perf.genre)
          tag.write()
          m.update()
        }
        case None => {}
      }
    }

    this
  }
  
}