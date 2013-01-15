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

import models.Offer


object OfferCtrl extends Controller {

  implicit def offersToJson(offers: List[Offer]): JsValue = {
    toJson(offers.map(offerToJson(_)))
  }

  implicit def offerToJson(offer: Offer): JsValue = {
    val offerMap = Map(
      "id"->toJson(offer.id),
      "origin"->toJson(offer.origin),
      "destination"->toJson(offer.destination),
      "date"->toJson(offer.date.getTime),
      "price"->toJson(offer.price),
      "user"->toJson(offer.user),
      "isDriver"->toJson(offer.isDriver)
    )
    toJson(offerMap)
  }

  def queryOffers(isDriver: Option[Boolean], origin: Option[String], destination: Option[String]) = Action {
    Logger.info("Querying all offers: isDriver=" + isDriver + " origin=" + origin + " destination=" + destination)
    val offers = isDriver match {
      case None => Offer.queryAll(origin, destination)
      case Some(isDrivr) => Offer.queryDriverOffers(isDrivr, origin, destination)
    }
    
    Ok(stringify(offers))
  }

  def createOffer(origin: String, destination: String, date: String, price: String, isDriver: Boolean) = Authenticated { authRequest =>
    Logger.info("Create offer with " + origin + " " + destination + " " + date + " " + price + " isDriver==" + isDriver)
    val priceVal = try {
      Some(price.toDouble)
    }
    catch {
      case _ => None
    }

    Offer.create(origin, destination, new Date(date.toLong), priceVal, authRequest.username, isDriver)
    Ok
  }
  
}
