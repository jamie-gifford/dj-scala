package au.com.thoughtpatterns.djs.model

import java.io.File
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.lib.MusicFile
import au.com.thoughtpatterns.djs.disco.Disco

object AlbumIdentifier_Test {
  
    def main(args: Array[String]) {

      val l = Library.load(new File("/home/djs/replica-dj/library.djs"))

      val target = (l.m \ l.m.path("cortina")).m.require(x => ! (x.comment != null && x.comment.contains("TIN")))
      
      val albums = target.groupBy { x => x.md.get.album }      
      val ti = Disco.TangoInfo
      
      for ((album, tracks) <- albums; if (album != null)) {

        println
        println("-------------------------------------")
        println(s"Album: $album")
//        for (t <- tracks; md <- t.md) {
//          println(s"  ${md.track}: $t")
//        }
        println

        val trackMap = tracks.map { t => ( t.md.get.track -> t ) }.toMap  
        
        val trackIndices = trackMap.keys.toSeq.sorted
        
        val items = tracks.map(toItem(_)).toList
        
        val a = new AlbumIdentifier(items)
        
        val results = a.identify.take(1)
        
        for (r <- results) {
          
          println(s"TINT: $r")
          
          val ts = ti.albums.getOrElse(r, List.empty)
          
          val claimedTrackMap = ts map { x => ( x.tint.track -> x ) } toMap 
          
          if ((trackIndices.toSet -- claimedTrackMap.keys).size > 0) {

            for (i <- trackIndices) {
              val ours = trackMap(i).md.get
              println(s"  OURS $i : ${ours.title} / ${ours.artist} / ${ours.year}")
            }

            for (theirs <- ts.seq.sortBy { t => t.tint.track }) {
              println(s"  THEIRS ${theirs.tint.track} : ${theirs.name} / ${theirs.performer} / ${theirs.date}") 
            }
            
          } else {
            for (i <- trackIndices) {
              val ours = trackMap(i).md.get
              val theirs = claimedTrackMap(i)
              println(s"  ${ours.title} / ${ours.artist} / ${ours.year} -> ${theirs.name} / ${theirs.performer} / ${theirs.date}")
            }
          }
          
        }
        
      }
      
      //val a = new AlbumIdentifier(items)
      
    }
    
    def toItem(m: MusicFile) : Item = {
      val md = m.md.get
      return new Item(if(md.track > 0) Some(md.track) else None, md.artist, md.title)
    }
}