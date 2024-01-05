package connectors

import models._
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GitHubConnector @Inject()(ws: WSClient, config: Configuration) {

  def get[Response](url: String)(implicit rds: OFormat[Response],
                                 ec: ExecutionContext): Future[Either[String, Response]] = {
    ws.url(url)
      .get()
      .map { result =>
        result.json.validate[Response] match {
          case JsSuccess(value, _) => Right(value)
          case e: JsError => Left(e.toString)
        }
      }
      .recover {
        case _: WSResponse =>
          Left("ERROR: Could not connect to Github API.")
      }
  }

  def getAsList[Response](url: String)(implicit rds: OFormat[Response],
                                       ec: ExecutionContext): Future[Either[String, List[Response]]] = {
    ws.url(url)
      .get()
      .map { result =>
        result.json.validate[List[Response]] match {
          case JsSuccess(value, _) => Right(value)
          case e: JsError => Left(e.toString)
        }
      }
      .recover {
        case _: WSResponse =>
          Left("ERROR: Could not connect to Github API.")
      }
  }

  def getRepos(login: String)(implicit rds: OFormat[RepoModel],
                              ec: ExecutionContext): Future[Either[String, List[RepoModel]]] = {
    getAsList[RepoModel](s"https://api.github.com/users/$login/repos")
  }

  def getContents(login: String, repoName: String, path: String)(
      implicit rds: OFormat[ContentModel],
      ec: ExecutionContext
  ): Future[Either[String, List[ContentModel]]] = {
    getAsList[ContentModel](s"https://api.github.com/repos/$login/$repoName/contents$path").flatMap {
      case Right(x: List[ContentModel]) => Future(Right(x))
      case Left(_) =>
        get[ContentModel](s"https://api.github.com/repos/$login/$repoName/contents$path").map {
          case Right(y: ContentModel) => Right(List(y))
          case Left(err) => Left(err)
        }
    }
  }

  def gitCUD(
      cudParam: gitCUDParameters
  )(implicit rds: OFormat[ContentModel], ec: ExecutionContext): Future[Either[String, String]] = {
    val gitHubAuthToken = config.get[String]("AUTHPASS")

    val request = ws
      .url(getUrl(cudParam.login, cudParam.repoName, cudParam.fileName, cudParam.path))
      .addHttpHeaders("Accept" -> "application/vnd.github+json", "Authorization" -> s"Bearer $gitHubAuthToken")
      .addHttpHeaders("Content-Type" -> "application/json")

    val file = findFile(cudParam.login, cudParam.repoName, cudParam.path, cudParam.fileName)

    val addedPayload: Future[String] = cudParam.CUD match {
      case "CREATE" => Future.successful(raw""", "content": "${s"${cudParam.content.getOrElse("")}"}"}""")
      case "UPDATE" => update(file, cudParam.content)
      case "DELETE" => delete(file)
    }

    addedPayload
      .map(
        added =>
          raw"""{"message": "${s"${cudParam.message}"}", "committer": {"name": "Vedant Nemane", "email": "vedant.nemane@mercator.group"}""" + added
      )
      .flatMap { jsonPayload =>
        val wsResponseFuture = cudParam.CUD match {
          case "DELETE" =>
            request.withBody(jsonPayload).delete()
          case _ => request.put(jsonPayload)
        }
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

  private def delete(file: Future[List[ContentModel]])(implicit ec: ExecutionContext) = {
    file.flatMap { fileList =>
      Future.successful(
        fileList.headOption
          .map {
            case cm: ContentModel => raw""", "sha": "${s"${cm.sha}"}"}"""
            case _ => "ERROR: Could not locate SHA!"
          }
          .getOrElse("ERROR: No file found!")
      )
    }
  }

  private def update(file: Future[List[ContentModel]], content: Option[String])(implicit ec: ExecutionContext) = {
    file.flatMap { fileList =>
      Future.successful(
        fileList.headOption
          .map {
            case cm: ContentModel =>
              raw""", "content": "${s"${content.getOrElse("")}"}", "sha": "${s"${cm.sha}"}"}"""
            case _ => "ERROR: Could not locate SHA!"
          }
          .getOrElse("ERROR: No file found!")
      )
    }
  }

  private def findFile(login: String, repoName: String, path: String, fileName: String)(
      implicit ec: ExecutionContext,
      rds: OFormat[ContentModel]
  ) = {
    getContents(login, repoName, path).map {
      case Right(res) =>
        res.filter { x =>
          x.name.equals(fileName)
        }
      case Left(_) => List()
    }
  }

  private def getUrl(login: String, repoName: String, fileName: String, path: String) = {
    path match {
      case "" => s"https://api.github.com/repos/$login/$repoName/contents/$fileName"
      case path => s"https://api.github.com/repos/$login/$repoName/contents/$path/$fileName"
    }
  }
}
