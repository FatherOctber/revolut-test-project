import com.revolut.testproject.ioc.Injection
import com.revolut.testproject.moneytransfer.AccountController
import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import org.slf4j.LoggerFactory

class ScalatraBootstrap extends LifeCycle {
  val logger = LoggerFactory.getLogger(getClass)

  override def init(context: ServletContext) {
    val injector = Injection.injector
    val transfer = injector.getInstance(classOf[AccountController])
    context mount(transfer, transfer.routeAddress())
  }
}