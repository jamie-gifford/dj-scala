package au.com.thoughtpatterns.djs.app

import java.io.File
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.webapp.Main
import au.com.thoughtpatterns.djs.lib.MetadataCache

/**
 * The following operations are available:
 * 
 * (Managed):
 * 
 * music (or m): get ManagedMusic
 * playlists (or p): get ManagedPlaylists
 * path(str): filter by path
 * 
 * || union
 * && intersection
 * \ difference
 * 
 * indirectContents: ManagedMusic contents of all playlists
 * 
 * filtre(f: T => Boolean) filter
 * 
 * replicate: manage a transformed copy of the contents (TODO refine this)
 * 
 * (ManagedMusic)
 * 
 * require(Metadata => Boolean): filter on metadata
 * dups: find dups within this ManagedMusic
 * 
 * ~: intersection based on "like" (ie, by approx performance instead of file)
 * 
 * q: Send contents to Clementine
 * 
 * byTitle, byArtist, byYear: sorting functions
 * 
 * prune: add "pruned2-" to genre of this.
 * 
 * synchronise: load metadata from TangoInfo.
 * 
 * (ManagedPlaylists)
 * 
 * containing(ManagedMusic): find subset of this whose contents intersects with the argument
 * 
 * relativize: save as relative playlists
 * 
 * prefer(ManagedMusic): use argument in preference to current contents.
 * 
 * 
 * Useful lines:
 * 
 * // exchange with currently playing track
 * val j = l.path(...).playlists 
 * q.q.exchange(j)
 * 
 */
object App {

  object cfg {
    var libfile: Option[File] = None
    var faststart: Boolean = false
    var fix: Boolean = false
    var list: Option[String] = None
    var add: List[File] = Nil
    var save = false
    var interpreter = false
    var webapp = false
  }
  
  def main(args: Array[String]) = {
    parse(args.toList)
    
    val lib = cfg.libfile match 
      { case Some(file) => Library.load(file) case None => new Library(None) }
    
    for ( f <- cfg.add) { 
      lib.add(f) 
    } 
    
    if (cfg.fix) {
      lib.playlists.relativize()
    }
    
    if (! cfg.faststart) {
      lib.syncM0U()
      lib.update()
      lib.playlists.adjust()
    }

    if (cfg.fix) {
      lib.playlists.relativize()
    }
    

    if (cfg.save) {
      lib.write0 // unconditional
    } else {
      lib.write // only when dirty
    }

    if (cfg.webapp) Main.runInThread()

    if (cfg.interpreter) new LibInterpreter(lib).run()
    
    for (genre <- cfg.list) {
      val x = for (m <- lib.music; md <- m.md; if (md.genre == genre)) yield m
      list(x)
    }
  }
  
  private def parse(args : List[String]) {
    
    args match {
      case ( "-l" :: lib :: tail ) => {
        cfg.libfile = Some(new File(lib))
        parse(tail)
      }
      case ( "faststart" :: tail ) => {
        cfg.faststart = true
        parse(tail)
      } 
      case ( "list" :: genre :: tail ) => {
        cfg.list = Some(genre)
        parse(tail)
      }
      case ( "add" :: filename :: tail ) => {
        cfg.add = new File(filename) :: cfg.add
        cfg.save = true
        parse(tail)
      }
      case ( "fix" :: tail ) => {
        cfg.fix = true
        cfg.save = true
        parse(tail)
      }
      case ( "-i" :: tail ) => {
        cfg.interpreter = true
        parse(tail)
      }
      case ( "-w" :: tail ) => {
        cfg.webapp = true
        parse(tail)
      }
      case Nil => {}
      case _ => throw new IllegalArgumentException("unexpected arguments " + args.mkString(" "))
    }
    
  } 
  
  def list(stuff: Iterable[Any]) {
    for (y <- stuff) { println(y) }
  }

}