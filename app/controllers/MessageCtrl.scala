package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent._
import play.api.Play.current

// Scala, Java imports
import scala.collection.JavaConversions._
import java.util.Date

import models.Message
import models.User

// mailer plugin
import com.typesafe.plugin._
import play.api.Play.current

// TODO create an email service
object MessageCtrl extends Controller {

  implicit def messagesToJson(messages: List[Message]): JsValue = {
    toJson(messages.map(messageToJson(_)))
  }

  implicit def messageToJson(message: Message): JsValue = {
    val messageMap = Map(
      "id"->toJson(message.id),
      "originUser"->toJson(message.originUser),
      "destinationUser"->toJson(message.destinationUser),
      "date"->toJson(message.date.getTime),
      "offerId"->toJson(message.offer),
      "state"->toJson(message.state),
      "content"->toJson(message.content)
    )
    toJson(messageMap)
  }
  def queryMessages(box: String) = Authenticated { authRequest =>
    Logger.info("Querying messages for user: " + authRequest.username)
    val messages = box match {
      case "received" => Message.getAllToUser(authRequest.username)
      case "sent" => Message.getAllByUser(authRequest.username)
      case _ => throw new Exception(authRequest.username + "is trying to query non-existent mailbox: " + box)
    }
    Logger.debug(messages.mkString)
    Logger.debug(stringify(toJson(messagesToJson(messages))))
    Ok(stringify(toJson(messagesToJson(messages))))
  }

  private def sendMessageNotificationEmail(destinationUser: String, originUser: String) {
    Logger.info("Sending out message notification email to " + destinationUser)
    val user = User.getByUsername(destinationUser)(0)
    val mail = use[MailerPlugin].email
    mail.setSubject("Die Mitbringer Benachrichtigung")
    mail.addRecipient(user.username + " <" + user.email + ">")
    mail.addBcc("peter.chronz@gmail.com", "tybytyby@gmail.com", "daniel@musikerchannel.de")
    mail.addFrom("Die Mitbringer <die.mitbringer@die-mitbringer.de>")
    //sends text/text
    mail.send( "Hi " + user.username + ",\n\nDu hast eine neue Nachricht von " + originUser + " auf http://www.die-mitbringer.de erhalten!\n\nViele Gruesse,\nDie Mitbringer\n\n")
    Logger.info("Email to " + user.username + "/" + user.email + " sent")
  }
  
  def sendMessage(content: String, offerId: String, destinationUser: String) = Authenticated { authRequest =>
    Logger.info("Sending message with " + content + " " + offerId + " " + destinationUser + " " + authRequest.username)
    Message.create(authRequest.username, destinationUser, new Date(), offerId.toLong, "unread", content)
    sendMessageNotificationEmail(destinationUser, authRequest.username)
    Ok
  }
  
}
