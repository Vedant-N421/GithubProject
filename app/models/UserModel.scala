package models

import play.api.libs.json.{JsValue, Json, OFormat, Writes}

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

  implicit val userWrites: Writes[UserModel] = new Writes[UserModel] {
    def writes(user: UserModel): JsValue = {
      Json.obj(
        "login" -> user.login,
        "id" -> user.id,
        "url" -> user.url,
        "node_id" -> user.node_id,
        "created_at" -> user.created_at,
        "followers" -> user.followers,
        "following" -> user.following
      )
    }
  }
}
