package au.com.thoughtpatterns.djs.util

object RecordingDate_Test {

    def main(args: Array[String]) = {

      val d = RecordingDate.parse("1943--1944")
      
      println("Date is " + d)
      
    }
  
}