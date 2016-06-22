package au.com.thoughtpatterns.djs.clementine

import java.io.File
import java.net.URL

import scala.Option.option2Iterable

import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.lib.ManagedMusic
import au.com.thoughtpatterns.djs.lib.MusicFile

class Player(val lib: Library) {

  def ext = PlayerInterfaceFactory.getPlayer();
  
  /**
   * Fetch current track
   */
  def q = {
    val url = ext.getCurrentTrack()
    ManagedMusic(lib, Some(toMusic(url)))
  }
  
  /**
   * Fetch current tracks
   */
  def qq = {
	val l = for (i <- Range(0, ext.getLength())) yield {
      val url = ext.getTrackLocation(i)
      toMusic(url)
    }
    new ManagedMusic(lib) {
      def iterator = l.iterator
    }
  }

  private def toMusic(url: URL) = {
    val file = try {
      new File(url.toURI())
    } catch {
      case _ : Exception => new File(url.getPath());
    }
    lib.resolve(new MusicFile(file))
  }
  
  
}