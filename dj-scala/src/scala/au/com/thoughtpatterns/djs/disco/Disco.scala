package au.com.thoughtpatterns.djs.disco

import java.io.File
import java.io.FileReader
import java.text.CollationKey

import scala.Option.option2Iterable
import scala.annotation.migration
import scala.collection.JavaConversions.asScalaBuffer

import au.com.thoughtpatterns.core.util.CsvUtils
import au.com.thoughtpatterns.djs.disco.CollectionsUtil.readCsv
import au.com.thoughtpatterns.djs.disco.Types.GordonAlbum
import au.com.thoughtpatterns.djs.disco.Types.GordonArtist
import au.com.thoughtpatterns.djs.disco.Types.GordonMp3Album
import au.com.thoughtpatterns.djs.disco.Types.GordonMp3Artist
import au.com.thoughtpatterns.djs.disco.Types.TINT
import au.com.thoughtpatterns.djs.disco.Types.TdjAlbum
import au.com.thoughtpatterns.djs.disco.Types.TdjArtist
import au.com.thoughtpatterns.djs.disco.Types.TiAlbumSide
import au.com.thoughtpatterns.djs.disco.Types.TiArtist
import au.com.thoughtpatterns.djs.disco.Types.parseTINT
import au.com.thoughtpatterns.djs.lib
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.util.RecordingDate

@SerialVersionUID(1L)
class Disco(lib: Option[Library], factories: List[Disco.EquivalenceFactory] = List()) extends Serializable {

  import Disco._

  val libSrc = lib match {
    case Some(l) => new LibrarySource(l)
    case None => null
  }

  def hasPerformance(p: Performance, src: Source): Boolean = {
    val coset = equiv.coset(p)
    val sources = coset.members flatMap { perfToSources.getOrElse(_, Set()) } map { _.src }
    val has = sources.contains(src)
    has
  }

  lazy val libPerformances = for (lib0 <- lib.toSeq; p <- lib0.m.tvm.approxPerfs)
    yield SourcedPerformance(Performance(p), libSrc)

  lazy val rawSourcedPerformances = (
    Gordon.performances ++
    TangoInfo.performances ++
    TangoDJ.performances ++
    libPerformances)

  lazy val rawPerformances = rawSourcedPerformances map { _.perf }

  lazy val sourcedToPerf = (for (sp <- rawSourcedPerformances) yield (sp -> sp.perf)).toMap
  lazy val perfToSources = CollectionsUtil.invert(sourcedToPerf)

  private def build(sets: List[Set[Performance]], e: Equivalence): Equivalence = {
    sets match {
      case (a :: b) => build(b, e ++ a)
      case Nil => e
    }
  }

  lazy val equiv = {
    // Start with empty equiv
    val empty = Equivalence(Set[Performance]())

    // Identify performances with equivalent titles according to this collator
    val spanish = java.text.Collator.getInstance(java.util.Locale.forLanguageTag("es"));
    spanish.setStrength(java.text.Collator.PRIMARY)

    val nonword = "[^\\p{L}]".r
    val trim = " +".r

    def canonical(s: String) = if (s != null) trim.replaceAllIn(nonword.replaceAllIn(s, " "), " ") else s

    case class Key(nameKey: CollationKey, performer: TiArtist, date: RecordingDate)

    val keys = (rawPerformances map {
      x =>
        x match {
          case Performance(name, genre, performer, date) =>
            (x -> Key(spanish.getCollationKey(canonical(name)), performer, date))
        }
    }).toMap

    val identified = CollectionsUtil.invert(keys).filter({ x => x match { case (a, b) => b.size > 1 } }).values.toList

    val first = build(identified, empty)

    // Now identify performances according to album matches

    val gordonAlbumIdentified = (
      for (
        (g, t) <- Gordon.trackMap;
        ti <- TangoInfo.tintMap.get(t)
      ) yield {
        Set(Gordon.mapGordon(g).perf, ti.toPerformance.perf)
      }).toList

    val second = build(gordonAlbumIdentified, first)

    val tdjAlbumIdentified = (
      for (
        (g, t) <- TangoDJ.trackMap;
        ti <- TangoInfo.tintMap.get(t)
      ) yield {
        Set(TangoDJ.mapTdj(g).perf, ti.toPerformance.perf)
      }).toList

    val third = build(tdjAlbumIdentified, second)

    def remainder(facts: List[EquivalenceFactory], equiv: Equivalence): Equivalence = {
      facts match {
        case head :: tail => {
          val e = head.equivs
          val next = build(e, equiv)
          remainder(tail, next)
        }
        case _ => equiv
      }
    }

    val last = remainder(factories, third)

    last
  }

