package au.com.thoughtpatterns.djs.disco

import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.disco.Disco.Performance
import au.com.thoughtpatterns.djs.disco.Types.TiArtist
import java.io.File
import au.com.thoughtpatterns.djs.util.RecordingDate

object ImporterTest3 {

  def main(args: Array[String]) {
    val lib = Library.load(new File("jamie.djs"))

    val x = lib.im("/media/Orange/Gordon/tmp/Rodolfo Biagi")
    
    val z = x.contents.head.idLib
    
  }

}