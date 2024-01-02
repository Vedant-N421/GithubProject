package controllers

import connectors.GitHubConnector
import models.GitFile.dataForm
import models._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.filters.csrf.CSRF
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

  private def accessToken(implicit request: Request[_]): Option[CSRF.Token] = {
    CSRF.getToken
  }

  def gitCreate(login: String, repoName: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.creategitfile(GitFile.dataForm, login, repoName))
  }

  def gitCreateForm(login: String, repoName: String): Action[AnyContent] = {
    Action.async { implicit request =>
      accessToken
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
              .gitCreate[GitFile](
                login,
                repoName,
                formData.message,
                formData.fileName,
                Base64.getEncoder.encodeToString(formData.content.getBytes(StandardCharsets.UTF_8)),
                formData.path
              )
          }
        )
    }
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
