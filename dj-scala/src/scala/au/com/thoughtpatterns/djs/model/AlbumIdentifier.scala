package au.com.thoughtpatterns.djs.model

import au.com.thoughtpatterns.djs.disco.Types.TiAlbumSide
import au.com.thoughtpatterns.djs.disco.Types.TINT

import scala.collection.JavaConversions._
import au.com.thoughtpatterns.djs.util.Log

case class Item(track: Option[Integer], artist: String, title: String)

class AlbumIdentifier(items: List[Item]) {

  val model = TIModel()

  def score(a: TiAlbumSide, remember: Boolean) = {

    val ratios = for (
      item <- items;
      trackNo <- item.track;
      t <- a match {
        case TiAlbumSide(tin, side) => model.getTrack(TINT(tin, side, trackNo))
      }
    ) yield {
      if (t == null) {
        // Album does not have that track
        0d
      } else {
        model.calcRatio(item.title, item.artist, t.title, t.orchestra)
      }
    }
    ratios.foldLeft(1d)(_ * _)
  }

  def identify: List[TiAlbumSide] = {
    val albums = model.tracks.getAlbums()
    
    Log.info(s"Model has ${albums.size} albums")
    
    val scores = (for (a <- albums; sides = a.getSides(); side <- 1 to sides) yield {
      val as = TiAlbumSide(a.getTin(), side)
      val s = score(as, false)
      as -> s
    }).toMap 
    
    Log.info("Identify against " + scores.keySet.size + " albums...")
    
    scores.keys.toList.sortBy({-scores.getOrElse(_, 0d)})
  }

}
