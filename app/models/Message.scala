package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import scala.util.Random
import play.api.Logger

import java.util.Date

case class Message(id: Long, originUser: String, destinationUser: String, date: Date, offer: Long, state: String, content: String) {}

object Message {
  // creates and stores a new message
  def create(originUser: String, destinationUser: String, date: Date, offer: Long, state: String, content: String) {
    try {
      DB.withConnection{ implicit c =>
      SQL("INSERT INTO message (originUser, destinationUser, date, offer, state, content) VALUES ({originUser}, {destinationUser}, {date}, {offer}, {state}, {content})").on("originUser"->originUser, "destinationUser"->destinationUser, "date"->date.getTime, "offer"->offer, "state"->state, "content"->content).executeUpdate()
      }
    }
    catch {
      case e: Exception =>
        Logger.error("Error while creating a message: ")
        e.printStackTrace
    }
  }
  
  // anorm parser
  val message = {
    get[Long]("id") ~
    get[String]("originUser") ~
    get[String]("destinationUser") ~
    get[Long]("date") ~
    get[Long]("offer") ~
    get[String]("state") ~
    get[String]("content") map {
      case id ~ originUser ~ destinationUser ~ date ~ offer ~ state ~ content => {
        Message(id, originUser, destinationUser, new Date(date), offer, state, content)
      }
    }
  }

  def getAll = DB.withConnection { implicit c =>
    SQL("SELECT * FROM message").as(message *)
  }
}

