package controllers

import models.{FriendData, User, UserDAO, UserParser}
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.{WS, WSResponse}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by Przemek on 2014-12-06.
 */
object Home extends Controller with UserParser {

  val friendForm = Form[FriendData](
    mapping(
      "name" -> optional(text)
    )(FriendData.apply)(FriendData.unapply)
  )

  def init = Action.async { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      fetchUserName(authToken).map { response =>
        val json = Json.parse(response.body)
        val user: User = json.as[User]
        UserDAO.save(user)
        Ok(views.html.home(user.name, friendForm)).withSession("oauth-token" -> authToken, "logged-name" -> user.name)
      }
    }.getOrElse {
      Future(Redirect(routes.Application.signin()).withNewSession)
    }
  }

  def proceed = Action { implicit request =>
    println("Proceed clicked")
    friendForm.bindFromRequest.fold(
      formWithErrors => {
        println("Form with errors")
        BadRequest("Form has errors")
      },
      friendData => {
        if (friendData.name == null) {
          Redirect(routes.Preferences.initPrivate())
        } else {
          Redirect(routes.Preferences.initWithPreferencesOf(friendData.name))
        }
      }
    )
  }

  def fetchUserName(authToken: String): Future[WSResponse] = {
    implicit val app = Play.current
    WS.url(app.configuration.getString("meetup.api.member.self").get).
      withHeaders(HeaderNames.AUTHORIZATION -> s"bearer $authToken").
      withQueryString(
        "sign" -> "true",
        "photo-host" -> "public",
        "page" -> "5").get()
  }

}
