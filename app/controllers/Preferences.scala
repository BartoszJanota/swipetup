package controllers

import models._
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.{WSResponse, WS}
import play.api.mvc._
import util.OAuth2._

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
      val preference: UserPreference = UserPreferenceDAO.findOneById(friendName).getOrElse(UserPreference.defaultUserPreference)
      println(preference)
      val filledSearchForm = searchForm.fill(SearchData(Some(preference.city), preference.category, Some(preference.text), Some("2014-12-06"), Some("2014-12-12"))) //date format is yyyy-mm-dd
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
          fetchUserName(authToken).map{ response =>
              val json = Json.parse(response.body)
              val user: User = json.as[User]
              UserDAO.save(user)
              val userPreference: UserPreference = UserPreference(user, searchData)
              UserPreferenceDAO.save(userPreference)
              println(userPreference)
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
