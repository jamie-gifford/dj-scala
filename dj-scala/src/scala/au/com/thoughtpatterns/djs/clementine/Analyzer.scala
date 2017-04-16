package au.com.thoughtpatterns.djs.clementine

import scala.annotation.migration
import scala.collection.JavaConverters.asScalaBufferConverter

import au.com.thoughtpatterns.djs.clementine.Clementine.Song
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.lib.ManagedMusic
import au.com.thoughtpatterns.djs.lib.Performance
import au.com.thoughtpatterns.djs.model.TIModel
import au.com.thoughtpatterns.djs.util.Log
import au.com.thoughtpatterns.djs.disco.Types.SpanishWord

class Analyzer(val lib: Library) {
  
  val player = new Clementine()
  
  lazy val songs = player.getTracks.asScala.toSeq
  
  lazy val model = TIModel()
  
  lazy val artistModel = model.artistModel

  def scoreData(query: Performance, perfs: Set[Performance]) = {
    
    (for (perf <- perfs) yield {
      val z = model.calcRatio(query.title, query.artist, perf.title, perf.artist)
      perf -> z
    }).toMap
  }

  /*
  def identify(query: Performance, perfs: Set[Performance]) : List[Performance] = {
    val data = scoreData(query, perfs)
    perfs.toSeq.sortBy(p => - data.getOrElse(p, 0d)).toList
  }
  */

  def eraseYear(p: Performance) = new Performance(p.title, p.artist, p.genre, null)
  
  lazy val mappy = lib.music.map(m => (toFingerprint(m.toApproxPerformance) -> m)).groupBy(_._1).mapValues(x => x.toMap.values.toSet)

  case class Fingerprint(title: Set[SpanishWord], artist: String)
  
  def toFingerprint(p: Performance) = Fingerprint(model.tokenise(p.title), p.artist)
  
  def isNew(song: Song) : ManagedMusic = {
    
    Log.info("")
    Log.info("Identifying " + song + "...")
    
    val artistRatios = model.identifyArtist(song.artist)

    val byRank = artistRatios.groupBy(_._2).mapValues(_.keys)
    
    val ranks = byRank.keys.toSeq.sortBy { -_ }
    
    // -------------------------------
    /*
    val test = ranks take 1
    val a0 = model.tokenise(song.artist).toList
    for (x <- test; if (x > 1); a <- byRank(x)) {
      val a1 = model.artistNamesMap.getOrElse(a, List.empty)
      val likelihood = artistModel.likelihood(a0, a1) 
      Log.info("   " + x + ": " + a + " from " + likelihood)
    }
    */
    // -------------------------------
    
    val top = ranks head
    
    val artists = byRank(top).toSet
    
    Log.info("Top artists " + artists)

    val candidates = lib.m.require(x => artists.contains(x.artist))
    
    val perfs = model.perfs.filter { p => artists.contains(p.artist) }
    
    val perf = new Performance(song.title, song.artist, null, null)
    
    val scores = scoreData(perf, perfs).groupBy(_._2).mapValues(_.keySet)
    
    val hiScore = scores.keySet.toSeq.sortBy(-_).head
    
    // Erase year since this is not present in input
    val hi = scores(hiScore).map(p => new Performance(p.title, p.artist, p.genre, null))

    Log.info("  best tango.info tracks identified as : " + hi)
    
    val already = hi.flatMap { p => mappy.getOrElse(toFingerprint(p), Set.empty) }
    
    Log.info("  mappy gives " + already.size + " files already in library matching " + hi)

    val alreadyLib = already.map { m => lib.resolve(m) }
    
    return ManagedMusic(lib, alreadyLib)

  }
  
  def newToXSPF = {
    
    /*
      XML parsing borked in scala? compiler complains about missing dependency. Sigh... 
    
    <playlist xmlns="http://xspf.org/ns/0/" version="1"> |
  <trackList> |
    {songs.filter(s => isNew(s)).map { s => <track> |
      <location>{s.url}</location> |
      <title>{s.title}</title> |
      <creator>{s.artist}</creator> |
      <album>{s.album}</album> |
      <duration>{s.seconds * 1000}</duration> |
      <trackNum>{s.track}</trackNum> |
    </track> | } } |
  </trackList>
</playlist>
    */

    def q(str: String) = str.replaceAll("<", "&lt;")
    
    def track(s: Song) =  "<track><location>" + q(s.url) + "</location>" + 
      "<title>" + q(s.title) + "</title>" + 
      "<creator>" + q(s.artist) + "</creator>" + 
      "<album>" + q(s.album) + "</album>" + 
      "<duration>" + s.seconds * 1000 + "</duration>" + 
      "<trackNum>" + s.track + "</trackNum>" +
    "</track>"

    val tracks = songs.filter(s => isNew(s).size == 0).map(track(_)).mkString("")
    
    "<playlist xmlns='http://xspf.org/ns/0/' version='1'><trackList> " + tracks + "</trackList></playlist>";

  }
  
  def test {
    for (song <- songs) {
      val already = isNew(song)
      Log.info("Identify " + song + " => " + already.size)
      for (a <- already) {
        Log.info("  " + a)
      }
    }
  }
  
}