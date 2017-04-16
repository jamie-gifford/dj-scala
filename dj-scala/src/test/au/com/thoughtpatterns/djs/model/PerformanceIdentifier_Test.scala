package au.com.thoughtpatterns.djs.model

import java.io.File
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.clementine.Player
import au.com.thoughtpatterns.djs.util.Log

object PerformanceIdentifier_Test {

  def main(args: Array[String]) {

    val l = Library.load(new File("/home/djs/tmp/sound.djs"))
    val player = new Player(l)

    val qq = player.qq
    
    qq.identify(Some("Racciatti"));

    /*
    for (m <- qq) {
      m.read()
      Log.info(m.toString)
    }
    
    for (m <- qq; md <- m.md) {

      val perf = md.toApproxPerformance
      val id = new PerformanceIdentifier(Some("Racciatti"));
      val out = id.identify(perf).take(5);

      
      
      Log.info("-----------------------------");
      Log.info("Matches for: " + m.file + "....");
      for (p <- out) {
        Log.info(p.toString())
      }

    }
    */
    

  }

}