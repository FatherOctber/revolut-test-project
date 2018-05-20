import com.fatheroctober.dbadapter.utils.SerializationUtil
import com.fatheroctober.moneytransfer.AccountController
import com.fatheroctober.moneytransfer.ioc.Injection
import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import org.slf4j.LoggerFactory

class ScalatraBootstrap extends LifeCycle {
  val logger = LoggerFactory.getLogger(getClass)

  override def init(context: ServletContext) {
    val bootConfig = config()
    val injector = if (bootConfig != null) Injection.injector(config().dbPort) else Injection.injector
    val transfer = injector.getInstance(classOf[AccountController])
    context mount(transfer, transfer.routeAddress())
  }

  def config(): BootConfig = {
    val boot = scala.io.Source.fromURL(getClass.getResource("boot.json"))
    if (boot != null) {
      val bootConfigStr = try boot.mkString finally boot.close()
      SerializationUtil.fromJson[BootConfig](bootConfigStr.getBytes)
    } else null
  }
}