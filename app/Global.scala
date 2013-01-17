import play.api._
import models._

import java.util.Date

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application starting!")
    User.getAll.length match {
      case 0 =>
        User.create("daniel", "daniel",  "daniel@musikerchannel.de", "A")
        User.activate("A")
        User.create("tibor", "tibor", "tybytyby@gmail.com", "B")
        User.activate("B")

        Offer.create("Ikea Kassel", "Goettingen", new Date(), Some(5), "tibor", true)
        Offer.create("Berlin", "Goettingen", new Date(), None, "daniel", false)
        Message.create("daniel", "tibor", new Date(), 1, "unread", "Hi Jones\n, Kannst Du mir etwas aus Berlin mitbringen?\nJimbo")
      case _ => 
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application terminated!")
  }  
    
}

