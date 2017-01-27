package au.com.thoughtpatterns.djs.lib

import au.com.thoughtpatterns.djs.util.RecordingDate
import au.com.thoughtpatterns.djs.disco.Types.SpanishWord

case class SpanishPerformance(title: SpanishWord, artist: SpanishWord, genre: String, year: RecordingDate)
  