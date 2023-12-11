package controllers

import connectors.GitHubConnector
import models.UserModel
import play.api.libs.json.Json
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

//  def displayUser(name: String): Action[AnyContent] = Action.async { implicit request =>
//    repositoryService.read(name).map {
//      case Right(user: UserModel) => Ok(views.html.displayuser(user))
//      case Left(err: String) =>
//        BadRequest(
//          views.html.displayuser(
//            UserModel(login = "404",
//                      id = 404,
//                      url = "404",
//                      node_id = "404",
//                      created_at = "404",
//                      followers = 404,
//                      following = 404)
//          )
//        )
//    }
//  }
}
