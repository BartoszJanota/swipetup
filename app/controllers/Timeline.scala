package controllers

import models._
import play.api._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by Przemek on 2014-12-06.
 */
object Timeline extends Controller with EventDataResultsParser with RsvpsResultsParser{

  def init(city: String, category: String, text: String, time: String) = Action.async { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      if (request.session.data.get("content-permission").get.equals("false")) {
        Future(Redirect(routes.Home.init()))
      } else {
      val loggedUser: String = request.session.data.get("logged-name").get
      fetchOpenEvents(authToken, city, category, text, time).map { response =>
        val json = Json.parse(response.body)
        val eventDataResults: EventDataResults = json.as[EventDataResults]
        val eventsToString = eventDataResults.results.map{ event =>
          event.id
        }.toList.mkString(",")
        var updatedList: List[EventData] = eventDataResults.results
        rsvps(authToken, eventsToString).map { response =>
          val json = Json.parse(response.body)
          val rsvpsResults: RsvpsResults = json.as[RsvpsResults]
          println("rsvpsResults: " + rsvpsResults)
          rsvpsResults.results.foreach{ result =>
            if(result.memberName == loggedUser){
              val filteredEvent: List[EventData] = eventDataResults.results.filter(_.id == result.eventId)
              if (filteredEvent.size > 0){
                println("filtered: " + filteredEvent.head)
                updatedList = updatedList.updated(updatedList.indexOf(filteredEvent.head), filteredEvent.head.copy(active = true))
              }
            }
          }
          println("updatedListAfterFilter: " + updatedList)
          }
        println("updatedListBeforeFilter: " + updatedList)
        Ok(views.html.timeline(loggedUser, updatedList))
        }
      }
    }.getOrElse {
      Future(Redirect(routes.Application.signin()).withNewSession)
    }
  }

  def activationChange() = Action.async { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      request.body.asFormUrlEncoded.map { form =>
        println("Activation change: " + form.get("id").get.head + ", " + form.get("active").get.head)
        rsvp(authToken, form).map { response =>
          println(response.body)
          println(response.status)
          Ok("OK")
        }
      }.getOrElse {
        Future(BadRequest("Expected application/form-url-encoded"))
      }
    }.getOrElse {
      Future(Redirect(routes.Application.signin()).withNewSession)
    }
  }

  def rsvps(authToken: String, events: String): Future[WSResponse] = {
    println(events)
    implicit val app = Play.current
    WS.url(app.configuration.getString("meetup.api.rsvps").get).
      withHeaders(HeaderNames.AUTHORIZATION -> s"bearer $authToken").
      withQueryString(
        "sign" -> "true",
        "event_id" -> events,
        "photo-host" -> "public",
        "page" -> "10000").
      get()
  }

  def rsvp(authToken: String, form: Map[String, Seq[String]]): Future[WSResponse] = {
    implicit val app = Play.current
    var rsvp = "no"
    if (form.get("active").get.head == "true") {
      rsvp = "yes"
      println(form.get("groupId").get.head)
      WS.url(app.configuration.getString("meetup.api.group.join").get).
        withHeaders(HeaderNames.AUTHORIZATION -> s"bearer $authToken").
        withQueryString(
          "group_id" -> form.get("groupId").get.head,
          "photo-host" -> "public").
        post("").map { response =>
        println(response.body)
        println(response.status)
        WS.url(app.configuration.getString("meetup.api.rsvp").get).
          withHeaders(HeaderNames.AUTHORIZATION -> s"bearer $authToken").
          withQueryString(
            "event_id" -> form.get("id").get.head,
            "rsvp" -> rsvp).
          post("")
      }
    }
    WS.url(app.configuration.getString("meetup.api.rsvp").get).
      withHeaders(HeaderNames.AUTHORIZATION -> s"bearer $authToken").
      withQueryString(
        "event_id" -> form.get("id").get.head,
        "rsvp" -> rsvp).
      post("")
  }

  def fetchOpenEvents(authToken: String, city: String, category: String, text: String, time: String): Future[WSResponse] = {
    implicit val app = Play.current
    if (category.isEmpty) {
      println("category: " + category)
      WS.url(app.configuration.getString("meetup.api.open_events").get).
        withHeaders(HeaderNames.AUTHORIZATION -> s"bearer $authToken").
        withQueryString(
          "sign" -> "true",
          "photo-host" -> "public",
          "city" -> city,
          "country" -> "PL",
          "time" -> time,
          "text" -> text,
          "page" -> "2").
        get()
    } else {
      WS.url(app.configuration.getString("meetup.api.open_events").get).
        withHeaders(HeaderNames.AUTHORIZATION -> s"bearer $authToken").
        withQueryString(
          "sign" -> "true",
          "photo-host" -> "public",
          "city" -> city,
          "category" -> category,
          "country" -> "PL",
          "time" -> time,
          "text" -> text,
          "page" -> "2").
        get()
    }
  }

}
