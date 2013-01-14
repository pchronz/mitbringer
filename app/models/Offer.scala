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

  def getAll() = DB.withConnection { implicit c =>
    SQL("SELECT * FROM offer").as(offer *)
  }
}
