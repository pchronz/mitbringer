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


object Offer extends Controller {

  def queryOffers = Action {
    Logger.info("Querying offers")
    val o1 = Map(
      "id"->"1",
      "origin"->"Ikea Kassel",
      "destination"->"Goettingen",
      "date"-> new Date().getTime.toString,
      "price"->"5",
      "user"->"fluppe67",
      "isDriver"->"1"
    )
    val o2 = Map(
      "id"->"2",
      "origin"->"Castrop",
      "destination"->"Kassel",
      "date"-> new Date().getTime.toString,
      "price"->"5",
      "user"->"roberta13",
      "isDriver"->"0"
    )
    val offers = List(o1, o2)
    Ok(stringify(toJson(offers)))
  }

  def createOffer(origin: String, destination: String, date: String, price: String, isDriver: Boolean) = Authenticated { authRequest =>
    Logger.info("Create offer with " + origin + " " + destination + " " + date + " " + price + " isDriver==" + isDriver)
    Ok
  }
  
}