  lazy val performances = (rawPerformances map { x => equiv.coset(x) }).toSet

  def getEquivSourced(perf: Performance) = {
    equiv.coset(perf).members flatMap { perfToSources.getOrElse(_, Set()) }
  }

  def getEquivSourced(perf: Equivalence#Coset) = {
    perf.members flatMap { perfToSources.getOrElse(_, Set()) }
  }

}

object Disco {

  trait EquivalenceFactory {
    def equivs: List[Set[Performance]]
  }

  // Identify performances with equivalent titles according to this collator
  private val spanish = java.text.Collator.getInstance(java.util.Locale.forLanguageTag("es"));
  spanish.setStrength(java.text.Collator.PRIMARY)

  private val nonword = "[^\\p{L}]".r
  private val trim = " +".r

  def toCollationKey(title: String) = {

    def canonical(s: String) = if (s != null) trim.replaceAllIn(nonword.replaceAllIn(s, " "), " ") else s

    spanish.getCollationKey(canonical(title))
  }

  // This should be a generic class but I couldn't get the subclassing necessary for overriding Coset.rep to work.
  @SerialVersionUID(1L)
  class Equivalence protected (identified: Set[Performance], base: Equivalence) extends Serializable {

    class Coset(val members: Set[Performance]) {
      def rep = {
        members.head
      }
    }

    type C = Equivalence#Coset

    val cosets: Map[Performance, C] = {

      val mergedCosets = if (base != null)
        identified flatMap { t => base.cosets.get(t) }
      else
        Set()

      val mergedCosetMembers = (mergedCosets flatMap { c => c.members }) ++ identified

      val mergedCoset = new Coset(mergedCosetMembers)

      val baseKeys = if (base != null)
        base.cosets.keys
      else
        Set()

      val z = (for (t <- mergedCosetMembers) yield (t -> mergedCoset)).toMap

      if (base != null) base.cosets ++ z else z;
    }

    def equiv(a: Performance, b: Performance): Boolean = coset(a) == coset(b)

    def coset(t: Performance): C = {
      cosets.getOrElse(t, new Coset(Set(t)))
    }

    def ++(i: Set[Performance]) = {
      if (i.size <= 1) {
        this
      } else {
        new Equivalence(i, this)
      }
    }

  }

  object Equivalence {

    def apply[T](identify: Set[Performance]) =
      if (identify.size > 1)
        new Equivalence(identify, null)
      else
        new Equivalence(Set[Performance](), null)

  }

  // ----------------------

  @SerialVersionUID(1L)
  case class SourcedPerformance(perf: Performance, src: Source) extends Serializable

  @SerialVersionUID(1L)
  case class Performance(name: String, genre: String, performer: TiArtist, date: RecordingDate) extends Serializable {
    /**
     * Yield approximate performances
     */
    def toLibPerformance = lib.Performance(
      name, if (performer != null) performer.toString else null, genre, if (date != null) date.approx else null)

    /**
     * Yield dateless equiv
     */
    def stripDate = Performance(name, genre, performer, null)
    
    /**
     * Yield vocalist-less equiv
     */
    def stripVocalist = 
      if (performer.vocalist == null) 
        Performance(name, genre, TiArtist(performer.orchestra, null), date) 
      else 
        this
  }

  def cleanVoc(voc: String) = voc match {
    case null => null
    case "-" => null
    case "0" => null
    case _ => voc
  }

  object Performance {

    private def split(orq: String): TiArtist = {

      val voc = "(.+), voc\\. (.+)".r
      val gordon = "(.+) ; (.+)".r

      if (orq == null) {
        return null
      }

      orq match {
        case voc(a, b) => TiArtist(a, cleanVoc(b))
        case gordon(a, "Instrumental") => TiArtist(a, null)
        case gordon(a, b) => TiArtist(a, cleanVoc(b))
        case _ => TiArtist(orq, null)
      }
    }

    def apply(p: lib.Performance): Performance = Performance(p.title, p.genre, split(p.artist), p.year)

  }

  @SerialVersionUID(1L)
  trait Source extends Serializable {

    def performances: Iterable[SourcedPerformance]

    def name: String

    override def toString = name

  }

  @SerialVersionUID(1L)
  class LibrarySource(lib: Library) extends Source {
    val name = "lib"

