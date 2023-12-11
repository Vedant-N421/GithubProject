package models

import play.api.libs.json.{Json, OFormat}

case class UserModel(
    login: String,
    id: Int,
    url: String,
    node_id: String,
    created_at: String,
    followers: Int,
    following: Int
)

object UserModel {
  implicit val formats: OFormat[UserModel] = Json.format[UserModel]
}
