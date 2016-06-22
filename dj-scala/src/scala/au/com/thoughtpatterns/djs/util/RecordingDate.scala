package au.com.thoughtpatterns.djs.util

@SerialVersionUID(1L)
class RecordingDate(val from: ApproxDate, val to: ApproxDate) extends Serializable {
  override def toString = {
    if (to == null)
      from.toString
    else
      from.toString + "--" + to.toString
  }

  def <(other: RecordingDate): Boolean = {
    // TODO improve this
    from.year < other.from.year
  }

  /**
   * Year range (years inclusive)
   */
  def inRange(start: Int, end: Int): Boolean = {
    from.year >= start &&
      (if (to != null) to.year else from.year) <= end
  }

  /**
   * @return true if this date is consistent with, but more accurate than, the other date
   */
  def refines(other: RecordingDate): Boolean = {
    if (other.to != null) {
      if (this.to != null) {
        return this.from.year >= other.from.year && this.to.year <= other.to.year
      } else {
        return this.from.year >= other.from.year && this.from.year <= other.to.year
      }
    } else if (this.from.year != other.from.year) {
      return false
    } else if (other.from.month == 0) {
      return true
    } else {
      return this == other
    }
  }

  def approx0 = if (to == null) new RecordingDate(ApproxDate(0, 0, from.year), null) else null 
  def approx = if (to == null) new RecordingDate(ApproxDate(0, 0, from.year), null) else new RecordingDate(ApproxDate(0, 0, from.year), ApproxDate(0, 0, to.year)) 

  override def hashCode = from.hashCode

  override def equals(other: Any) = {
    other match {
      case o: RecordingDate => from == o.from && to == o.to
      case _ => false
    }
  }

}

@SerialVersionUID(1L)
case class ApproxDate(day: Int, month: Int, year: Int) extends Serializable {
  override def toString = {
    if (day > 0)
      "%4d-%02d-%02d".format(year, month, day)
    else
      "%4d".format(year)
  }
}

object RecordingDate {

  val yyyy = "([0-9]{4})".r
  val yyyy_mm_dd = "([0-9]{4})-([0-9]+)-([0-9]+)".r
  val yyyy_yyyy = "([0-9]{4})--([0-9]{4})".r

  def parse(in: String): RecordingDate = in match {
    case null => null
    case yyyy(y) => new RecordingDate(ApproxDate(0, 0, y.toInt), null)
    case yyyy_mm_dd(y, m, d) => new RecordingDate(ApproxDate(d.toInt, m.toInt, y.toInt), null)
    case yyyy_yyyy(from, to) => new RecordingDate(ApproxDate(0, 0, from.toInt), ApproxDate(0, 0, to.toInt))
    case _ => null
  }

  def year(y: Int): RecordingDate = new RecordingDate(ApproxDate(0, 0, y), null)

}