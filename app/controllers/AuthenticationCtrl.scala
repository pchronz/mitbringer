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

import models.User

// mailer plugin
import com.typesafe.plugin._
import play.api.Play.current

// create an email service
object AuthenticationCtrl extends Controller {
  def authenticate(username: String, password: String) = Action {
    Logger.info("Authenticatation for " + username + " " + password)
    if(User.authenticate(username, password)) {
      Logger.info("Authentication successful")
      Ok
    }
    else {
      Logger.info("Authentication failed")
      Unauthorized
    }
  }

  private def sendRegistrationEmail(username: String, email: String) = {
    Logger.info("Sending out registration email to " + username + "/" + email)
    val mail = use[MailerPlugin].email
    mail.setSubject("Die Mitbringer Registrierung")
    mail.addRecipient(username + " <" + email + ">")
    mail.addBcc("peter.chronz@gmail.com", "daniel@musikerchannel.de", "tybytyby@gmail.com")
    mail.addFrom("Die Mitbringer <die.mitbringer@gmail.com>")
    //sends text/text
    mail.send( "Hi " + username + ",\n\nWir freuen uns dich begruessen zu duerfen. Um dein Profil zu aktivieren, klicke bitte auf folgenden Link:\n\nhttp://www.die-mitbringer.de\n\nViele Gruesse,\nDie Mitbringer\n\nPS: Falls du dich nicht bei Die Mitbringer registriert hast, ignoriere bitte diese Nachricht und klicke nicht auf den obigen Link\n\n")
    Logger.info("Email to " + username + "/" + email + " sent")
  }

  def register(username: String, password: String, email: String) = Action {
    Logger.info("New user registering: " + username + "/" + password + "/" + email)
    User.create(username, password, email) match {
      case Some(user) => 
        sendRegistrationEmail(username, email)
        Ok
      case None => BadRequest
    }
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
          if(User.authenticate(username, password)) Some(username)
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


