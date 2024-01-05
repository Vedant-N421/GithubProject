package models
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.{Json, OFormat}

case class GitFile(
    message: String,
    fileName: String,
    content: Option[String],
    path: String
)

object GitFile {
  implicit val formats: OFormat[GitFile] = Json.format[GitFile]

  implicit val dataForm: Form[GitFile] = Form(
    mapping(
      "message" -> text,
      "fileName" -> text,
      "content" -> optional(text),
      "path" -> text
    )(GitFile.apply)(GitFile.unapply)
  )
}
