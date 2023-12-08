package models

import akka.http.scaladsl.model.headers.Date
import play.api.libs.json.{Json, OFormat}

case class UserModel(
    username: String,
    dateCreated: Date,
    location: String,
    followerCount: Int,
    followingCount: Int
)

object UserModel {
  implicit val formats: OFormat[UserModel] = Json.format[UserModel]
}
