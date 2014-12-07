package controllers

import models.{SearchData, User, UserDAO, UserParser}
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
object Preferences extends Controller with UserParser {

  val categories = List("java", "python", "scala", "C#", "javascript")

  val searchForm = Form[SearchData](
    mapping(
      "city" -> optional(text),
      "category" -> list(text),
      "text" -> optional(text),
      "startDate" -> optional(text),
      "endDate" -> optional(text)
    )(SearchData.apply)(SearchData.unapply)
  )

  def initWithPreferencesOf(friendName: String) = Action { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      // populate searchForm with data from db
      // load categories
      val filledSearchForm = searchForm.fill(SearchData(Some("Kraków"), List("java", "python"), Some("asynchronous programming"), Some("2014-12-06"), Some("2014-12-12"))) //date format is yyyy-mm-dd
      Ok(views.html.preferences(filledSearchForm, categories, friendName))
    }.getOrElse {
      Unauthorized("No way buddy, not your session!")
    }
  }

  def initPrivate = Action { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      // load city's name by ip
      // load categories
      val filledSearchForm = searchForm.fill(SearchData(Some("Kraków"), List(), None, None, None))
      Ok(views.html.preferences(filledSearchForm, categories))
    }.getOrElse {
      Unauthorized("No way buddy, not your session!")
    }
  }

  def search = Action { implicit request =>
    request.session.get("oauth-token").map { authToken =>
      println("authToken from search: " + authToken)
      //getUserName(authToken)
      println("Search clicked")
      searchForm.bindFromRequest.fold(
        formWithErrors => {
          println("Form with errors")
          BadRequest(views.html.preferences(searchForm, categories))
        },
        searchData => {
          fetchUserName(authToken).map { response =>
            val json = Json.parse(response.body)
            val user: User = json.as[User]
            UserDAO.save(user)
            println(user)
            println(searchData.city)
            println(searchData.category)
            println(searchData.text)
            println(searchData.startDate)
            println(searchData.endDate)
          }
          Redirect(routes.Timeline.init()).withSession("oauth-token" -> authToken)
        }
      )
    }.getOrElse {
      Unauthorized("No way buddy, not your session!")
    }
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
