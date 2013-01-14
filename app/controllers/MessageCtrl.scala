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
  def queryMessages = Authenticated { authRequest =>
    Logger.info("Querying messages")
    val messages = Message.getAll
    Logger.debug(messages.mkString)
    Logger.debug(stringify(toJson(messagesToJson(messages))))
    Ok(stringify(toJson(messagesToJson(messages))))
  }
  
  def sendMessage(content: String, offerId: String, destinationUser: String) = Authenticated { authRequest =>
    Logger.info("Sending message with " + content + " " + offerId + " " + destinationUser + " " + authRequest.username)
    Message.create(authRequest.username, destinationUser, new Date(), offerId.toLong, "unread", content)
    Ok
  }
  
}
