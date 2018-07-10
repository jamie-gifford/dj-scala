package au.com.thoughtpatterns.djs.lib

import java.io.File

object NameFixer_Test {
  
  def main(args: Array[String]) {

    val l = Library.load(new File("/home/djs/tmp/sound.djs"))

    val r = l.m;
    //val r = l.m.title("Arrabalera");
    
    println("Looking at " + r.size + " items");
    
    val f = new NameGuesser(r)
    
    f.preview
    
    //f.rename
    
    //l.m.print
    
    //println(found)
  }
    
}