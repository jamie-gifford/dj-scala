package au.com.thoughtpatterns.djs.lib

trait PlaylistSet extends Iterable[PlaylistFile] {

  implicit def iterableToPlaylistSet(i: Iterable[PlaylistFile]): PlaylistSet = new PlaylistSet() {
    def iterator = i.iterator
  }

}

object PlaylistSet {
  def apply(i: Iterable[PlaylistFile]) : PlaylistSet = new PlaylistSet() {
    def iterator = i.iterator
  }
}