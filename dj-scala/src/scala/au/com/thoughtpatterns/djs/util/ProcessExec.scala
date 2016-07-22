package au.com.thoughtpatterns.djs.util

import java.io.InputStream
import java.io.InputStreamReader

object ProcessExec {

  case class ProcessResult(stdout: String, resultCode: Integer)
  
  def exec(cmd: List[String]) : ProcessResult = {
    
    Log.info("execute " + cmd.mkString(" "));
        
    val l = java.util.Arrays.asList(cmd.toArray: _*)
    val b = new ProcessBuilder(l)

    val p = b.start();
    
    val resultCode = p.waitFor();
    
    def slurp(in: InputStream) : String = {
      val x = io.Source.fromInputStream(in)
      x.getLines().mkString("\n")
    }

    val stdout = slurp(p.getInputStream())
    val stderr = slurp(p.getErrorStream())
    
    Log.info("... result code " + resultCode)
    
    if (resultCode != 0) {
      Log.info("Failed: " + stderr)
    }
    
    ProcessResult(stdout, resultCode)
  }
  
}