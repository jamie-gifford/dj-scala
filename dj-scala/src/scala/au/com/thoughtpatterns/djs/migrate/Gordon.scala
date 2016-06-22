package au.com.thoughtpatterns.djs.migrate

import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.disco.Disco
import au.com.thoughtpatterns.djs.disco.Disco.Performance
import au.com.thoughtpatterns.djs.util.Log
import java.io.File
import java.nio.file.Path
import au.com.thoughtpatterns.djs.tag.Lltag
import au.com.thoughtpatterns.djs.disco.Disco.Gordon.GordonTrack
import au.com.thoughtpatterns.djs.disco.CollectionsUtil
import au.com.thoughtpatterns.djs.lib.MusicFile
import java.io.PrintWriter
import java.io.FileWriter

object Gordon {

  def main(args: Array[String]) {

    val lib = Library.load(new java.io.File("jamie-beore-phase2.djs"))

    val disco = lib.ourDisco

    // Things migrated in phase 1 (already implemented)
    val toMigrate = disco.performances map { _.rep } filter {
      p =>
        {
          val srces = disco.getEquivSourced(p) map { _.src }
          srces.contains(Disco.Gordon) &&
            !srces.contains(disco.libSrc) &&
            p.date != null &&
            p.performer != null 
        }
    }

    println("Found " + toMigrate.size + " items to migrate")
    
    // Potentially migratable things that are not yet migrated
    val toNotMigrate = disco.performances filter {
      coset => {
          val srces = disco.getEquivSourced(coset) map { _.src }
          
          // Duplicate test from toMigrate.
          val p = coset.rep
            
          srces.contains(Disco.Gordon) &&
            !srces.contains(disco.libSrc) &&
            ! (
            p.date != null &&
            p.performer != null
            )
      }
    }

    println("Found " + toNotMigrate.size + " items not migrated in phase 1")

    
    // Display stuff that we have not migrated.
    for (coset <- toNotMigrate) {
      val p = coset.rep
      val tracks = disco.getEquivSourced(coset) flatMap { Disco.Gordon.gordonTrackSrc.getOrElse(_, Set.empty) }
      println(p)
      for (track <- tracks) {
        println("  " + track.file)
      } 
    }

    // ----------------------------
    
    // Generate migration plan - old, wrong version that used disco.perfToSources 
    // instead of disco.getEquivSourced

    def getCandidates(m: Performance): Set[Disco.Gordon.GordonTrack] = {
      val srces = disco.perfToSources(m) filter { _.src == Disco.Gordon }
      val out = srces flatMap { Disco.Gordon.gordonTrackSrc.getOrElse(_, Set()) }
      out
    }

    def calculateMigration(perfs: Set[Performance]): Map[Performance, GordonTrack] =
      (for (
        m <- toMigrate;
        candidates = getCandidates(m);
        g <- candidates headOption
      ) yield (m -> g)).toMap

    // Phase 1 = "rightPlan"
    val rightPlan = calculateMigration(toMigrate)

    println("Right plan has " + rightPlan.values.toSet.size + " gordon tracks")

    // -----------------------------
    // Better plan

    def getBetterCandidates(m: Performance): Set[Disco.Gordon.GordonTrack] = {
      val srces = disco.getEquivSourced(m) filter { _.src == Disco.Gordon }
      val out = srces flatMap { Disco.Gordon.gordonTrackSrc.getOrElse(_, Set()) }
      out
    }

    def calculateBetterMigration(perfs: Set[Performance]): Map[Performance, GordonTrack] =
      (for (
        m <- perfs;
        candidates = getBetterCandidates(m);
        g <- candidates headOption
      ) yield (m -> g)).toMap

    // Phase 2 = "betterPlan"
    val betterPlan = calculateBetterMigration(toMigrate)

    println("Better plan has " + betterPlan.values.toSet.size + " gordon tracks")

    // -----------------------------
    // Stuff that is already in the library, if it is rated 0.4 or better, and if the source is bad (Jerry, mm, renata, erika, ipod, murat)
    
    val musicMap = (for (m <- lib.m) yield m -> m.toApproxPerformance).toMap
    
    val perfMap = CollectionsUtil.invert(musicMap)

    // Look for performances in the library where the best performance we have is a "bad" one
    
    println("Looking for bad perfs")
    
    val badPerfs = perfMap.keys filter {
      p => {
        val ms = perfMap.getOrElse(p, Set())
        
        // Only interested when there are highly rated perfs
        
        def isGood(m: MusicFile) : Boolean = {
            val path = m.file.getAbsolutePath()
            return ! (path.contains("/Jerry/") || path.contains("/murat/") || path.contains("/mm/") || path.contains("/ipod/"))
        }

        def isInteresting(m: MusicFile) : Boolean = {
          m.md match {
            case Some(md) => md.rating.getOrElse(0d) >= 0.4
            case _ => false
          }
        }
        
        val interesting = ms filter isInteresting

        val good = ms filter isGood
        
        /*interesting.size > 0 && */ good.size == 0
      }
    } toSet
    
    println("----------------")
    
    println("Found " + badPerfs.size + " bad perfs")
    
    val toMigrateBadQual = disco.performances map { _.rep } filter {
      p =>
        {
          val srces = disco.getEquivSourced(p) map { _.src }
          badPerfs.contains(p.toLibPerformance) &&
            srces.contains(Disco.Gordon) &&
            p.date != null &&
            p.performer != null
        }
    }

    val badPerfsPlan = calculateBetterMigration(toMigrateBadQual)
    
    // -----------------------------
    // All cumparsitas that we don't already have
    
    // Things migrated in phase 1 (already implemented)
    val cumparsitasToMigrate0 = disco.performances map { _.rep } filter {
      p =>
        {
          val srces = disco.getEquivSourced(p) map { _.src }
          srces.contains(Disco.Gordon) &&
            !srces.contains(disco.libSrc) &&
            p.name != null &&
            p.name.toLowerCase().contains("cumparsita") 
        }
    }
    
    val cumparsitasToMigrate = cumparsitasToMigrate0 -- toMigrate
    
    println("Found " + cumparsitasToMigrate.size + " cumparsitas items to migrate")
    
    for (c <- cumparsitasToMigrate) println(c)

    val cumparsitasPlan = calculateBetterMigration(cumparsitasToMigrate)

    // -----------------------------
    
    def calcDestFile(g: GordonTrack): File = {
      val src = g.file
      val srcPath = src.toPath

      val srcRoot = new File("/media/Orange/Gordon").toPath
      val relSrcPath = srcRoot.relativize(srcPath)

      val destRoot = new File("/media/Orange/Music/ogg/dj/Gordon-BA").toPath
      val absDestPath = destRoot.resolve(relSrcPath)

      val destFile0 = absDestPath.toFile()

      val destFile = new File(destFile0.getAbsolutePath().replaceAll(".m4a", ".flac"))
      destFile
    }

    val migrateTargets = (rightPlan.values map { calcDestFile(_) }).toSet

    val alreadyMigrated = {

      def readDir(dir: File): Set[File] = {

        val contents = dir.listFiles()
        val flacs = contents filter { _.getName().endsWith(".flac") }
        val subdirs = contents filter { _.isDirectory() } flatMap { readDir(_) }

        (flacs ++ subdirs).toSet
      }

      readDir(new java.io.File("/media/Orange/Music/ogg/dj/Gordon-BA"))
    }

    println("Right plan has " + migrateTargets.size + " targets")
    println("Already migrated " + alreadyMigrated.size + " files")

    def migrate(m: Performance, plan: Map[Performance, GordonTrack]) : Iterable[File] = {

      Log.info("Migrate " + m)
      
      for (g <- plan.get(m)) yield {
        
        val src = g.file
        val destFile = calcDestFile(g)

        Log.info("  target " + destFile)
        
        val parent = destFile.getParentFile()

        if (!parent.exists() && !parent.mkdirs()) {
          Log.info("Failed to create parent dir for " + destFile)
        } else {
          copy(src, destFile, m)
          destFile
        }
        destFile
      } 
      
    }
    
    def doMigrate(ms: Iterable[Performance], plan: Map[Performance, GordonTrack], logFile: File) {
      val pw = new PrintWriter(new FileWriter(logFile))
      for (p <- ms) {
        val fs = migrate(p, plan)
        for (f <- fs) pw.write(f.getAbsolutePath)
      }
      pw.close()
    }
    
    def checkDeleted(m: Performance, plan: Map[Performance, GordonTrack]) : Boolean = {
      for (g <- plan.get(m)) {
        val src = g.file
        val destFile = calcDestFile(g)

        if (! destFile.exists()) {
          return true
        }
      }
      return false
    }

    val deleted = toMigrate filter { checkDeleted(_, rightPlan) }

    println("No migrate (deleted) : " + deleted.size)

    val phase2Perfs = toMigrate -- deleted -- rightPlan.keys
    
    println("Phase 2 perfs to migrate: " + phase2Perfs.size)
    
    /*
    for (p <- phase2Perfs) {
      migrate(p, betterPlan);
    }
    
    */
    
    // No need to print - the code works fine.
    /*
    for (m <- noMigrate) {
      println("No migrate (deleted) : " + m)
    }
    *
    */
    
    /*
    doMigrate(toMigrateBadQual, badPerfsPlan, new File("bad-perfs-migrate.m3u"))
    */

    doMigrate(cumparsitasToMigrate, cumparsitasPlan, new File("cumparsitas.m3u"))

  }

