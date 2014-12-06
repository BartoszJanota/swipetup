package controllers

import play.api._
import play.api.mvc._

/**
 * Created by Przemek on 2014-12-06.
 */
object Preferences extends Controller {

  def init = Action { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      Ok(views.html.preferences())
    }.getOrElse {
      Unauthorized("No way buddy, not your session!")
    }
  }

}
