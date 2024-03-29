package models

import play.api.libs.json.{Json, OFormat}

case class RepoModel(
    id: Int,
    name: String,
    url: String,
    owner: Owner
)

object RepoModel {
  implicit val formats: OFormat[RepoModel] = Json.format[RepoModel]
}

case class Owner(
    login: String,
    url: String
)

object Owner {
  implicit val formats: OFormat[Owner] = Json.format[Owner]
}
