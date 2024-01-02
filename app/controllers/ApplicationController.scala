package controllers

import models.GitFile.dataForm
import connectors.GitHubConnector
import models._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.RepositoryService

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApplicationController @Inject()(
    val controllerComponents: ControllerComponents,
    val repositoryService: RepositoryService,
    val gitHubConnector: GitHubConnector
)(implicit val ec: ExecutionContext)
    extends BaseController
    with play.api.i18n.I18nSupport {

  def gitCreateForm(): Action[AnyContent] =
    Action.async { implicit request =>
      dataForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            // here write what you want to do if the form has errors
            Future(BadRequest("ERROR: Data entered did not match requirements!"))
          },
          formData => {
            // here write how you would use this data to create a new file
            gitHubConnector
              .gitCreate[GitFile](formData.message,
                                  formData.fileName,
                                  Base64.getEncoder.encodeToString(formData.content.getBytes(StandardCharsets.UTF_8)),
                                  formData.path)
              .map {
                case Right(file: String) => Created(Json.toJson(file))
                case Left(err: String) => BadRequest(Json.toJson(err))
              }
          }
        )
//      gitHubConnector
//        .gitCreate[GitFile](
//          "Created with Scala and Play",
//          ".gitignore",
//          "VGVzdGluZyB0aGUgY3JlYXRlIGZ1bmN0aW9uIHdpdGggY3VybCBjb21tYW5kLg==",
//          "https://api.github.com/repos/Vedant-N421/ScalaUdemyTraining/contents/.gitignore"
//        )
//        .map {
//          case Right(stuff) => Ok(stuff)
//          case Left(moreStuff) => BadRequest(moreStuff)
//        }
    }

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
        case Right(user) => Accepted(Json.toJson(user))
        case Left(error) => BadRequest(Json.toJson(error))
      }
    }

  def showRepositories(login: String): Action[AnyContent] = Action.async {
    repositoryService.getRepos(login).map {
      case Right(viewmodel) => Ok(views.html.displayrepos(viewmodel))
      case Left(err: String) => BadRequest(Json.toJson(err))
    }
  }

  def showContents(login: String, repoName: String, path: String): Action[AnyContent] = Action.async {
    repositoryService.getContents(login, repoName, path).map {
      case Right(viewmodel) =>
        viewmodel.file match {
          case Some(item) => Ok(views.html.displayfile(viewmodel))
          case _ => Ok(views.html.displaycontents(viewmodel))
        }
      case Left(err: String) => BadRequest(Json.toJson(err))
    }
  }
}