    lazy val performances = lib.m.tvm.approxPerfs map {
      p => SourcedPerformance(Performance(p), this)
    }
  }

  // -----------------------
  // TI stuff

  object TangoInfo extends Source {

    val name = "ti"

    case class TiTrack(
      name: String, // 0
      genre: String, // 2
      performer: TiArtist, // 3,4
      date: RecordingDate, // 5
      tint: TINT) { // 7 
      def approxDate = if (date != null) date.approx else null

      def toPerformance = SourcedPerformance(Performance(name, genre, performer, approxDate), TangoInfo)
    }

    lazy val tiTracks = {
      val rows = (new CsvUtils()).fromCsv(new FileReader("ti-tracks.csv"))
      for {
        row <- rows.toList
        maybe = parseTINT(row(7))
        if maybe.isDefined
      } yield {
        TiTrack(row(0), row(2), TiArtist(row(3), cleanVoc(row(4))), RecordingDate.parse(row(5)), maybe.get)
      }
    }

    lazy val tintMap: Map[TINT, TiTrack] = (for { t <- tiTracks } yield { (t.tint, t) }).toMap

    lazy val performances =
      (for { t <- tiTracks } yield { t.toPerformance }).toSet

    lazy val albums: Map[TiAlbumSide, List[TiTrack]] =
      tiTracks.groupBy(t => TiAlbumSide(t.tint.tin, t.tint.side))
  }

  // -----------------------
  // TDJ stuff

  object TangoDJ extends Source {

    val name = "tdj"

    case class TdjTrack(
      name: String, // 2
      genre: String, // 5
      orchestra: TdjArtist, // 3
      album: String, // 8
      date: RecordingDate,
      track: Int,
      rating: Int) // 7

    lazy val albumMap: Map[TdjAlbum, TiAlbumSide] =
      readCsv("tdj-ti-album-map.csv",
        row => (TdjAlbum(row(0)), TiAlbumSide(row(1), Integer.parseInt(row(2)))))

    lazy val tdjTracks = {
      val rows = (new CsvUtils()).fromCsv(new FileReader("tango-at.csv"))
      for {
        row <- rows.toList
      } yield {
        TdjTrack(row(2), row(5), TdjArtist(row(3)), row(8), RecordingDate.parse(row(4)), row(1).toInt, row(7).toInt)
      }
    }

    lazy val trackMap: Map[TdjTrack, TINT] = {
      (for {
        y <- tdjTracks
        a = TdjAlbum(y.album)
        b = albumMap.get(a)
        if (b.isDefined)
        c = b.get
      } yield {
        (y, TINT(c.tin, c.side, y.track))
      }).toMap
    }

    lazy val artistMap = readCsv("tdj-ti-artist-map.csv",
      row => {
        def f(k: Int) = if (row.length > k) row(k) else null
        (TdjArtist(f(0)), TiArtist(f(1), cleanVoc(f(2))))
      })

    implicit def tdjToTi(a: TdjArtist): TiArtist = artistMap.getOrElse(a, TiArtist(a.name, null))

    def mapTdj(tracks: Iterable[TdjTrack]): Iterable[SourcedPerformance] =
      for { t <- tracks } yield { mapTdj(t) }

    def mapTdj(t: TdjTrack): SourcedPerformance = {
      val tint = trackMap.get(t)
      val name0 = tint.flatMap({ x: TINT => TangoInfo.tintMap.get(x) })
      val name = name0.map({ y: TangoInfo.TiTrack => y.name })

      val n = name.getOrElse(t.name)

      val perf = Performance(name.getOrElse(t.name), t.genre.toLowerCase(), t.orchestra, t.date)
      SourcedPerformance(perf, this)
    }

    lazy val performances = mapTdj(tdjTracks).toSet
  }

  // -----------------------
  // Gordon stuff

  object Gordon extends Source {

    val name = "gordon"

    case class GordonTrack(
      name: String,
      genre: String,
      orchestra: GordonArtist,
      album: String,
      date: RecordingDate,
      track: Int,
      file: File) {
    }

    lazy val albumMap: Map[GordonAlbum, TiAlbumSide] =
      readCsv("gordon-ti-album-map.csv",
        row => (GordonAlbum(row(0)), TiAlbumSide(row(1), Integer.parseInt(row(2)))))

