package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
    Logger.info("Redirecting user to JavaScript client")
    Redirect("/assets/webclient/app/index.html")
  }

  def justActivated = Action {
    Logger.info("Redirecting user to JavaScript client @ justActivated")
    Redirect("/assets/webclient/app/index.html#/justActivated")
  }

  def failedActivation = Action {
    Logger.info("Redirecting user to JavaScript client @ failedActivation")
    Redirect("/assets/webclient/app/index.html#/failedActivation")
  }
  
}
