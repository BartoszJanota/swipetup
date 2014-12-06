package controllers

import models.FriendData
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

/**
 * Created by Przemek on 2014-12-06.
 */
object Home extends Controller {

  var localAuthToken: String = null

  val friendForm = Form[FriendData](
    mapping(
      "name" -> optional(text)
    )(FriendData.apply)(FriendData.unapply)
  )

  def init = Action { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      localAuthToken = authToken
      Ok(views.html.home(friendForm))
    }.getOrElse {
      Unauthorized("No way buddy, not your session!")
    }
  }

  def proceed = Action { implicit request =>
    println("Proceed clicked")
    friendForm.bindFromRequest.fold(
      formWithErrors => {
        println("Form with errors")
        BadRequest(views.html.home(friendForm))
      },
      friendData => {
        if (friendData.name == null) {
          Redirect(routes.Preferences.initPrivate()).withSession("oauth-token" -> localAuthToken)
        } else {
          Redirect(routes.Preferences.initPublic(friendData.name)).withSession("oauth-token" -> localAuthToken)
        }
      }
    )
  }

}
