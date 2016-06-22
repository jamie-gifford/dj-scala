package au.com.thoughtpatterns.djs.util

object Log {

  def info(str: String) {
    println(str)
  }
  
  def error(ex: Throwable) {
    ex.printStackTrace()
  }
  
}