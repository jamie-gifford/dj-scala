package au.com.thoughtpatterns.djs.lib

import au.com.thoughtpatterns.djs.util.RecordingDate
import au.com.thoughtpatterns.djs.disco.Types.SpanishWord

case class Performance(title: String, artist: String, genre: String, year: RecordingDate) {
  
  def toSpanishPerformance = SpanishPerformance(new SpanishWord(title), new SpanishWord(artist), genre, year)
 
  def toApproxPerformance = Performance(title, artist, genre, if (year != null) year.approx else null)
  
}

