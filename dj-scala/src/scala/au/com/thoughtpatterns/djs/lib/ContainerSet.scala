package au.com.thoughtpatterns.djs.lib

// Unused...
trait ContainerSet extends Iterable[MusicContainer] {

  private def musicIterable =
    (this filter {
      x => x match { case y: MusicFile => true case _ => false }
    }).asInstanceOf[Iterable[MusicFile]]

  private def playlistIterable =
    (this filter {
      x => x match { case y: PlaylistFile => true case _ => false }
    }).asInstanceOf[Iterable[PlaylistFile]]


  // Idiomatic?
  def music = new MusicSet() {
    def iterator = musicIterable.iterator
  }
  
  def playlists = new PlaylistSet() {
    def iterator = playlistIterable.iterator
  }
  
  def indirectContents = {
    MusicSet(music.toSet union (playlists flatMap { _.indirectContents }).toSet)
  }

}