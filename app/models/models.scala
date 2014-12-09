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
                 status: String,
                 hasPhoto: Boolean
                 )

object User {
  def generate(name: Option[String],
               status: Option[String],
               photo: Option[Photo]) =
    User(
      name = name.getOrElse("undefined"),
      status = status.getOrElse("inactive"),
      hasPhoto = photo.isDefined
    )
}

trait UserParser extends PhotoParser {
  implicit val userReads: Reads[User] = (
    (JsPath \ "name").readNullable[String] and
      (JsPath \ "status").readNullable[String] and
      (JsPath \ "photo").readNullable[Photo]
    )(User.generate _)
}

case class Photo(photoLink: String, highresLink: String, thumbLink: String, photoId: Long)

object Photo {
  def generate(photoLink: Option[String],
               highresLink: Option[String],
               thumbLink: Option[String],
               photoId: Option[Long]) =
    Photo(
      photoLink = photoLink.getOrElse(""),
      highresLink = highresLink.getOrElse(""),
      thumbLink = thumbLink.getOrElse(""),
      photoId = photoId.getOrElse(0)
    )
}

trait PhotoParser {
  implicit val photoReads: Reads[Photo] = (
    (JsPath \ "photo_link").readNullable[String] and
      (JsPath \ "highres_link").readNullable[String] and
      (JsPath \ "thumb_link").readNullable[String] and
      (JsPath \ "photo_id").readNullable[Long]
    )(Photo.generate _)
}

object UserDAO extends SalatDAOWithCfg[User, String]("app.mongo.uri", "swipetup_users")

object UserPreference {

  def defaultUserPreference = UserPreference("undefined", "", List(), "")

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

case class RsvpsResults(results: List[Rsvp])

object RsvpsResults {
  def generate(results: Option[List[Rsvp]], total: Option[Int]) =
    RsvpsResults(results = results.getOrElse(List()))
}

trait RsvpsResultsParser extends RsvpParser {
  implicit val categoryResultsRead: Reads[RsvpsResults] = (
    (JsPath \ "results").readNullable[List[Rsvp]] and
      (JsPath \ "total").readNullable[Int]
    )(RsvpsResults.generate _)
}

case class Rsvp(response: String, memberName: String, eventId: String)

object Rsvp {
  def generate(response: Option[String],
               memberName: Option[String],
                eventId: Option[String]) =
    Rsvp(response = response.getOrElse("undefined"), memberName = memberName.getOrElse("undefined"), eventId = eventId.getOrElse("undefined"))
}

trait RsvpParser {
  implicit val categoryReads: Reads[Rsvp] = (
    (JsPath \ "response").readNullable[String] and
      (JsPath \ "member" \ "name").readNullable[String] and
      (JsPath \ "event" \ "id").readNullable[String]
    )(Rsvp.generate _)
}
