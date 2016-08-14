package au.com.thoughtpatterns.djs.lib

import java.io.File
import java.nio.file.Path

import au.com.thoughtpatterns.djs.util.Log

/**
 * A file on the filesystem that relates to music.
 */
@SerialVersionUID(1L)
trait MusicContainer extends Serializable {

  def file: File
  
  def lib : Library

  var lastRead: Long = 0

  private var readDirty: Boolean = true

  def fileAge = if (file.exists) file.lastModified() else 0

  def check() = {
    // Add 1000 to handle NTFS
    readDirty = readDirty || fileAge > lastRead + 1000 || fileAge == 0 
    readDirty
  }
  
  def exists = file.exists

  def setReadDirty(flag: Boolean) {
    readDirty = flag
  }

  def read()

  def update() = {
    //Log.info("update " + file + "; fileAge = " + fileAge + ", lastRead = " + lastRead + ", diff = " + (fileAge - lastRead))
    read()
    lib.markDirty()
    lastRead = fileAge
    readDirty = false
  }

  def updateIfDirty() {
    if (!readDirty) {
      return ;
    }
    read()
    lib.markDirty()
  }
  
  def toJson: String = ""

  // -----------------------
  // Operations

  def replicate(strategy: ReplicationStrategy) {}
  
  def deleteMdFile() {}
  
}

