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
      if (request.session.data.get("content-permission").get.equals("false")) {
        Future(Redirect(routes.Home.init()))
      } else {
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
      }
    }.getOrElse {
      Future(Redirect(routes.Application.signin()).withNewSession)
    }
  }

  def initPrivate = Action.async { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      if (request.session.data.get("content-permission").get.equals("false")) {
        Future(Redirect(routes.Home.init()))
      } else {
        val loggedUser = request.session.data.get("logged-name").get
        fetchCategories(authToken).map { response =>
          val json = Json.parse(response.body)
          val categoryResults: CategoryResults = json.as[CategoryResults]
          val mappedCategoryResults: Map[String, String] = categoryResults.results.map(category => category.id.toString -> category.name).toMap
          val filledSearchForm = searchForm.fill(SearchData(None, List(), None, None, None)) //date format is yyyy-mm-dd
          Ok(views.html.preferences(loggedUser, filledSearchForm, mappedCategoryResults))
        }
      }
    }.getOrElse {
      Future(Redirect(routes.Application.signin()).withNewSession)
    }
  }

  def search = Action { implicit request =>
    request.session.get("oauth-token").map { authToken =>
      val loggedUser = request.session.data.get("logged-name").get

      println("Search clicked")
      searchForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest("Form was not properly validated")
        },
        searchData => {
          val user = UserDAO.findOneById(loggedUser).get
          val userPreference: UserPreference = UserPreference(user, searchData)
          UserPreferenceDAO.save(userPreference)
          println(userPreference)
          val format = new java.text.SimpleDateFormat("yyyy-MM-dd")
          println(searchData.startDate)
          println(searchData.endDate)
          var time = System.currentTimeMillis.toString + ",1m"
          if (searchData.endDate != null && searchData.startDate != null) {
            val startDate: Long = format.parse(searchData.startDate).getTime
            println(startDate)
            val endDate: Long = format.parse(searchData.endDate).getTime
            println(endDate)
            time = startDate.toString + "," + endDate.toString
          }
          Redirect(routes.Timeline.init(userPreference.city, userPreference.category.mkString(","), userPreference.text, time))
        }
      )
    }.getOrElse {
      Redirect(routes.Application.signin()).withNewSession
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
