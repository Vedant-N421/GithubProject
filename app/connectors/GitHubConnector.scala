package connectors

import models.UserModel
import play.api.libs.json.OFormat
import play.api.libs.ws.{WSClient, WSResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GitHubConnector @Inject()(ws: WSClient) {
  def get[Response](
      login: String = "404",
      url: String = "https://api.github.com/users/"
  )(implicit rds: OFormat[Response], ec: ExecutionContext): Future[Either[String, UserModel]] = {
    val request = ws.url(url + login)
    val response = request.get()

    response
      .map { result =>
        Right(result.json.as[UserModel])
      }
      .recover {
        case _: WSResponse =>
          Left("Could not connect to Github API.")
      }
  }
}
