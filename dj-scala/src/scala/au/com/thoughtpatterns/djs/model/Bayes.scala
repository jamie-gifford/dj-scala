package au.com.thoughtpatterns.djs.model

@SerialVersionUID(1L)
class Bayes[T](universe: Iterable[Iterable[T]]) extends Serializable {

  val u : Double = universe.size
  
  private lazy val ratios = {
    val words = (for (sentence <- universe.toList; word <- sentence) yield word)
    val wordGroups = words.toList.groupBy(x => x)
    val wordCount = (for ((w, list) <- wordGroups) yield (w, list.size))
    val r = (for ((w, count) <- wordCount) yield ( w, u/count )).toMap
    r
  }

  def ratio(word: T) : Double = {
    val r = ratios.getOrElse(word, 1d)
    r
  }
  
  def likelihood(a: List[T], b: List[T]) : Double = {
    a.toSeq match {
      case Nil => 1d
      case first :: rest => (if (b.contains(first)) ratio(first) else 1d) * likelihood(rest, b)
    }
  }
}
