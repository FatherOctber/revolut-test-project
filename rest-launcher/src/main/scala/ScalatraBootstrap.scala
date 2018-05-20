import com.fatheroctober.dbadapter.utils.SerializationUtil
import com.fatheroctober.moneytransfer.AccountController
import com.fatheroctober.moneytransfer.ioc.{Injection, ShutdownResource}
import com.google.inject.Injector
import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import org.slf4j.LoggerFactory

class ScalatraBootstrap extends LifeCycle {
  val logger = LoggerFactory.getLogger(getClass)
  private var injector: Injector = null

  override def init(context: ServletContext) {
    val bootConfig = config()
    injector = if (bootConfig != null) Injection.injector(config().dbPort) else Injection.injector
    val transfer = injector.getInstance(classOf[AccountController])
    context mount(transfer, transfer.routeAddress())
  }

  override def destroy(context: ServletContext): Unit = {
    val shutdowner = injector.getInstance(classOf[ShutdownResource])
    shutdowner.shutdown()
  }

  def config(): BootConfig = {
    val boot = scala.io.Source.fromURL(getClass.getResource("boot.json"))
    if (boot != null) {
      val bootConfigStr = try boot.mkString finally boot.close()
      SerializationUtil.fromJson[BootConfig](bootConfigStr.getBytes)
    } else null
  }
}