package models

/**
 * Created by Przemek on 2014-12-06.
 */
class EventData {
  var id: String = _
  var title: String = _
  var group: String = _
  var description: String = _
  var active: Boolean = _
  var time: String = _
  var friends: Int = _
}

object EventData {
  def apply(id: String, title: String, group: String, description: String, active: Boolean, time: String, friends: Int) = {
    val eventData = new EventData
    eventData.id = id
    eventData.title = title
    eventData.group = group
    eventData.description = description
    eventData.active = active
    eventData.time = time
    eventData.friends = friends
    eventData
  }

  def unapply(eventData: EventData) = Some(
    eventData.id,
    eventData.title,
    eventData.group,
    eventData.description,
    eventData.active,
    eventData.time,
    eventData.friends
  )
}