  def copy(src: File, dest: File, perf: Performance) {

    if (dest.exists()) {
      Log.info("Skipping " + dest)
      return
    }

    val tmp = File.createTempFile("djscala-", ".flac", dest.getParentFile())
    tmp.delete()

    val cmd = List("avconv", "-i", src.getAbsolutePath(), tmp.getAbsolutePath())

    def writeTags() = {
      val lltag = new Lltag(tmp, true)
      lltag.setArtist(perf.performer.toString)
      lltag.setTitle(perf.name)
      lltag.setGenre(perf.genre)
      if (perf.date != null) {
        lltag.setYear(perf.date)
      }
      lltag.write()
      true
    }

    val cmd3 = List("mv", tmp.getAbsolutePath(), dest.getAbsolutePath())

    val okay = exec(cmd) && writeTags() && exec(cmd3)

    Log.info("Result " + okay + " for " + perf + "\n")

    tmp.exists() && tmp.delete()
  }

  def exec(cmd: List[String]): Boolean = {
    Log.info("execute " + cmd.mkString(" "));

    val l = java.util.Arrays.asList(cmd.toArray: _*)
    val b = new ProcessBuilder(l)

    b.inheritIO()

    val p = b.start();
    p.waitFor();

    p.exitValue() == 0
  }

  def exec0(cmd: List[String]): Boolean = {
    Log.info("execute " + cmd.mkString(" "));
    true
  }
  
  def findRoot(f: java.io.File) : java.io.File = {
    if (f.getParent().contains("Tango Organised By Album")) f else findRoot(f.getParentFile())
  } 

}