package au.com.thoughtpatterns.djs.lib

import au.com.thoughtpatterns.djs.util.RecordingDate

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
  composer: String = null
  ) extends Serializable {
  
  def toPerformance = Performance(title, artist, genre, year)
  def toApproxPerformance = Performance(title, artist, genre, if (year != null) year.approx else null)
  
}