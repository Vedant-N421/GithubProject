package connectors

import models.{ContentModel, RepoModel, UserModel}
import play.api.libs.json.{JsError, JsSuccess, OFormat}
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

  def getRepos[Response](
      login: String = "404",
      url: String = "https://api.github.com/users/"
  )(implicit rds: OFormat[Response], ec: ExecutionContext): Future[Either[String, List[RepoModel]]] = {
    val request = ws.url(url + login + "/repos")
    val response = request.get()
    response
      .map { result =>
        Right(result.json.as[List[RepoModel]])
      }
      .recover {
        case _: WSResponse =>
          Left("Could not connect to Github API.")
      }
  }

  def getContents[Response](
      login: String = "404",
      repoName: String,
      path: String
  )(implicit rds: OFormat[Response], ec: ExecutionContext): Future[Either[String, List[ContentModel]]] = {
    val url = s"https://api.github.com/repos/$login/$repoName/contents"
    val request = ws.url(url + path)
    val response = request.get()
    response
      .map { result =>
        result.json.validate[List[ContentModel]] match {
          case JsSuccess(ls, _) => Right(ls)
          case e: JsError =>
            result.json.validate[ContentModel] match {
              case JsSuccess(cm, _) => Right(List(cm))
            }
        }
      }
      .recover {
        case _: WSResponse =>
          Left("Could not connect to Github API.")
      }
  }

  def gitCreate[Response](
      message: String,
      fileName: String,
      content: String,
      path: String
  )(implicit rds: OFormat[Response], ec: ExecutionContext): Future[Either[String, String]] = {
//    Need to remove the below from being hard-coded lol
    val gitHubAuthToken = "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"

    val request = ws
      .url(path + fileName)
      .addHttpHeaders("Accept" -> "application/vnd.github+json", "Authorization" -> s"Bearer ${gitHubAuthToken}")
      .addHttpHeaders("Content-Type" -> "application/json")

    val jsonPayload =
      s"""{"message": ${message}, "committer": {"name": "Vedant Nemane", "email": "vedant.nemane@mercator.group"}, "content": "${content}"}"""

    val wsResponseFuture = request.put(jsonPayload)

    wsResponseFuture
      .map { x: WSResponse =>
        Right(x.body)
      }
      .recover {
        case _: WSResponse =>
          Left("Could not connect to GitHub API.")
      }
  }
}
