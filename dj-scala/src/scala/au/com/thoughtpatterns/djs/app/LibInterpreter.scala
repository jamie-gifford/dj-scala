package au.com.thoughtpatterns.djs.app

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.ILoop
import au.com.thoughtpatterns.djs.lib.Library
import au.com.thoughtpatterns.djs.util.Log
import java.io.File
import au.com.thoughtpatterns.djs.clementine.Player
import au.com.thoughtpatterns.djs.clementine.PlayerInterfaceFactory
import java.io.BufferedReader
import java.io.PrintWriter
import au.com.thoughtpatterns.djs.webapp.DJSession
import au.com.thoughtpatterns.djs.webapp.Desktop

class LibInterpreter(val library: Library, val input: Option[BufferedReader], val output: PrintWriter)
  extends ILoop(input, output) {

  def this(library: Library) = this(library, None, new PrintWriter(Console.out, true))

  var bindings: Seq[(String, String, Any)] = IndexedSeq()

  def bindVal[T: Manifest](name: String, value: T) {
    bindings :+= (name, manifest[T].toString, value)
  }

  def applyBindings() = {
    for ((bname, btype, bval) <- bindings) {
      // Log.info("Apply " + bname + ", " + btype)
      intp.beQuietDuring(intp.bind(bname, btype, bval))
    }
  }

  def applyBindingsWait() {
    // Forces wait for globalFuture
    processLine("print(\"Applying bindings\\n\");")
    applyBindings();
  }
  
  def run() {
    val settings = new Settings()
    settings.usejavacp.value = true
    settings.Yreplsync.value = true

    settings.embeddedDefaults[Library]

    bindVal("l", library)
    bindVal("m", library.music)
    bindVal("q", new Player(library))
    
    bindVal("audacious", PlayerInterfaceFactory.useAudacious _);
    bindVal("clementine", PlayerInterfaceFactory.useClementine _);

    def runOperator(index: Int, opname: String) {
      DJSession.session.desk.runOperator(index, opname)
    }

    bindVal("runOperator", runOperator _);

    def clearDesktop() { DJSession.session.desk = new Desktop(None, None) }

    bindVal("clearDesktop", clearDesktop _);

    LibInterpreter.interpreter = this

    process(settings)

    Log.info("Finished shell")
  }

  override def printWelcome() {
    applyBindingsWait();
    for (cmd <- library.getInitCommands) {
      Log.info("INIT: " + cmd)
      command(cmd)
    }

    echo("--- Started DJ interpreter, type l.help for help ---")
  }
  
}

object LibInterpreter {

  var interpreter: LibInterpreter = null

  var accum: Option[Any] = None

}