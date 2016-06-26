package au.com.thoughtpatterns.djs

import au.com.thoughtpatterns.djs.lib.ReplicationStrategy
import java.io.File
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.util.Log

object Replicate_Test {

  def main(args: Array[String]) : Unit = {
    
    val from = "/media/james/Orange/Music"
    val to = "/home/djs/replica-dj"
    val l = Library.load(new java.io.File("jamie.djs"))
    
    l.m.replDJ(from, to)
    
  }
  
}