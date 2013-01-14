package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import scala.util.Random
import play.api.Logger

case class User(username: String, password: String, email: String) {}

object User {
  // creates and stores a new user
  def create(username: String, password: String, email: String): Option[User] = {
    try {
      DB.withConnection{ implicit c =>
        SQL("INSERT INTO usar (username, password, email) VALUES ({username}, {password}, {email})").on("username"->username, "password"->password, "email"->email).executeUpdate()
        Some(User(username, password, email))
      }
    }
    catch {
      case e: Exception =>
        Logger.error("Error while creating a user: " + username + "/" + password + "/" + email)
        e.printStackTrace
        return None
    }
  }
  
  // anorm parser
  val user = {
    get[String]("username") ~
    get[String]("password") ~
    get[String]("email") map {
      case username ~ password ~ email => {
        User(username, password, email)
      }
    }
  }

  def authenticate(username: String, password: String) = DB.withConnection { implicit c =>
    val users = SQL("SELECT * FROM usar WHERE username={username} AND password={password}").on("username"->username, "password"->password).as(user *)
    users.length  match {
      case 0 => false
      case 1 => true
      case _ => 
        Logger.error("Found multiple users with identical username and password: " + username + "/" + password)
        true
    }
  }
}
