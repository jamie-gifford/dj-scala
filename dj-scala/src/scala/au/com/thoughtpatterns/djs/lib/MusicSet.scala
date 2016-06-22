package au.com.thoughtpatterns.djs.lib

trait MusicSet extends Iterable[MusicFile] {

  implicit def iterableToMusicSet(i: Iterable[MusicFile]): MusicSet = new MusicSet() {
    def iterator = i.iterator
  }

  def require(f: => Metadata => Boolean): MusicSet = {
    this filter {
      m => m.md match { 
        case Some(metadata) => { import metadata._; f(metadata) } 
        case None => false 
     }
    }
  }

}

object MusicSet {
  def apply(i: Iterable[MusicFile]) : MusicSet = new MusicSet() {
    def iterator = i.iterator
  }
}