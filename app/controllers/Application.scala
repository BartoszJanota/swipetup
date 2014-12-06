package controllers

import java.util.UUID

import play.api._
import play.api.mvc._
import util.OAuth2

object Application extends Controller {

  def signin = Action { implicit request =>
    val callbackURL = util.routes.OAuth2.callback(None, None).absoluteURL()
    val oauth2 = new OAuth2(Play.current)
    val state = UUID.randomUUID().toString()
    val accessScope = "basic" //Access to Meetup group info, Everywhere API, creating and editing Events and RSVP's, posting photos
  val redirectMeetupUrl = oauth2.getAuthorizationUrl(callbackURL, accessScope, state)
    Ok(views.html.signin("Your new application is ready.", redirectMeetupUrl)).withSession("oauth-state" -> state)
  }

}