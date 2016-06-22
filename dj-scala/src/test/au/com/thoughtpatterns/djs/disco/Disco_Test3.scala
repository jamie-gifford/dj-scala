package au.com.thoughtpatterns.djs.disco

import java.io.File

import au.com.thoughtpatterns.djs.disco.Types.TiArtist
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.util.RecordingDate

object Disco_Test3 {

  def main(args: Array[String]) {

    val lib = Library.load(new File("jamie.djs"))
    val disco = lib.ourDisco

    val p1 = Disco.Performance("Rayo de luz", "vals", TiArtist("Edgardo Donato", "Luis Díaz"), null);
    val p2 = Disco.Performance("Rayo De Luz", "vals", TiArtist("Edgardo Donato", "Luis Díaz"), null);

    println("-------------------")

    val e1 = disco.getEquivSourced(p1)
    for (q <- e1) { println(q) }

    println("-------------------")

    val e2 = disco.getEquivSourced(p2)
    for (q <- e2) { println(q) }

  }

}