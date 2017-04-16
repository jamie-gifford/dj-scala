package au.com.thoughtpatterns.djs.lib

import java.io.File
import java.io.PrintWriter
import scala.Option.option2Iterable
import au.com.thoughtpatterns.core.util.CsvUtils
import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.disco.Types._
import au.com.thoughtpatterns.djs.util.RecordingDate
import au.com.thoughtpatterns.djs.disco.Types.SpanishWord
import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.clementine.Player
import java.util.Date
import au.com.thoughtpatterns.djs.clementine.Clementine

object Library_Test {

  def main(args: Array[String]) {

     val l = Library.load(new File("/home/djs/tmp/sound.djs"))

     //val clem = new Clementine();
     
     //val tracks = clem.getTracks();
     
     //println(tracks);
     
    
      //val l = Library.load(new File("/media/djs/Orange-mirror/Music/ogg.djs"))

      //l.add(new File("/media/djs/Orange-mirror/Music/ogg"))
      
     /*
     val tvm = Set("tango", "vals", "milonga")
     
     val interesting = l.m.require(x => x.comment == null).require(x => x != null && tvm.contains(x.genre)).path("Jerry")
 
     val old = 1l;
     val now = new Date().getTime

     println("Got " + interesting.size + " to process")
     
     for (m <- interesting) {
       println(m.file)
       m.file.setLastModified(now)
     }
      
     */
     
//     val q = new Player(l)
     
//     q.q.synchronise(1)
     
  }
}