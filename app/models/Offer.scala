package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import scala.util.Random

case class Offer(id: String, origin: String, destination: String, date: Long, price: Integer, user: String, isDriver: Boolean) {}

object Offer {
  def apply(origin: String, destination: String, date: Long, price: Integer, user: String, isDriver: Boolean) = {
  }
}
