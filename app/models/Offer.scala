package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import scala.util.Random
import play.api.Logger

import java.util.Date

case class Offer(id: Long, origin: String, destination: String, date: Date, price: Option[Double], user: String, isDriver: Boolean) {}

object Offer {
  // creates and stores a new offer
  def create(origin: String, destination: String, date: Date, price: Option[Double], user: String, isDriver: Boolean) {
    try {
      DB.withConnection{ implicit c =>
        val isDriverString = if(isDriver) "t" else "f"
        price match {
          case Some(price) => 
            SQL("INSERT INTO offer (origin, destination, date, price, isDriver, username) VALUES ({origin}, {destination}, {date}, {price}, {isDriver}, {username})").on("origin"->origin, "destination"->destination, "date"->date.getTime(), "price"->price, "isDriver"->isDriverString, "username"->user).executeUpdate()
          case None => 
            SQL("INSERT INTO offer (origin, destination, date, isDriver, username) VALUES ({origin}, {destination}, {date}, {isDriver}, {username})").on("origin"->origin, "destination"->destination, "date"->date.getTime(), "isDriver"->isDriverString, "username"->user).executeUpdate()
        }
      }
    }
    catch {
      case e: Exception =>
        Logger.error("Error while creating an offer: ")
        e.printStackTrace
    }
  }
  
  // anorm parser
  val offer = {
    get[Long]("id") ~
    get[String]("origin") ~
    get[String]("destination") ~
    get[Long]("date") ~
    get[Option[Double]]("price") ~
    get[String]("isDriver") ~
    get[String]("username")  map {
      case id ~ origin ~ destination ~ date ~ price ~ isDriver ~ username => {
        val isDriverBool = if(isDriver=="t") true else false
        Offer(id, origin, destination, new Date(date), price, username, isDriverBool)
      }
    }
  }

  private def preprocessQueryString(query: Option[String]):Option[String] = {
    query match {
      case Some(q) => if(q == "") None else Some(q.toUpperCase)
      case None => None
    }
  }

  def queryAll(origin: Option[String], destination: Option[String]) = DB.withConnection { implicit c =>
    val orig = preprocessQueryString(origin)
    val dest = preprocessQueryString(destination)
    (orig, dest) match {
      case (None, None) => SQL("SELECT * FROM offer").as(offer *)
      case (None, Some(d)) => 
        val likeD = "%" + d + "%"
        SQL("SELECT * FROM offer WHERE UCASE(destination) LIKE {likeD}").on("likeD"->likeD).as(offer *)
      case (Some(o), None) => 
        val likeO = "%" + o + "%"
        SQL("SELECT * FROM offer WHERE UCASE(origin) LIKE {likeO}").on("likeO"->likeO).as(offer *)
      case (Some(o), Some(d)) => 
        val likeO = "%" + o + "%"
        val likeD = "%" + d + "%"
        SQL("SELECT * FROM offer WHERE UCASE(destination) LIKE {likeD} AND UCASE(origin) LIKE {likeO}").on("likeO"->likeO, "likeD"->likeD).as(offer *)
    }
  }

  def queryDriverOffers(isDriver: Boolean, origin: Option[String], destination: Option[String]) = DB.withConnection { implicit c =>
    val orig = preprocessQueryString(origin)
    val dest = preprocessQueryString(destination)
    val driver = if(isDriver) "t" else "f"
    (orig, dest) match {
      case (None, None) => SQL("SELECT * FROM offer WHERE isDriver={driver}").on("driver"->driver).as(offer *)
      case (None, Some(d)) => 
        val likeD = "%" + d + "%"
        SQL("SELECT * FROM offer WHERE isDriver={driver} AND UCASE(destination) LIKE {likeD}").on("driver"->driver, "likeD"->likeD).as(offer *)
      case (Some(o), None) => 
        val likeO = "%" + o + "%"
        SQL("SELECT * FROM offer WHERE isDriver={driver} AND UCASE(origin) LIKE {likeO}").on("driver"->driver, "likeO"->likeO).as(offer *)
      case (Some(o), Some(d)) => 
        val likeO = "%" + o + "%"
        val likeD = "%" + d + "%"
        SQL("SELECT * FROM offer WHERE isDriver={driver} AND UCASE(destination) LIKE {likeD} AND UCASE(origin) LIKE {likeO}").on("driver"->driver, "likeO"->likeO, "likeD"->likeD).as(offer *)
    }
  }
}
