package models

import play.api.libs.json.{Reads, JsPath}
import play.api.libs.functional.syntax._
import com.novus.salat.annotations.Key
import util.SalatDAOWithCfg
import models.mongoContext._


case class User(
                 @Key("_id") name: String,
                  status: String
                  )

object User extends{
  def generate(name: Option[String],
               status: Option[String]) =
    User(
      name = name.getOrElse("undefined"),
      status = status.getOrElse("inactive")
    )
}

trait UserParser {
  implicit val userReads: Reads[User] = (
    (JsPath \ "name").readNullable[String] and
      (JsPath \ "status").readNullable[String]
    )(User.generate _)

}

object UserDAO extends SalatDAOWithCfg[User, String]("app.mongo.uri", "swipetup_users")

