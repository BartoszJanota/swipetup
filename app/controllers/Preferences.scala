package controllers

import models._
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
object Preferences extends Controller with CategoryResultsParser {

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

  def initWithPreferencesOf(friendName: String) = Action.async { request =>
    //implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      val loggedUser = request.session.data.get("logged-name").get
      fetchCategories(authToken).map { response =>
        val json = Json.parse(response.body)
        val categoryResults: CategoryResults = json.as[CategoryResults]
        val mappedCategoryResults: Map[String, String] = categoryResults.results.map(category => category.id.toString -> category.name).toMap
        println(mappedCategoryResults)
        val preference: UserPreference = UserPreferenceDAO.findOneById(friendName).getOrElse(UserPreference.defaultUserPreference)
        val filledSearchForm = searchForm.fill(SearchData(Some(preference.city), preference.category, Some(preference.text), None, None)) //date format is yyyy-mm-dd
        Ok(views.html.preferences(loggedUser, filledSearchForm, mappedCategoryResults, friendName))
      }
    }.getOrElse {
      Future(Unauthorized("No way buddy, not your session!"))
    }
  }

  def initPrivate = Action.async { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      val loggedUser = request.session.data.get("logged-name").get
      fetchCategories(authToken).map { response =>
        val json = Json.parse(response.body)
        val categoryResults: CategoryResults = json.as[CategoryResults]
        val mappedCategoryResults: Map[String, String] = categoryResults.results.map(category => category.id.toString -> category.name).toMap
        println(mappedCategoryResults)
        // load city's name by ip
        val filledSearchForm = searchForm.fill(SearchData(None, List(), None, None, None)) //date format is yyyy-mm-dd
        Ok(views.html.preferences(loggedUser, filledSearchForm, mappedCategoryResults))
      }
    }.getOrElse {
      Future(Unauthorized("No way buddy, not your session!"))
    }
  }

  def search = Action { implicit request =>
    request.session.get("oauth-token").map { authToken =>
      val loggedUser = request.session.data.get("logged-name").get
      println("authToken from search: " + authToken)
      //getUserName(authToken)
      println("Search clicked")
      searchForm.bindFromRequest.fold(
        formWithErrors => {
          println("Form with errors")
          BadRequest("Form was not properly validated")
        },
        searchData => {
          val user = UserDAO.findOneById(loggedUser).get
          val userPreference: UserPreference = UserPreference(user, searchData)
          UserPreferenceDAO.save(userPreference)
          println(userPreference)
          println(searchData.startDate)
          println(searchData.endDate)
          Redirect(routes.Timeline.init())
        }
      )
    }.getOrElse {
      Unauthorized("No way buddy, not your session!")
    }
  }

  def fetchCategories(authToken: String): Future[WSResponse] = {
    implicit val app = Play.current
    WS.url(app.configuration.getString("meetup.api.categories").get).
      withHeaders(HeaderNames.AUTHORIZATION -> s"bearer $authToken").
      withQueryString(
        "desc" -> "false",
        "offset" -> "0",
        "format" -> "json",
        "sign" -> "true",
        "photo-host" -> "public",
        "page" -> "50").get()
  }
}
