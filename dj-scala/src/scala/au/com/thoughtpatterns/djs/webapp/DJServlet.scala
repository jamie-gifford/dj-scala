package au.com.thoughtpatterns.djs.webapp

import au.com.thoughtpatterns.djs.app.LibInterpreter
import au.com.thoughtpatterns.djs.util.Log
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServlet
import au.com.thoughtpatterns.djs.lib.Managed
import au.com.thoughtpatterns.djs.lib.ManagedContainers
import au.com.thoughtpatterns.djs.lib.DesktopCompat
import scala.collection.mutable.MutableList

class DJServlet extends HttpServlet {

  override def service(request: ServletRequest, response: ServletResponse) {

    response.setCharacterEncoding("UTF-8")
    response.setContentType("application/json")
    
    val w = response.getWriter()

    val expr = request.getParameter("expr")

    if (expr != null && LibInterpreter.interpreter != null) {
      val cmd = expr
      Log.info("Command: " + cmd)
      val r = LibInterpreter.interpreter.command(cmd)
      Log.info("Result: " + r)
    }

    val json = DJSession.session.desk.toJson

    w.write(json)

    w.close()

  }

}

case class DeskObject(index: Int, m: DesktopCompat)

class Desktop(m: Option[DesktopCompat], prev: Option[Desktop], linkTo: Set[DesktopCompat] = Set()) {

  val index: Int = if (prev.isDefined) prev.get.index + 1 else -1

  private val added = m map { DeskObject(index, _) }
  
  val reverseMap : Map[DesktopCompat, DeskObject] = {
    var tmp = scala.collection.mutable.Map[DesktopCompat, DeskObject]()
    for (d <- prev) {
      tmp = tmp ++ d.reverseMap
    }
    for (c <- m; a <- added) {
      tmp += ( c -> a )
    }
    tmp.toMap
  }
  
  val links: List[Pair[Int, Int]] = {
    var tmp = MutableList[Pair[Int, Int]]()
    for (d <- prev) {
      tmp = tmp ++ d.links
    }
    for (l <- linkTo; x <- reverseMap.get(l)) {
      tmp += Pair(index, x.index)
    }
    tmp.toList
  }
  
  val objects: List[DeskObject] = {
    var tmp = MutableList[DeskObject]()
    for (d <- prev) {
      tmp = tmp ++ d.objects
    }
    for (a <- added) {
      tmp += a
    }
    tmp.toList
  }

  def toJson: String = {

    val list = for (o <- objects) yield o.m.toDeskJson(o.index)
    
    val nodes = list.mkString(",\n")

    val lks = (for ((x, y) <- links) yield { "{\"source\": " + x + ", \"target\": " + y + ", \"value\": 1}" }).mkString(",")
    
    s"""
      | {
      |  "nodes":[
      |   $nodes
      |  ],
      |  "links": [
      |   $lks
      |  ]
      | }
      |""".stripMargin

  }

  def runOperator(index : Int, opName : String) {
    for (o <- objects if o.index == index) {
      o.m.execute(opName)
    }
  }
  
}

class DJSession {

  var desk = new Desktop(None, None)

}

object DJSession {

  val session = new DJSession

}