package models

/**
 * Created by Przemek on 2014-12-06.
 */
class SearchData {
  var city: String = _
  var category: List[String] = _
  var text: String = _
  var startDate: String = _
  var endDate: String = _
}

object SearchData {
  def apply(city: Option[String], category: List[String], text: Option[String], startDate: Option[String], endDate: Option[String]) = {
    val searchData = new SearchData
    searchData.city = city.orNull
    searchData.category = category
    searchData.text = text.orNull
    searchData.startDate = startDate.orNull
    searchData.endDate = endDate.orNull
    searchData
  }

  def unapply(searchData: SearchData) = Some(Option(searchData.city), searchData.category, Option(searchData.text), Option(searchData.startDate), Option(searchData.endDate))
}

