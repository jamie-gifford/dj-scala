package au.com.thoughtpatterns.djs

import au.com.thoughtpatterns.djs.lib.ReplicationStrategy
import java.io.File
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.util.Log

object Replicate_Test {

  def main(args: Array[String]) : Unit = {
    
    val from = "/media/james/Orange/Music"
    val to = "/home/djs/replica-dj/Music"
    val l = Library.load(new java.io.File("orange.djs"))
    
    l.m.replDJ(from, to)
    
  }
  
}