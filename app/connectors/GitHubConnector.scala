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
      path: Option[String]
  )(implicit rds: OFormat[Response], ec: ExecutionContext): Future[Either[String, List[ContentModel]]] = {
    val request = path match {
      case Some(s) => ws.url(s"https://api.github.com/repos/$login/$repoName/contents/$s")
      case None => ws.url(s"https://api.github.com/repos/$login/$repoName/contents")
    }
//    val request = ws.url("https://api.github.com/repos/" + login + "/" + repo + "/contents/")
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

//  def getFiles[Response](
//      login: String = "404",
//      repoName: String,
//      path: String
//  )(implicit rds: OFormat[Response], ec: ExecutionContext): Future[Either[String, List[ContentModel]]] = {
//    val request = ws.url(s"https://api.github.com/repos/$login/$repoName/contents/$path")
//    val response = request.get()
//    response
//      .map { result =>
//        Right(result.json.as[List[ContentModel]])
//      }
//      .recover {
//        case _: WSResponse =>
//          Left("Could not connect to Github API.")
//      }
//  }

}
