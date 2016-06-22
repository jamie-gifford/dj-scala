package au.com.thoughtpatterns.djs.lib

import au.com.thoughtpatterns.djs.util.RecordingDate

trait Formatter {

  type Printable = {
    def title : String
    def artist : String
    def genre : String
    def year : RecordingDate
  }
  
  def format(data: Printable) = {
    "%40.40s %60.60s %12.12s %10.10s".format(data.title, data.artist, if (data.year != null) data.year.toString() else "-", data.genre)
  }
  
}