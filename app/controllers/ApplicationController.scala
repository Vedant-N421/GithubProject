package controllers

import connectors.GitHubConnector
import models.{DataModel, UserModel}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results._
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.RepositoryService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ApplicationController @Inject()(
    val controllerComponents: ControllerComponents,
    val repositoryService: RepositoryService,
    val gitHubConnector: GitHubConnector
)(implicit val ec: ExecutionContext)
    extends BaseController
    with play.api.i18n.I18nSupport {

  def displayUser(login: String): Action[AnyContent] = Action.async { implicit request =>
    gitHubConnector.get[UserModel](login).map {
      case Right(user: UserModel) => Ok(Json.toJson(user))
      case Left(err: String) => BadRequest(err)
    }
  }

  def create(): Action[JsValue] = Action.async(parse.json) = { implicit request =>
    repositoryService.create(request).map {
      case Right(book) => Created(Json.toJson(book))
      case Left(error) => BadRequest(Json.toJson(error))
    }
  }

  def read(id: String): Action[AnyContent] = Action.async { implicit request =>
    repositoryService.read(id).map {
      case Right(book: UserModel) => Ok(Json.toJson(book))
      case Left(error) => BadRequest(Json.toJson(error))
    }
  }

  def update(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    repositoryService.update(id, request).map {
      case Right(book) => Accepted(Json.toJson(book))
      case Left(error) => BadRequest(Json.toJson(error))
    }
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    repositoryService.delete(id: String).map {
      case Right(message) => Accepted(Json.toJson(message))
      case Left(error) => BadRequest(Json.toJson(error))
    }
  }
}
