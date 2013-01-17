package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import scala.util.Random
import play.api.Logger

// hashing
import com.roundeights.hasher.Implicits._

case class User(username: String, password: String, email: String) {}

object User {
  private def saltAndHash(username: String, password: String): String = {
    password.salt(username).bcrypt
  } 
  // creates and stores a new user
  def create(username: String, password: String, email: String, confirmationKey: String): Option[User] = {
    try {
      val securePassword = saltAndHash(username, password) 
      DB.withConnection{ implicit c =>
        SQL("INSERT INTO usar_unconfirmed (username, password, email, confirmationKey) VALUES ({username}, {password}, {email}, {confirmationKey})").on("username"->username, "password"->securePassword, "email"->email, "confirmationKey"->confirmationKey).executeUpdate()
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

  def activate(confirmationKey: String) = {
    DB.withConnection{ implicit c =>
      def activateUser(users: List[User], confirmationkey: String) {
        val u = users(0)
        SQL("INSERT INTO usar (username, password, email) VALUES ({username}, {password}, {email})").on("username"->u.username, "password"->u.password, "email"->u.email).executeUpdate()
        SQL("DELETE FROM usar_unconfirmed WHERE confirmationKey={confirmationKey}").on("confirmationKey"->confirmationKey).executeUpdate()
      }
      val users = SQL("SELECT username, password, email FROM usar_unconfirmed WHERE confirmationKey={confirmationKey}").on("confirmationKey"->confirmationKey).as(user *)
      users.length match {
        case 0 => false
        case 1 => 
          activateUser(users, confirmationKey)
          true
        case _ =>
          Logger.warn("Found multiple users with identical confirmation key")
          activateUser(users, confirmationKey)
          true
      }
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
    val users = SQL("SELECT * FROM usar WHERE username={username}").on("username"->username, "password"->password).as(user *)
    users.length  match {
      case 0 => false
      case _ => 
        password.salt(username) bcrypt= users(0).password 
    }
  }

  def getAll() = DB.withConnection { implicit c =>
    SQL("SELECT * FROM usar").as(user *)
  }

  def getByUsername(username: String) = DB.withConnection { implicit c =>
    SQL("SELECT * FROM usar WHERE username={username}").on("username"->username).as(user *)
  }

  def resetPassword(username: String, email: String):Option[String] = DB.withConnection { implicit c =>
    def generateRandomString(): String = {
      val random = new Random()
      val chars = List('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
      (1 to 6).map{i => val num = random.nextInt(chars.length); chars(num)}.mkString("")
    }
    val users = SQL("SELECT * FROM usar WHERE username={username} AND email={email}").on("username"->username, "email"->email).as(user *)
    users.length match {
      case 0 => None
      case _ => 
        val newPassword = generateRandomString
        Logger.debug("New password: " + newPassword)
        val newSecurePassword = saltAndHash(username, newPassword)
        Logger.debug("New secure password: " + newSecurePassword)
        SQL("UPDATE usar SET password={newSecurePassword} WHERE username={username}").on("newSecurePassword"->newSecurePassword, "username"->username).executeUpdate()
        Some(newPassword)
    }
  }
  def changePassword(username: String, newPassword: String):Boolean = DB.withConnection { implicit c =>
    try {
      val newSecurePassword = saltAndHash(username, newPassword)
      Logger.debug("New secure password: " + newSecurePassword)
      SQL("UPDATE usar SET password={newSecurePassword} WHERE username={username}").on("newSecurePassword"->newSecurePassword, "username"->username).executeUpdate()
      true
    }
    catch {
      case e: Exception => 
        Logger.error("Error while setting a new password for user: " + username)
        e.printStackTrace()
        false
    }
  }
}
