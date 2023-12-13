package models

import play.api.libs.json.{Json, OFormat}

case class RepoModel(
    id: Int,
    name: String,
    url: String
)

object RepoModel {
  implicit val formats: OFormat[RepoModel] = Json.format[RepoModel]
}
