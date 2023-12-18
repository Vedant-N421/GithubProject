package connectors

import models.{ContentModel, RepoModel, UserModel}
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
    print(s"REQUEST =====> ${url + login}")
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
    print(s" login = $login repoName = $repoName and path = $path")
    val url = s"https://api.github.com/repos/$login/$repoName/contents"
    val request = ws.url(url + path)
    val response = request.get()

    response
      .map { result =>
        Right(result.json.as[List[ContentModel]])
      }
      .recover {
        case _: WSResponse =>
          Left("Could not connect to Github API.")
      }
  }

}
