package services

import com.mongodb.client.result.DeleteResult
import connectors.GitHubConnector
import models._
import play.api.libs.json.JsValue
import play.api.mvc.Request
import repositories.UserRepoTrait
import viewmodels.{ContentViewModel, RepoListViewModel}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepositoryService @Inject()(
    val userRepoTrait: UserRepoTrait,
    val gitHubConnector: GitHubConnector
)(implicit executionContext: ExecutionContext) {

  def getContents(login: String, repoName: String, path: String): Future[Either[String, ContentViewModel]] = {
    gitHubConnector.getContents(login, repoName, path).map {
      case Right(ls: List[ContentModel]) =>
        ls match {
          case _ if ls.head.`type` == "file" && ls.length == 1 =>
            Right(ContentViewModel(Some(ls.head), None, repoName, path, login))
          case _ => Right(ContentViewModel(None, Some(ls), repoName, path, login))
        }
      case Left(err) => Left(err)
    }
  }

  def getRepos(
      login: String
  ): Future[Either[String, RepoListViewModel]] = {
    gitHubConnector.getRepos(login).map {
      case Right(ls: List[RepoModel]) => Right(RepoListViewModel(ls))
      case Left(err) => Left(err)
    }
  }

  def index(): Future[Either[String, Seq[UserModel]]] = {
    userRepoTrait.index().map {
      case Right(item: Seq[UserModel]) => Right(item)
      case Left(_) => Left("Users cannot be found!")
    }
  }

  def create(request: Request[JsValue]): Future[Either[String, UserModel]] = {
    request.body
      .validate[UserModel]
      .fold(
        errors => {
          Future(Left("ERROR: User not created."))
        },
        userData => {
          userRepoTrait.create(userData).map {
            case None =>
              Left("ERROR: User not created.")
            case _ =>
              Right(userData)
          }
        }
      )
  }

  def update(id: String, request: Request[JsValue]): Future[Either[String, UserModel]] = {
    request.body
      .validate[UserModel]
      .fold(
        errors => {
          Future(Left("ERROR: User not updated."))
        },
        userData => {
          userRepoTrait.update(id, userData).map { _ =>
            Right(userData)
          }
        }
      )
  }

  def read(id: String): Future[Either[String, UserModel]] = {
    for {
      book <- userRepoTrait.read(id)
      res = book match {
        case Some(item: UserModel) => Right(item)
        case _ | None => Left("ERROR: Unable to read user's profile.")
      }
    } yield res
  }

  def delete(id: String): Future[Either[String, String]] = {
    userRepoTrait.delete(id: String).map {
      case Right(_: DeleteResult) => Right("INFO: User was deleted successfully.")
      case Left(error) => Left(error)
    }
  }

  def readAny[T](field: String, value: T): Future[Either[String, UserModel]] = {
    for {
      user <- userRepoTrait.readAny(field, value)
      res = user.map { item: UserModel =>
        Right(item)
      }
    } yield res.getOrElse(Left("ERROR: Unable to read user's profile."))
  }

  def partialUpdate[T](
      login: String,
      field: String,
      value: T
  ): Future[Either[String, UserModel]] = {
    userRepoTrait.partialUpdate(login, field, value).map {
      case Some(user) => Right(user)
      case _ => Left("ERROR: User not updated.")
    }
  }
}
