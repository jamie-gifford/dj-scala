package au.com.thoughtpatterns.djs.disco

import au.com.thoughtpatterns.dj.disco.tangoinfo.Tracks
import java.io.File
import au.com.thoughtpatterns.dj.disco.tangodjat.TangoDJAt

object StaticFiles {

  def loadTiTracks() {
    // ti-tracks.csv
    Tracks.loadFromWeb(new File("ti-tracks.csv"));
  }

  def loadTangoAtTracks() {
    val a = new TangoDJAt();
    a.fetchArtists();
    // Doesn't work because of 300 record limit
    a.dump(new File("BROKEN-tango-at.csv"));
  }
  
  def main(args: Array[String]) {
    loadTiTracks();
    loadTangoAtTracks();

  }

}