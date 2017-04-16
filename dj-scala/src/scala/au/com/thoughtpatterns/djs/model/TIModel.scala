package au.com.thoughtpatterns.djs.model

import java.io.File
import java.io.ObjectInputStream
import java.io.FileInputStream
import scala.util.Try
import scala.util.Success
import java.io.ObjectOutputStream
import java.io.FileOutputStream
import au.com.thoughtpatterns.djs.util.Log
import au.com.thoughtpatterns.dj.disco.tangoinfo.Tracks
import au.com.thoughtpatterns.djs.lib.Performance
import scala.collection.JavaConversions._
import au.com.thoughtpatterns.dj.disco.tangoinfo.Track
import au.com.thoughtpatterns.djs.disco.Types.TINT
import java.io.Reader
import java.io.InputStreamReader
import au.com.thoughtpatterns.djs.disco.Types.SpanishWord


@SerialVersionUID(1L)
class TIModel(tracksReader: Reader) extends Serializable {

  val tracks = Tracks.load(tracksReader)
  val perfs = tracks.toSongs().toSet

  private val albumsByTin = (for (a <- tracks.getAlbums()) yield (a.getTin() -> a)) toMap
    
  def getTrack(tint: TINT) : Option[Track] = tint match {
    case TINT(tin, side, track) => albumsByTin.get(tin) match {
      case Some(album) => Some(album.getTrack(side, track))
      case _ => None
    }
  }
  
  private val titles = perfs map { p => tokenise(p.title) }
  private val artists = perfs map { p => tokenise(p.artist) }

  class ArtistModel(universe: Iterable[Iterable[SpanishWord]]) extends Bayes(universe) {

    val exceptions = Set("Orquesta", "Típica").map { new SpanishWord(_) }

    override def ratio(word: SpanishWord) : Double = {
      
      if (exceptions.contains(word)) {
        1d
      } else {
        super.ratio(word)
      }
    }

  }
  
  val titleModel = new Bayes(titles)
  val artistModel = new ArtistModel(artists)

  def calcRatio(title0: String, artist0: String, title1: String, artist1: String) = {
    val t0 = tokenise(title0).toList
    val t1 = tokenise(title1).toList
    val a0 = tokenise(artist0).toList
    val a1 = tokenise(artist1).toList

    val titleRatio = titleModel.likelihood(t0, t1)
    val artistRatio = artistModel.likelihood(a0, a1)

    titleRatio * artistRatio
  }

  lazy val titleMap = perfs map { p => (p -> tokenise(p.title).toList) } toMap
  lazy val artistMap = perfs map { p => (p -> tokenise(p.artist).toList) } toMap
  
  lazy val artistNames = perfs map { p => p.artist } toSet
  lazy val artistNamesMap = artistNames map { a => (a -> tokenise(a).toList) } toMap

  def identify(title: String, artist: String): List[Performance] = {
    val t0 = tokenise(title).toList
    val a0 = tokenise(artist).toList

    val ratios = perfs map {
      p =>
        {
          // TODO cache these calculations
          val t1 = titleMap.getOrElse(p, List.empty)
          val a1 = artistMap.getOrElse(p, List.empty)

          p -> titleModel.likelihood(t0, t1) * artistModel.likelihood(a0, a1)
        }
    } toMap

    def ratio(p: Performance) = ratios.getOrElse(p, 0d)

    val sorted = perfs.toList.sortWith({ ratio(_) > ratio(_) })
    sorted
  }
  
  def identifyArtist(artist: String): Map[String, Double] = {
    
    val a0 = tokenise(artist).toList

    val ratios = artistNames map {
      a =>
        {
          val a1 = artistNamesMap.getOrElse(a, List.empty)
          val likelihood = artistModel.likelihood(a0, a1) 
          a -> likelihood
        }
    } toMap

    ratios
  }

  def save(f: File) {
    try {
      val oos = new ObjectOutputStream(new FileOutputStream(f));
      oos.writeObject(this);
      oos.close();
    } catch {
      case ex: Exception => Log.info("Failed to store cache" + ex);
    }
  }

  import au.com.thoughtpatterns.djs.disco.Types.SpanishWord

  def tokenise(input: String): Set[SpanishWord] = {
    val tokens = (if (input == null) "" else input).split("[^\\p{IsAlphabetic}0-9]").toSet
    tokens map { new SpanishWord(_) }
  }

}

object TIModel {

  private var model : TIModel = null

  def apply(): TIModel = {

    if (model == null) {

      model = {
        val cacheName = ".tangotracks.csv.model";
        val modelFile = new File(cacheName);

        def load(f: File): TIModel = {
          Log.info("Loading model cache from " + f + "...");
          val ois = new ObjectInputStream(new FileInputStream(f));
          try {
            return ois.readObject().asInstanceOf[TIModel];
          } finally {
            Log.info("...loaded");
            ois.close();
          }
        }

        val loadCache = Try(
          if (modelFile.exists())
            Some(load(modelFile))
          else
            None);

        loadCache match {
          case Success(Some(cache)) => cache
          case _ => {
            val in = getClass.getClassLoader.getResource("au/com/thoughtpatterns/djs/disco/tangoinfo/tangotracks.csv").openStream();
            val reader = new InputStreamReader(in);
            val model = new TIModel(reader);
            model.save(modelFile)
            in.close();
            model;
          }
        }

      }
    }
    
    model

  }
}
