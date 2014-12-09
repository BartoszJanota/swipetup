package controllers

import models.{EventDataResults, EventDataResultsParser}
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
object Timeline extends Controller with EventDataResultsParser {

  def init(city: String, category: String, text: String, time: String) = Action.async { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      val loggedUser: String = request.session.data.get("logged-name").get
      fetchOpenEvents(authToken, city, category, text, time).map { response =>
        val json = Json.parse(response.body)
        val eventDataResults: EventDataResults = json.as[EventDataResults]
        Ok(views.html.timeline(loggedUser, eventDataResults.results))
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
          //if(response.status != 201){
            //println("redirecting")
            //Redirect(routes.Timeline.init("Kraków", "", "", "")).withNewSession
          //} else {
            Ok("OK")
          //}
        }
      }.getOrElse {
        Future(BadRequest("Expected application/form-url-encoded"))
      }
    }.getOrElse {
      Future(Redirect(routes.Application.signin()).withNewSession)
    }
  }


  def rsvp(authToken: String, form: Map[String, Seq[String]]): Future[WSResponse] ={
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
    if (category.isEmpty){
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
          "page" -> "30").
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
          "page" -> "30").
        get()
    }
  }

}
