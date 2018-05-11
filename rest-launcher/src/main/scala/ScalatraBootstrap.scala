import com.revolut.testproject.moneytransfer.MoneyTransferController
import javax.servlet.ServletContext
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context mount(new MoneyTransferController, "/money-transfers/*")
  }
}