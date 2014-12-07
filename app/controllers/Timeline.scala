package controllers

import models.EventData
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
      val events: List[EventData] = List(
        EventData(
          "1",
          "Shapeless - generic programming for scala",
          "Kraków Scala User group",
          "The presentation joined with living examples are thought as a gentle introduction into generic programming with Miles Sabin's beloved child - 'shapeless'. So at the beginning the simple problem will be formulated and then we start a short but I believe interesting journey toward the solution. At the end I will try to show the ultimate direction supported by last works done among all Stefan Zeiger presented on Scala IO in Paris this year. ",
          active = false,
          "Tuesday, Dec 9, 6:30 PM",
          44
        ),
        EventData(
          "2",
          "Get fit with some cardio!",
          "Kraków Exercise Meetup",
          "Cardio exercise class once a week lasting about an hour. Cost: 20zl. Time slots for classes still to be decided. It could turn into Tuesdays and Thursdays depending on how successful classes are. Location to be decided.",
          active = false,
          "Tuesday, Dec 9, 7:00 PM",
          1
        ),
        EventData(
          "3",
          "Change Leaders X-mas meetup",
          "Kraków Change Leaders montly Meetup",
          "X-mas party for Change Leaders friends, supporters and fellows. If you like the idea, want to hang out with us or find our more, just join and have a great time. For sure you will leave the party with head full of great ideas!",
          active = false,
          "Wednesday, Dec 10, 6:00 PM",
          22
        ),
        EventData(
          "4",
          "Web Analytics Wednesday Kraków",
          "GDG Kraków",
          "Web Analytics Wednesday is the world's only social networking event for web analytics professionals. Web Analytics Wednesday is a global effort to put \"faces with names\" and to get local members of the web analytics community networking.",
          active = false,
          "Wednesday, Dec 10, 6:30 PM",
          28
        ),
        EventData(
          "5",
          "#2 Spotkanie Entuzjastów R w Krakowie",
          "Spotkania Użytkowników R",
          "Zapraszamy na kolejne spotkanie entuzjastów R w Krakowie.",
          active = false,
          "Wednesday, Dec 10, 7:00 PM",
          21
        )
      )
      Ok(views.html.timeline(events))
    }.getOrElse {
      Unauthorized("No way buddy, not your session!")
    }
  }

  def activationChange() = Action { request =>
    implicit val app = Play.current
    request.session.get("oauth-token").map { authToken =>
      request.body.asFormUrlEncoded.map { form =>
        println("Activation change: " + form.get("id").get.head + ", " + form.get("active").get.head)
        Ok("OK")
      }.getOrElse {
        BadRequest("Expected application/form-url-encoded")
      }
    }.getOrElse {
      Unauthorized("No way buddy, not your session!")
    }
  }

}
