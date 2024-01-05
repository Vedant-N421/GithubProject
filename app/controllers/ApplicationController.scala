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
              .gitCUD(
                gitCUDParameters(
                  "CREATE",
                  login,
                  repoName,
                  formData.message,
                  formData.fileName,
                  extractContent(formData),
                  formData.path
                )
              )
              .map {
                case Right(file: String) => Created(Json.toJson(file))
                case Left(err: String) => BadRequest(Json.toJson(err))
              }
          }
        )
    }
  }

  private def extractContent(formData: GitFile) = {
    formData.content match {
      case Some(string) => Some(Base64.getEncoder.encodeToString(string.getBytes(StandardCharsets.UTF_8)))
      case None => None
    }
  }

  def gitUpdate(login: String, repoName: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.updategitfile(GitFile.dataForm, login, repoName))
  }

  def gitUpdateForm(login: String, repoName: String): Action[AnyContent] = {
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
              .gitCUD(
                gitCUDParameters(
                  "UPDATE",
                  login,
                  repoName,
                  formData.message,
                  formData.fileName,
                  extractContent(formData),
                  formData.path
                )
              )
              .map {
                case Right(file: String) => Created(Json.toJson(file))
                case Left(err: String) => BadRequest(Json.toJson(err))
              }
          }
        )
    }
  }

  def gitDelete(login: String, repoName: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.deletegitfile(GitFile.dataForm, login, repoName))
  }

  def gitDeleteForm(login: String, repoName: String): Action[AnyContent] = {
    Action.async { implicit request =>
      accessToken
      println(s"Flag 1")
      dataForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            println(s"Flag 2")
            // here write what you want to do if the form has errors
            Future(BadRequest("ERROR: Data entered did not match requirements!"))
          },
          formData => {
            println(s"Flag 3")
            // here write how you would use this data to create a new file
            gitHubConnector
              .gitCUD(
                gitCUDParameters(
                  "DELETE",
                  login = login,
                  repoName = repoName,
                  message = formData.message,
                  fileName = formData.fileName,
                  content = None,
                  path = formData.path
                )
              )
              .map {
                case Right(file: String) => Created(Json.toJson(file))
                case Left(err: String) => BadRequest(Json.toJson(err))
              }
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
      case Right(viewModel) => Ok(views.html.displayrepos(viewModel))
      case Left(err: String) => BadRequest(Json.toJson(err))
    }
  }

  def showContents(login: String, repoName: String, path: String): Action[AnyContent] = Action.async {
    repositoryService.getContents(login, repoName, path).map {
      case Right(viewModel) =>
        viewModel.file match {
          case Some(item) => Ok(views.html.displayfile(viewModel))
          case _ => Ok(views.html.displaycontents(viewModel))
        }
      case Left(err: String) => BadRequest(Json.toJson(err))
    }
  }
}
