package models

import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

/**
 * Created by Przemek on 2014-12-06.
 */
case class EventData(id: String,
                     title: String,
                     group: String,
                     description: String,
                     active: Boolean,
                     time: String,
                     friends: Int,
                     eventUrl: String
                      )

object EventData {
  val format = new java.text.SimpleDateFormat("yyyy-MM-dd")
  def generate(id: Option[String],
               title: Option[String],
               group: Option[String],
               description: Option[String],
               active: Option[Boolean],
               time: Option[Long],
               friends: Option[Int],
               eventUrl: Option[String]) =
    EventData(id = id.getOrElse(""),
      title = title.getOrElse(""),
      group = group.getOrElse(""),
      description = {
        var parsedNoHTML: String = description.getOrElse("").replaceAll( """(<\/?.*?>)""", "")
        if (parsedNoHTML.length > 300){
          parsedNoHTML = parsedNoHTML.substring(0,300) + "..."
        }
        parsedNoHTML
      },
      active = active.getOrElse(false),
      time = format.format(new java.util.Date(time.getOrElse(System.currentTimeMillis))),
      friends = friends.getOrElse(0),
      eventUrl = eventUrl.getOrElse(""))
}

trait EventDataParser {
  implicit val eventDataReads: Reads[EventData] = (
    (JsPath \ "id").readNullable[String] and
      (JsPath \ "name").readNullable[String] and
      (JsPath \ "group" \ "name").readNullable[String] and
      (JsPath \ "description").readNullable[String] and
      (JsPath \ "active").readNullable[Boolean] and
      (JsPath \ "time").readNullable[Long] and
      (JsPath \ "yes_rsvp_count").readNullable[Int] and
      (JsPath \ "event_url").readNullable[String]
    )(EventData.generate _)
}

case class EventDataResults(results: List[EventData])

object EventDataResults {
  def generate(results: Option[List[EventData]], total: Option[Int]) =
    EventDataResults(results = results.getOrElse(List()))
}

trait EventDataResultsParser extends EventDataParser {
  implicit val eventDataResultsRead: Reads[EventDataResults] = (
    (JsPath \ "results").readNullable[List[EventData]] and
      (JsPath \ "total").readNullable[Int]
    )(EventDataResults.generate _)
}

