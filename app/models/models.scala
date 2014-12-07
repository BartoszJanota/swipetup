package models

import com.novus.salat.annotations.Key
import models.mongoContext._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}
import util.SalatDAOWithCfg


case class CategoryResults(results: List[Category])

object CategoryResults {
  def generate(results: Option[List[Category]], total: Option[Int]) =
    CategoryResults(results = results.getOrElse(List()))
}

trait CategoryResultsParser extends CategoryParser {
  implicit val categoryResultsRead: Reads[CategoryResults] = (
    (JsPath \ "results").readNullable[List[Category]] and
      (JsPath \ "total").readNullable[Int]
    )(CategoryResults.generate _)
}

case class Category(id: Int, name: String)

object Category {
  def generate(name: Option[String],
               id: Option[Int]) =
    Category(name = name.getOrElse("undefined"), id = id.getOrElse(0))
}

trait CategoryParser {
  implicit val categoryReads: Reads[Category] = (
    (JsPath \ "name").readNullable[String] and
      (JsPath \ "id").readNullable[Int]
    )(Category.generate _)
}

case class User(
                 @Key("_id") name: String,
                 status: String
                 )

object User {
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

object UserPreference {

  def defaultUserPreference = UserPreference("undefined", "Kraków", List(), "Java is default!")

  def apply(user: User, searchData: SearchData): UserPreference = {
    UserPreference(userName = user.name, city = searchData.city, category = searchData.category, text = searchData.text)
  }
}

case class UserPreference(
                           @Key("_id") userName: String,
                           city: String = "",
                           category: List[String] = List(),
                           text: String = ""
                           )

object UserPreferenceDAO extends SalatDAOWithCfg[UserPreference, String]("app.mongo.uri", "swipetup_users_preference")