    lazy val gordonTracks = {
      val rows = (new CsvUtils()).fromCsv(new FileReader("gordon.csv"))
      for {
        row <- rows.toList
      } yield {
        GordonTrack(row(0), row(3), GordonArtist(row(1)), row(4), RecordingDate.parse(row(2)), row(5).toInt, new File(row(6)))
      }

    }

    lazy val trackMap: Map[GordonTrack, TINT] = {
      (for {
        y <- gordonTracks
        a = GordonAlbum(y.album)
        b = albumMap.get(a)
        if (b.isDefined)
        c = b.get
      } yield {
        (y, TINT(c.tin, c.side, y.track))
      }).toMap
    }

    lazy val artistMap0 = readCsv("gordon-ti-artist-map.csv",
      row => {
        def f(k: Int) = if (row.length > k) row(k) else null
        (GordonArtist(f(0)), TiArtist(f(1), cleanVoc(f(2))))
      })

    // Extend gordon-ti-artist-map to cover collator-equivalences
    lazy val artistMap1 = {

      val spanish = java.text.Collator.getInstance(java.util.Locale.forLanguageTag("es"));
      spanish.setStrength(java.text.Collator.PRIMARY)

      def extend(artists: List[GordonArtist], base: Map[GordonArtist, TiArtist]): Map[GordonArtist, TiArtist] = {
        artists match {
          case (artist :: tail) => {
            val equivs = base.keys filter { _.name != null } filter {
              a => artist.name != null && (spanish.compare(artist.name, a.name) == 0)
            } take 1
            val newPart = (for (g <- equivs; t <- base.get(g)) yield (artist -> t)).toMap

            extend(tail, base ++ newPart)
          }
          case Nil => base
        }
      }

      val gordonArtists = ((gordonTracks map { g => g.orchestra }) toSet) filter { g => artistMap0.get(g) == None } toList

      extend(gordonArtists, artistMap0)
    }

    // Map any remaining "A ; B" forms to "A voc. B" unless B = "Instrumental"
    lazy val artistMap = {
      val all = ((gordonTracks map { g => g.orchestra }) toSet) filter { g => artistMap0.get(g) == None } toList
      val remaining = (all.toSet diff artistMap1.keySet) filter { _.name != null } filter { _.name.contains(" ; ") }

      val semi = "(.+) ; (.+)".r

      def mapOrq(in: String): String = {
        val x = (artistMap1.get(GordonArtist(in)) ++ artistMap1.get(GordonArtist(in + " ; Instrumental")))
        val y = x map { _.orchestra }
        val z = y.headOption.getOrElse(in)

        z
      }

      def map(in: GordonArtist): TiArtist = {
        in match {
          case GordonArtist(semi(orq, "Instrumental")) => TiArtist(mapOrq(orq), null)
          case GordonArtist(semi(orq, voc)) => TiArtist(mapOrq(orq), voc)
          case GordonArtist(orq) => TiArtist(mapOrq(orq), null)
        }
      }

      val mapped = (for (r <- remaining) yield (r -> map(r))).toMap

      artistMap1 ++ mapped
    }

    implicit def gordonToTi(a: GordonArtist): TiArtist = artistMap.getOrElse(a, TiArtist(a.name, null))

    def mapGordon(tracks: Iterable[GordonTrack]): Map[GordonTrack, SourcedPerformance] =
      (for { t <- tracks } yield { t -> mapGordon(t) }).toMap

    def mapGordon(t: GordonTrack): SourcedPerformance = {
      val tint = trackMap.get(t)
      val name0 = tint.flatMap({ x: TINT => TangoInfo.tintMap.get(x) })
      val name = name0.map({ y: TangoInfo.TiTrack => y.name })

      val n = name.getOrElse(t.name)

      val perf = Performance(name.getOrElse(t.name), if (t.genre != null) t.genre.toLowerCase() else null, t.orchestra, t.date)
      SourcedPerformance(perf, this)
    }

    lazy val gordonPerformances = mapGordon(gordonTracks)

    lazy val gordonTrackSrc = CollectionsUtil.invert(gordonPerformances)

    lazy val performances = gordonPerformances.values.toSet
  }

  // -----------------------
  // Gordon stuff

  object GordonMp3 extends Source {

    val name = "gordon-mp3"

    case class GordonMp3Track(
      name: String,
      genre: String,
      orchestra: GordonMp3Artist,
      album: String,
      date: RecordingDate,
      track: Int,
      file: File) {
    }

    lazy val albumMap: Map[GordonMp3Album, TiAlbumSide] =
      readCsv("gordon-ti-album-map.csv",
        row => (GordonMp3Album(row(0)), TiAlbumSide(row(1), Integer.parseInt(row(2)))))

