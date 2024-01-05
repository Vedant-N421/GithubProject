package models

import play.api.libs.json.{Json, OFormat}

case class ContentModel(
    name: String,
    path: String,
    sha: String,
    url: String,
    `type`: String,
    content: Option[String]
)

object ContentModel {
  implicit val formats: OFormat[ContentModel] = Json.format[ContentModel]
}
