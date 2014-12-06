package util

import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.ws.WS
import play.api.mvc.{Action, Controller, Results}
import play.api.{Application, Play}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by bj on 29.11.14.
 */
class OAuth2(application: Application) {
  lazy val meetupAuthKey = application.configuration.getString("meetup.auth.key").get
  lazy val meetupAuthSecret = application.configuration.getString("meetup.auth.secret").get

  def getAuthorizationUrl(redirectUri: String, scope: String, state: String): String = {
    val baseUrl = application.configuration.getString("meetup.redirect.url").get
    baseUrl.format(meetupAuthKey, redirectUri, state) //basic by default
  }

  def getToken(code: String): Future[String] = {

    val redirectUri: String = application.configuration.getString("swipetup.domain").get + "/_oauth-callback"
    println(redirectUri)

    val tokenResponse = WS.url(application.configuration.getString("meetup.access.url").get)(application).
      withQueryString("client_id" -> meetupAuthKey,
        "client_secret" -> meetupAuthSecret,
        "grant_type" -> "authorization_code",
        "redirect_uri" -> redirectUri,
        "code" -> code).
      withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON, HeaderNames.CONTENT_TYPE -> "application/x-www-form-urlencoded").
      post(Results.EmptyContent())

    tokenResponse.flatMap { response =>
      (response.json \ "access_token").asOpt[String].fold(Future.failed[String](new IllegalStateException("Go away intruder! Not your session!"))) { accessToken =>
        println(accessToken)
        Future.successful(accessToken)
      }
    }
  }
}

object OAuth2 extends Controller {
  lazy val oauth2 = new OAuth2(Play.current)

  def callback(codeOpt: Option[String] = None, stateOpt: Option[String] = None) = Action.async { implicit request =>
    (for {
      code <- codeOpt
      state <- stateOpt
      oauthState <- request.session.get("oauth-state")
    } yield {
      if (state == oauthState) {
        oauth2.getToken(code).map { accessToken =>
          Redirect(controllers.routes.Home.init()).withSession("oauth-token" -> accessToken)
        }.recover {
          case ex: IllegalStateException => Unauthorized(ex.getMessage)
        }
      }
      else {
        Future.successful(BadRequest("Invalid meetup login"))
      }
    }).getOrElse(Future.successful(BadRequest("No parameters supplied")))
  }

  def success() = Action.async { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").fold(Future.successful(Unauthorized("No way buddy, not your session!"))) { authToken =>
      println(authToken)
      WS.url(app.configuration.getString("meetup.api.open_events").get).
        //WS.url("https://api.meetup.com/2/open_events?&sign=true&photo-host=public&city=Kraków&country=PL&page=5").
        withHeaders(HeaderNames.AUTHORIZATION -> s"bearer $authToken").
        withQueryString(
          "sign" -> "true",
          "photo-host" -> "public",
          "city" -> "Kraków",
          "country" -> "PL",
          "page" -> "5").
        get().map { response =>
        Ok(response.json)
      }
    }
  }
}
