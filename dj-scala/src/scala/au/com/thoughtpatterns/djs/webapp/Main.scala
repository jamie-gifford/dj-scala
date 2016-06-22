package au.com.thoughtpatterns.djs.webapp

import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.Server
import org.mortbay.jetty.webapp.WebAppContext
import org.mortbay.jetty.servlet.AbstractSessionManager
import au.com.thoughtpatterns.djs.lib.Library

object Main extends Runnable {

  def main(args: Array[String]) = {
    run()
  }

  def run() {

    val server = new Server()

    val connector = new SelectChannelConnector()
    connector.setPort(8082);

    val cs = List(connector)

    server.setConnectors(cs.toArray);

    val web = new WebAppContext();
    web.setContextPath("/");
    web.setWar("src/webapp");

    server.addHandler(web);

    val manager = web.getSessionHandler().getSessionManager();
    if (manager.isInstanceOf[AbstractSessionManager]) {
      manager.asInstanceOf[AbstractSessionManager].setUsingCookies(false);
    }

    server.start();
    server.join();
  }

  def runInThread() {
    new Thread(this).start()
  }

}