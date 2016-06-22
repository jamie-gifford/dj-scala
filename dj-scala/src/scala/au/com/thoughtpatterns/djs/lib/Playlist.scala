package au.com.thoughtpatterns.djs.lib

@SerialVersionUID(1L)
trait Playlist extends Serializable {

  def tracks : List[MusicFile]
  
  def broken = (tracks map { _.file } filter { ! _.exists() }).size > 0
  
}