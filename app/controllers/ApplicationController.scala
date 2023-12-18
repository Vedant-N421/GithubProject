package controllers

import connectors.GitHubConnector
import models._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.RepositoryService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(
    val controllerComponents: ControllerComponents,
    val repositoryService: RepositoryService,
    val gitHubConnector: GitHubConnector
)(implicit val ec: ExecutionContext)
    extends BaseController
    with play.api.i18n.I18nSupport {

  def displayUser(login: String): Action[AnyContent] = Action.async { implicit request =>
    gitHubConnector.get[UserModel](login).map {
      case Right(user: UserModel) => Ok(views.html.displayuser(user))
      case Left(err: String) => BadRequest(err)
    }
  }

  def index(): Action[AnyContent] = Action.async { implicit request =>
    repositoryService.index().flatMap {
      case Right(item: Seq[UserModel]) => Future(Ok(Json.toJson(item)))
      case Left(error: String) => Future(BadRequest(Json.toJson(error)))
    }
  }

  def create(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    repositoryService.create(request).map {
      case Right(user: UserModel) => Created(Json.toJson(user))
      case Left(error) => BadRequest(Json.toJson(error))
    }
  }

  def read(login: String): Action[AnyContent] = Action.async { implicit request =>
    repositoryService.read(login).map {
      case Right(user: UserModel) => Ok(Json.toJson(user))
      case Left(error) => BadRequest(Json.toJson(error))
    }
  }

  def update(login: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    repositoryService.update(login, request).map {
      case Right(user: UserModel) => Accepted(Json.toJson(user))
      case Left(error) => BadRequest(Json.toJson(error))
    }
  }

  def delete(login: String): Action[AnyContent] = Action.async { implicit request =>
    repositoryService.delete(login: String).map {
      case Right(message) => Accepted(Json.toJson(message))
      case Left(error) => BadRequest(Json.toJson(error))
    }
  }

  def readAny[T](field: String, value: T): Action[AnyContent] = Action.async { implicit request =>
    repositoryService.readAny(field, value).map {
      case Right(user: UserModel) =>
        Ok(Json.toJson(user))
      case Left(error) =>
        BadRequest(Json.toJson(error))
    }
  }

  def partialUpdate[T](login: String, field: String, value: T): Action[JsValue] =
    Action.async(parse.json) { implicit request =>
      repositoryService.partialUpdate(login, field, value).map {
        case Right(user) => (Accepted(Json.toJson(user)))
        case Left(error) => (BadRequest(Json.toJson(error)))
      }
    }

  def showRepositories(login: String): Action[AnyContent] = Action.async {
    repositoryService.getRepos(login).map {
      case Right(webpage) => Ok(webpage)
      case Left(err: String) => BadRequest(Json.toJson(err))
    }
  }

  def showContents(login: String, repoName: String, path: Option[String]): Action[AnyContent] = Action.async {
    repositoryService.getContents(login, repoName, path).map{
      case Right(webpage) => Ok(webpage)
      case Left(err: String) => BadRequest(Json.toJson(err))
    }
  }

  def showContents(login: String, repoName: String, path: Option[String]): Action[AnyContent] = Action.async {
    repositoryService.getContents(login, repoName, path).map {
      case Right(contentList: List[ContentModel]) =>
        contentList.map {
          case _ if contentList.head.`type` == "file" && contentList.length == 1 =>
            Ok(views.html.displayfile(contentList.head, repoName, path))
          case _ => Ok(views.html.displaycontents(contentList, repoName, login, path))
        }
      case Left(err: String) => BadRequest(Json.toJson(err))
    }
  }

//  def findFiles(login: String, repoName: String, path: Option[String]): Action[AnyContent] = Action.async {
//    repositoryService.getContents(login, repoName, path).map {
//      case Right(Right(file: ContentModel)) => TODO
//      case Right(Left(dir: List[ContentModel])) => TODO
//      case Left(_) => BadRequest
//    }
//  }
}
