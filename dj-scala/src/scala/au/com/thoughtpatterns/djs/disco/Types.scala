package au.com.thoughtpatterns.djs.disco

@SerialVersionUID(1L)
object Types extends Serializable {

  case class TiAlbumSide(tin: String, side: Integer)
  case class TiArtist(orchestra: String, vocalist: String) {
    override def toString : String = {
      if (vocalist == null || vocalist == "-" || vocalist == "0") 
        orchestra
      else 
        orchestra + ", voc. " + vocalist    
    }
  }
  case class TINT(tin: String, side: Integer, track: Integer)

  case class TdjAlbum(name: String)
  case class TdjArtist(name: String)
  case class TdjTrack(albumName: String, track: Integer)

  case class GordonAlbum(name: String)
  case class GordonArtist(name: String)
  case class GordonTrack(albumName: String, track: Integer)

  case class GordonMp3Album(name: String)
  case class GordonMp3Artist(name: String)
  case class GordonMp3Track(albumName: String, track: Integer)

  
  def parseTINT(str: String) = {
    val tintPattern = """(\d*)-(\d+)-(\d+)""".r
    val tintPattern(tin, side, track) = str
    tin match {
      case "" => None
      case _ => Some(TINT(tin, Integer.parseInt(side), Integer.parseInt(track)))
    }
  }

  private val YYYY = "([0-9]{4})?.*".r
  
  private val spanish = java.text.Collator.getInstance(java.util.Locale.forLanguageTag("es"));
  spanish.setStrength(java.text.Collator.PRIMARY)

  @SerialVersionUID(1L)
  class SpanishWord(word: String) extends Serializable {

    val key = spanish.getCollationKey(word).toByteArray().toList

    override val hashCode = key.foldLeft(0)((a, b) => (a * 257 + b) % 65537)

    override def equals(other: Any) = other match {
      case w: SpanishWord => key == w.key
      case _ => false
    }

    override def toString = word
  }

   
}