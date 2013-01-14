import play.api._
import models._

import java.util.Date

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application starting!")
    User.getAll.length match {
      case 0 =>
        User.create("jimbo", "jimbo", "j@im.bo")
        User.create("jones", "jones", "j@on.es")

        Offer.create("Ikea Kassel", "Goettingen", new Date(), Some(5), "jimbo", true)
        Offer.create("Berlin", "Goettingen", new Date(), None, "jimbo", false)
        Message.create("jimbo", "jones", new Date(), 1, "unread", "Hi Jones\n, Kannst Du mir etwas aus Berlin mitbringen?\nJimbo")
      case _ => 
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application terminated!")
  }  
    
}

