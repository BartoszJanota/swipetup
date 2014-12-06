package controllers

import play.api._
import play.api.mvc._

/**
 * Created by Przemek on 2014-12-06.
 */
object Timeline extends Controller {

  var localAuthToken: String = null

  def init() = Action { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      localAuthToken = authToken
      // load events from meetup
      Ok(views.html.timeline())
    }.getOrElse {
      Unauthorized("No way buddy, not your session!")
    }
  }

}
