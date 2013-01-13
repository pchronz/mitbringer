package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Results._

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent._
import play.api.Play.current

// Scala, Java imports
import scala.collection.JavaConversions._
import java.util.Date


object Authentication extends Controller {
  def authenticate(username: String, password: String) = Action {
    Logger.info("Authenticatation for " + username + " " + password)
    if(username == password) {
      Logger.info("Authentication successful")
      Ok
    }
    else {
      Logger.info("Authentication failed")
      Unauthorized
    }
  }

  def register(username: String, password: String, email: String) = Action {
    Logger.info("New user registering: " + username + "/" + password + "/" + email)
    Ok
  }
}

object Authenticated {
    val authenticationForm = Form(
      tuple(
        "username"->text,
        "password"->text
      )
    )
    def authenticate[T](implicit request: Request[T]): Option[String] = {
        def auth(username: String, password: String) = {
          if(username == password) Some(username)
          else None
        }
        try {
            val (username, password) = authenticationForm.bindFromRequest.get
            Logger.info("User credentials: " + username + " " + password)
            auth(username, password)
        }
        catch {
          case e =>
            Logger.info("Could not authenticate")
            None
        }
    }

    def apply[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result) = Action(p) { implicit request =>
        Logger.info("Authenticating for request " + request.toString)
        authenticate match {
          case Some(username) => f(AuthenticatedRequest(username, request))
          case None => Unauthorized
        }
    }

    def apply(f: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] = {
        Authenticated(BodyParsers.parse.anyContent)(f) 
    }

}

case class AuthenticatedRequest[A](username: String, request: Request[A]) extends WrappedRequest(request) 


