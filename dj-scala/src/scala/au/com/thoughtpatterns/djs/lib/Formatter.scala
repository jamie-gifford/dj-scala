package au.com.thoughtpatterns.djs.lib

import au.com.thoughtpatterns.djs.util.RecordingDate

trait Formatter {

  type Printable1 = {
    def title : String
    def artist : String
    def genre : String
    def year : RecordingDate
  }
  type Printable2 = {
    def title : String
    def artist : String
    def genre : String
    def year : RecordingDate
    def tuning: Option[Double]
  }
  
  def format1(data: Printable1) = {
    "%40.40s %60.60s %12.12s %10.10s".format(
        data.title, 
        data.artist, 
        if (data.year != null) data.year.toString() else "-", 
        data.genre
    )
  }
  def format2(data: Printable2) = {
    "%40.40s %60.60s %12.12s %10.10s %8.8s".format(
        data.title, 
        data.artist, 
        if (data.year != null) data.year.toString() else "-", 
        data.genre, 
        if (data.tuning != null && data.tuning.isDefined) data.tuning.get.toString() + " c" else "-"
    )
  }
  
}