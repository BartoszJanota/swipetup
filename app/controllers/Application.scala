package controllers

import java.util.UUID

import play.api._
import play.api.mvc._
import util.OAuth2

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Application extends Controller {

  def signin = Action.async { implicit request =>
    request.session.get("logged-name").map { loggedUser =>
      Future(Redirect(routes.Home.init()))
    }.getOrElse {
      val callbackURL = util.routes.OAuth2.callback(None, None).absoluteURL()
      val oauth2 = new OAuth2(Play.current)
      val state = UUID.randomUUID().toString()
      val accessScope = "basic"
      val redirectMeetupUrl = oauth2.getAuthorizationUrl(callbackURL, accessScope, state)
      Future(Ok(views.html.signin("Your new application is ready.", redirectMeetupUrl)).withSession("oauth-state" -> state))
    }
  }

  def logout = Action { implicit request =>
    Redirect(routes.Application.signin()).withNewSession
  }

}