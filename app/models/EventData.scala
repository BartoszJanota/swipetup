package models

import play.api.libs.json.{JsPath, Reads}
import models.mongoContext._
import play.api.libs.functional.syntax._

/**
 * Created by Przemek on 2014-12-06.
 */
case class EventData(id: String,
                      title: String,
                      group: String,
                      description: String,
                      active: Boolean,
                      time: Long,
                      friends: Int,
                      eventUrl: String
)

object EventData {
  def generate(id: Option[String],
               title: Option[String],
               group: Option[String],
               description: Option[String],
               active: Option[Boolean],
               time: Option[Long],
               friends: Option[Int],
               eventUrl: Option[String]) =
    EventData(id = id.getOrElse("undefined"),
              title = title.getOrElse("undefined"),
              group = group.getOrElse("undefined"),
              description = description.getOrElse("undefined"),
              active = active.getOrElse(false),
              time = time.getOrElse(0L),
              friends = friends.getOrElse(0),
              eventUrl = eventUrl.getOrElse("undefined"))
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

object EventDataResults{
  def generate(results: Option[List[EventData]], total: Option[Int]) =
    EventDataResults(results = results.getOrElse(List()))
}

trait EventDataResultsParser extends EventDataParser {
  implicit val eventDataResultsRead: Reads[EventDataResults] = (
    (JsPath \ "results").readNullable[List[EventData]] and
      (JsPath \ "total").readNullable[Int]
    )(EventDataResults.generate _)
}

