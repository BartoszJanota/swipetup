package controllers

import models.{EventDataResultsParser, EventDataResults}
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
object Timeline extends Controller with EventDataResultsParser{

  def init() = Action.async { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      val loggedUser = request.session.data.get("logged-name").get
      fetchOpenEvents(authToken).map { response =>
          val json = Json.parse(response.body)
          val eventDataResults: EventDataResults = json.as[EventDataResults]
        Ok(views.html.timeline(loggedUser, eventDataResults.results))
      }
    }.getOrElse {
      Future(Unauthorized("No way buddy, not your session!"))
    }
  }

  def activationChange() = Action { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      request.body.asFormUrlEncoded.map { form =>
        println("Activation change: " + form.get("id").get.head + ", " + form.get("active").get.head)
        Ok("OK")
      }.getOrElse {
        BadRequest("Expected application/form-url-encoded")
      }
    }.getOrElse {
      Unauthorized("No way buddy, not your session!")
    }
  }
  def fetchOpenEvents(authToken: String): Future[WSResponse] = {
    implicit val app = Play.current
    WS.url(app.configuration.getString("meetup.api.open_events").get).
      withHeaders(HeaderNames.AUTHORIZATION -> s"bearer $authToken").
      withQueryString(
        "sign" -> "true",
        "photo-host" -> "public",
        "city" -> "Kraków",
        "country" -> "PL",
        "page" -> "10").
      get()
  }

}
