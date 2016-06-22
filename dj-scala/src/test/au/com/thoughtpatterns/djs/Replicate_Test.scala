package au.com.thoughtpatterns.djs

import au.com.thoughtpatterns.djs.lib.ReplicationStrategy
import java.io.File
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.util.Log

object Replicate_Test {

  def main(args: Array[String]) : Unit = {
    
    val from = "/media/Orange/Music"
    val to = "/media/Orange/replica"
    val x = new ReplicationStrategy.Ogg(new File(from).toPath(), new File(to).toPath())
    val l = Library.load(new java.io.File("jamie.djs"))

    
    val tandas = l.p.path("Jamie")
    
    val td = tandas
    
    Log.info("Replicate " + td.print)
    Log.info("Replicate " + td.size + " items")
    
    td.repl(from, to)
    
  }
  
}