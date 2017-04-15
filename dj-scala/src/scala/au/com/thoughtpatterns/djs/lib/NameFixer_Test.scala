package au.com.thoughtpatterns.djs.lib

import java.io.File

object NameFixer_Test {
  
  def main(args: Array[String]) {

    val l = Library.load(new File("/home/djs/replica-dj/library.djs"))

    val f = new NameFixer(l.m.tvm.byTitle)
    
    //println(found)
  }
    
}