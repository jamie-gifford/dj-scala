package au.com.thoughtpatterns.djs.lib

import au.com.thoughtpatterns.djs.util.RecordingDate

/**
 * If the fields here change, the mdFile json caching code in MetadataCache needs to be updated too.
 */
@SerialVersionUID(1L)
case class Metadata(
  title: String,
  artist: String,
  album: String,
  year: RecordingDate,
  comment: String,
  genre: String,
  track: Int, 
  rating: Option[Double],
  bpm:  Option[Double] = None,
  rg: Option[ReplayGainData] = None,
  composer: String = null,
  group: String = null
  ) extends Serializable {
  
  def toPerformance = Performance(title, artist, genre, year)
  def toApproxPerformance = Performance(title, artist, genre, if (year != null) year.approx else null)
  def orq = artist.replaceAll(" voc\\..*", "");
  
}

case class MusicKey(group0: String) {

  val key = {
    val i = group0.indexOf('/')
    if (i >= 0) group0.substring(0, i) else group0;
  }
  
  val majorMinor = if (key.indexOf('m') == -1) 1 else -1;
  
  val major = majorMinor == 1
  
  val tonality = if (major) key else key.substring(0, key.indexOf('m'))
  
  def num() = {
    
    // F♯/G♭
    
    tonality match {
      case "C♭" => 11
      case "C" => 0
      case "C♯" => 1
      case "D♭" => 1
      case "D" => 2
      case "D♯" => 3
      case "E♭" => 3
      case "E" => 4
      case "F" => 5
      case "F♯" => 6
      case "G♭" => 6
      case "G" => 7
      case "G♯" => 8
      case "A♭" => 8
      case "A" => 9
      case "A♯" => 10
      case "B♭" => 10
      case "B" => 11
    }
    
  }

  /*

0 C
1 C♯
2 D
3 D♯
4 E
5 F
6 F♯
7 G
8 G♯
9 A
10 A♯
11 B

interval x = 

   */

  
  val adjustedNum = if (major) num else num + 3;
  
  def fifths(key: MusicKey) : Integer = {

    def norm(x: Integer) : Integer = {
      val z = (x + 12 * 100) % 12;
      return if (z > 6) z - 12 else z;  
    }
    
    val interval = (adjustedNum - key.adjustedNum)

    val fifths = norm(interval * 5);    
    
    //System.out.println("fifths from " + this + " to " + key + " : interval=" + interval + "; fifths=" + fifths)
    
    return fifths;
  }

  def fifths2(key: MusicKey) : Double = {
    val eps = if (majorMinor * key.majorMinor == -1) 0.5 else 0
    return fifths(key) + eps;   
  }
  
  def distance(key: MusicKey) : Double = {
    
    val f = fifths(key);    
    
    val eps = if (majorMinor * key.majorMinor == -1) 0.5 else 0
    
    val dist = Math.abs(f) + eps;
    
    //System.out.println("distance from " + this + " to " + key + " ; fifths=" + f + "; eps=" + eps)
    
    return dist;
  }
  
}
