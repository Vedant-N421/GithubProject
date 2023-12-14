package models

import play.api.libs.json.{Json, OFormat}

case class ContentModel(
    name: String,
    path: String,
    url: String
)

object ContentModel {
  implicit val formats: OFormat[ContentModel] = Json.format[ContentModel]
}