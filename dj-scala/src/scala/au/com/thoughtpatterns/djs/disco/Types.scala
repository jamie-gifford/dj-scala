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
  
  def parseArtist(str: String) : TiArtist = {
    if (str == null) {
      TiArtist(null, null)
    } else {
      val bits = str.split(", voc.");
      if (bits.length == 1) {
        TiArtist(str, null)
      } else {
        TiArtist(bits(0), bits(1))
      }
    }
  }

  private val YYYY = "([0-9]{4})?.*".r
  
  private val spanish = java.text.Collator.getInstance(java.util.Locale.forLanguageTag("es"));
  spanish.setStrength(java.text.Collator.PRIMARY)

  @SerialVersionUID(1L)
  class SpanishWord(val word: String) extends Serializable {

    val key = spanish.getCollationKey(if (word == null) { "" } else { word } ).toByteArray().toList

    override val hashCode = key.foldLeft(0)((a, b) => (a * 257 + b) % 65537)

    override def equals(other: Any) = other match {
      case w: SpanishWord => key == w.key
      case _ => false
    }

    override def toString = word
  }

  /**
   * Like SpanishWord except "ñ", "n" are identified
   */
  @SerialVersionUID(1L)
  class SpanishWord2(val word0: String) extends Serializable {

    val word = word0.replaceAll("ñ", "n").replaceAll("Ñ", "N")
    
    val key = spanish.getCollationKey(if (word == null) { "" } else { word } ).toByteArray().toList

    override val hashCode = key.foldLeft(0)((a, b) => (a * 257 + b) % 65537)

    override def equals(other: Any) = other match {
      case w: SpanishWord2 => key == w.key
      case _ => false
    }

    override def toString = word0
  }
   
}