package models

/**
 * Created by Przemek on 2014-12-06.
 */
class FriendData {
  var name: String = _
}

object FriendData {
  def apply(name: Option[String]) = {
    val friendData = new FriendData
    friendData.name = name.orNull
    friendData
  }

  def unapply(friendData: FriendData) = Some(Option(friendData.name))
}
