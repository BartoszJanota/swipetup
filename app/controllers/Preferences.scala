package controllers

import models.SearchData
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

/**
 * Created by Przemek on 2014-12-06.
 */
object Preferences extends Controller {

  var localAuthToken: String = null

  val categories = List("java", "python", "scala", "C#", "javascript")

  val searchForm = Form[SearchData](
    mapping(
      "city" -> optional(text),
      "category" -> optional(text),
      "text" -> optional(text),
      "startDate" -> optional(text),
      "endDate" -> optional(text)
    )(SearchData.apply)(SearchData.unapply)
  )

  def initPublic(name: String) = Action { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      localAuthToken = authToken
      // populate searchForm with data from db
      // load categories
      searchForm.fill(SearchData(Some("Kraków"), Some("IT"), Some("java"), Some("06.12.2014"), Some("12.12.2014")))
      Ok(views.html.preferences(searchForm, categories, name))
    }.getOrElse {
      Unauthorized("No way buddy, not your session!")
    }
  }

  def initPrivate() = Action { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      localAuthToken = authToken
      // load city's name by ip
      // load categories
      searchForm.fill(SearchData(Some("Kraków"), None, None, None, None))
      Ok(views.html.preferences(searchForm, categories))
    }.getOrElse {
      Unauthorized("No way buddy, not your session!")
    }
  }

  def search = Action { implicit request =>
    println("Search clicked")
    searchForm.bindFromRequest.fold(
      formWithErrors => {
        println("Form with errors")
        BadRequest(views.html.preferences(searchForm, categories))
      },
      searchData => {
        println(searchData.city)
        println(searchData.category)
        println(searchData.text)
        println(searchData.startDate)
        println(searchData.endDate)
        Redirect(routes.Timeline.init()).withSession("oauth-token" -> localAuthToken)
      }
    )
  }

}
