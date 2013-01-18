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
import java.util.Random

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

  private def sendRegistrationEmail(username: String, email: String, confirmationKey: String) = {
    Logger.info("Sending out registration email to " + username + "/" + email)
    val mail = use[MailerPlugin].email
    mail.setSubject("Die Mitbringer Registrierung")
    mail.addRecipient(username + " <" + email + ">")
    mail.addBcc("peter.chronz@gmail.com", "daniel@musikerchannel.de", "tybytyby@gmail.com")
    mail.addFrom("Die Mitbringer <die.mitbringer@die-mitbringer.de>")
    //sends text/text
    mail.send( "Hi " + username + ",\n\nWir freuen uns dich begruessen zu duerfen. Um dein Profil zu aktivieren, klicke bitte auf folgenden Link:\n\nhttp://www.die-mitbringer.de/logins/activation/" + confirmationKey + "\n\nViele Gruesse,\nDie Mitbringer\n\nPS: Falls du dich nicht bei Die Mitbringer registriert hast, ignoriere bitte diese Nachricht und klicke nicht auf den obigen Link\n\n")
    Logger.info("Email to " + username + "/" + email + " sent")
  }

  def register(username: String, password: String, email: String) = Action {
    def generateConfirmationKey(): String = {
      val random = new Random()
      val chars = List('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
      (1 to 32).map{i => val num = random.nextInt(chars.length); chars(num)}.mkString("")
    }
    Logger.info("New user registering: " + username + "/" + password + "/" + email)
    val confirmationKey = generateConfirmationKey
    User.create(username, password, email, confirmationKey) match {
      case Some(user) => 
        sendRegistrationEmail(username, email, confirmationKey)
        Ok
      case None => BadRequest
    }
  }

  def activateUser(confirmationKey: String) = Action {
    Logger.info("Activating user")
    if(User.activate(confirmationKey)) Redirect("/justActivated")
    else Redirect("/failedActivation")
  }

  def resetPassword(username: String, email: String) = Action {
    Logger.info("Resetting password for " + username + "/" + email)
    User.resetPassword(username, email) match {
      case Some(password) =>
        sendPasswordResetMail(username, email, password)
        Ok
      case None => BadRequest
    }
  }

  def changePassword(newPassword: String) = Authenticated { implicit authRequest =>
    Logger.info("Changing password for " + authRequest.username)
    if(User.changePassword(authRequest.username, newPassword)) Ok
    else BadRequest
  }

  private def sendPasswordResetMail(username: String, email: String, newPassword: String) = {
    Logger.info("Sending out new password email to " + username + "/" + email)
    val mail = use[MailerPlugin].email
    mail.setSubject("Die Mitbringer Passwort")
    mail.addRecipient(username + " <" + email + ">")
    mail.addFrom("Die Mitbringer <die.mitbringer@die-mitbringer.de>")
    //sends text/text
    mail.send( "Hi " + username + ",\n\nJemand hat dein Passwort zurueckgesetzt. Dein neues Passwort lautet:\n\n" + newPassword + "\n\nBitte logge dich moeglichst bald neu ein und aendere dieses Passwort.\n\nViele Gruesse,\nDie Mitbringer\n")
    Logger.info("Email to " + username + "/" + email + " sent")
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


