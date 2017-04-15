package au.com.thoughtpatterns.djs.disco.tangoinfo

import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.lib.ManagedMusic
import au.com.thoughtpatterns.dj.disco.tangoinfo.TangoInfo
import au.com.thoughtpatterns.core.util.BusinessDate
import au.com.thoughtpatterns.djs.lib.MusicFile
import au.com.thoughtpatterns.djs.util.Log
import au.com.thoughtpatterns.djs.tag.TagFactory
import au.com.thoughtpatterns.djs.util.RecordingDate
import au.com.thoughtpatterns.core.util.SystemException

class Synchroniser(lib: Library, list: ManagedMusic) {

  var dryrun: Boolean = false

  var maxdays: Int = 180

  private val OLD_TIN = "([0-9]+),SIDE=([0-9]+)".r

  def update() {
    val ti = new TangoInfo()

    val today = BusinessDate.newSystemToday()

    def tint(m: MusicFile) = {
      val tin0 = m.extraProps.getOrElse("TIN", "")
      val (tin, side) = tin0 match {
        case OLD_TIN(tin, side) => (tin, side.toInt)
        case _ => (tin0, m.extraProps.getOrElse("SIDE", "1").toInt)
      }

      (tin, side)
    }

    for (
      m <- list;
      md <- m.md) {
      Log.info(md + ": " + m.extraProps.getOrElse("ti_last_update", "2000-01-01"));
    }

    
    val update = for (
      m <- list;
      md <- m.md;
      if (md.comment != null && md.comment.contains("TIN"));
      lastUpdate = m.extraProps.getOrElse("ti_last_update", "2000-01-01");
      date = BusinessDate.newYYYYMMDD_quiet(lastUpdate);
      if today.daysSince(date) > maxdays
    ) yield {

      // Side effect - queue fetching
      val (tin, side) = tint(m)
      ti.fetchTINT(tin, side, md.track)

      // Yield m
      m
    }

    val ch = MusicFile.CommentHelper
    val tags = new TagFactory

    for (
      m <- update;
      md <- m.md;
      (tin, side) = tint(m);
      timd = ti.getMetadata(tin, side, md.track);
      if (timd != null)
    ) {

      Log.info("---------------")
      Log.info("Data for " + m.file)
      Log.info("Title: " + timd.title)
      Log.info("Artist: " + timd.artist)
      Log.info("Album: " + timd.album)
      Log.info("Track: " + timd.trackNumber)
      Log.info("Genre: " + timd.genre)
      Log.info("Date: " + timd.date)

      val tag = tags.getTag(m.file)

      val before = tag.getSignature

      tag.setAlbum(timd.album)
      tag.setArtist(timd.artist)
      tag.setTitle(timd.title)

      // Don't change pruned genres
      val oldGenre = tag.getGenre()
      if (oldGenre == null || !oldGenre.contains("pruned")) {
        tag.setGenre(timd.genre)
      }

      val year = RecordingDate.parse(timd.date)
      if (year != null) {
        val already = tag.getYear()
        if (already == null || !already.refines(year)) {
          tag.setYear(year)
        }
      }

      val after = tag.getSignature

      val comment = ch.setExtraProp(tag.getComment(), "ti_last_update", today.toYYYY_MM_DD())
      tag.setComment(comment)
      
      // Debugging
      if (! comment.contains("TIN")) 
        throw new SystemException("Invalid TIN in " + comment)
      
      if (!dryrun) {
        tag.write()
        m.update()
      }

    }
  }

}

