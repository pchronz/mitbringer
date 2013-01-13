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


object Message extends Controller {

  def queryMessages = Authenticated { authRequest =>
    val m1 = Map(
      "id"->"1",
      "originUser"->"bingolero",
      "date"-> new Date().getTime.toString,
      "offerId"->"1",
      "state"->"unread",
      "content"->"Hi,\nKannst Du mir etwas mitbringen?\nBL"
    )
    val m2 = Map(
      "id"->"2",
      "originUser"->"roberta",
      "date"-> new Date().getTime.toString,
      "offerId"->"1",
      "state"->"read",
      "content"->"Bring mir mal was mit"
    )
    val messages = List(m1, m2)
    Logger.info(stringify(toJson(messages)))
    Ok(stringify(toJson(messages)))
  }
  
  def sendMessage(content: String, offerId: String, destinationUser: String, originUser: String) = Authenticated { authRequest =>
    Logger.info("Sending message with " + content + " " + offerId + " " + destinationUser + " " + originUser)
    Ok
  }
  
}
