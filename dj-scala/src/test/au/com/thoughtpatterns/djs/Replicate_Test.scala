package au.com.thoughtpatterns.djs

import au.com.thoughtpatterns.djs.lib.ReplicationStrategy
import java.io.File
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.util.Log

object Replicate_Test {

  def main(args: Array[String]) : Unit = {
    
    val from = "/home/james/tpg/dj-scala/dj-scala/var"
    val to = "/home/james/tpg/dj-scala/dj-scala/var-repl"
    val l = Library.load(new java.io.File("test.djs"))
    
    l.m.replDJ(from, to)
    
  }
  
}