    lazy val gordonTracks = {
      val rows = (new CsvUtils()).fromCsv(new FileReader("gordon-mp3.csv"))
      for {
        row <- rows.toList
      } yield {
        GordonMp3Track(row(0), row(3), GordonMp3Artist(row(1)), row(4), RecordingDate.parse(row(2)), try { row(5).toInt } catch { case _: Exception => 0 }, new File(row(6)))
      }

    }

    lazy val trackMap: Map[GordonMp3Track, TINT] = {
      (for {
        y <- gordonTracks
        a = GordonMp3Album(y.album)
        b = albumMap.get(a)
        if (b.isDefined)
        c = b.get
      } yield {
        (y, TINT(c.tin, c.side, y.track))
      }).toMap
    }

    lazy val artistMap0 = readCsv("gordon-mp3-ti-artist-map.csv",
      row => {
        def f(k: Int) = if (row.length > k) row(k) else null
        (GordonMp3Artist(f(0)), TiArtist(f(1), cleanVoc(f(2))))
      })

    // Extend gordon-ti-artist-map to cover collator-equivalences
    lazy val artistMap1 = {

      val spanish = java.text.Collator.getInstance(java.util.Locale.forLanguageTag("es"));
      spanish.setStrength(java.text.Collator.PRIMARY)

      def extend(artists: List[GordonMp3Artist], base: Map[GordonMp3Artist, TiArtist]): Map[GordonMp3Artist, TiArtist] = {
        artists match {
          case (artist :: tail) => {
            val equivs = base.keys filter { _.name != null } filter {
              a => artist.name != null && (spanish.compare(artist.name, a.name) == 0)
            } take 1
            val newPart = (for (g <- equivs; t <- base.get(g)) yield (artist -> t)).toMap

            extend(tail, base ++ newPart)
          }
          case Nil => base
        }
      }

      val gordonArtists = ((gordonTracks map { g => g.orchestra }) toSet) filter { g => artistMap0.get(g) == None } toList

      extend(gordonArtists, artistMap0)
    }

    // Map any remaining "A ; B" forms to "A voc. B" unless B = "Instrumental"
    lazy val artistMap = {
      val all = ((gordonTracks map { g => g.orchestra }) toSet) filter { g => artistMap0.get(g) == None } toList
      val remaining = (all.toSet diff artistMap1.keySet) filter { _.name != null } filter { _.name.contains(" ; ") }

      val semi = "(.+) ; (.+)".r

      def mapOrq(in: String): String = {
        val x = (artistMap1.get(GordonMp3Artist(in)) ++ artistMap1.get(GordonMp3Artist(in + " ; Instrumental")))
        val y = x map { _.orchestra }
        val z = y.headOption.getOrElse(in)

        z
      }

      def map(in: GordonMp3Artist): TiArtist = {
        in match {
          case GordonMp3Artist(semi(orq, "Instrumental")) => TiArtist(mapOrq(orq), null)
          case GordonMp3Artist(semi(orq, voc)) => TiArtist(mapOrq(orq), voc)
          case GordonMp3Artist(orq) => TiArtist(mapOrq(orq), null)
        }
      }

      val mapped = (for (r <- remaining) yield (r -> map(r))).toMap

      artistMap1 ++ mapped
    }

    implicit def gordonToTi(a: GordonMp3Artist): TiArtist = artistMap.getOrElse(a, TiArtist(a.name, null))

    def mapGordon(tracks: Iterable[GordonMp3Track]): Map[GordonMp3Track, SourcedPerformance] =
      (for { t <- tracks } yield { t -> mapGordon(t) }).toMap

    def mapGordon(t: GordonMp3Track): SourcedPerformance = {
      val tint = trackMap.get(t)
      val name0 = tint.flatMap({ x: TINT => TangoInfo.tintMap.get(x) })
      val name = name0.map({ y: TangoInfo.TiTrack => y.name })

      val n = name.getOrElse(t.name)

      val perf = Performance(name.getOrElse(t.name), if (t.genre != null) t.genre.toLowerCase() else null, t.orchestra, t.date)
      SourcedPerformance(perf, this)
    }

    lazy val gordonPerformances = mapGordon(gordonTracks)

    lazy val gordonTrackSrc = CollectionsUtil.invert(gordonPerformances)

    lazy val performances = gordonPerformances.values.toSet
  }

}