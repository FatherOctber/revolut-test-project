import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object Launcher {
  def main(args: Array[String]) {
    val server = jetty(8080)
    server.start
    server.join
  }

  def jetty(port: Int): Server = {
    val serverPort = if (System.getenv("PORT") != null) System.getenv("PORT").toInt else port

    val server = new Server(serverPort)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)
    server
  }

}