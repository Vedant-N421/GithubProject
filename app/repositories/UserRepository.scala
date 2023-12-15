package repositories

import com.google.inject.ImplementedBy
import connectors.GitHubConnector
import models.{ContentModel, RepoModel, UserModel}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.empty
import org.mongodb.scala.model.{Filters, IndexModel, Indexes, ReplaceOptions}
import org.mongodb.scala.result
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[UserRepository])
trait UserRepoTrait {
//  def getFiles(login: String, repoName: String, path: String): Future[Option[Either[List[ContentModel], ContentModel]]]

  def getContents(login: String, repoName: String, path: Option[String]): Future[Option[List[ContentModel]]]

  def getRepos(login: String): Future[Option[List[RepoModel]]]

  def index(): Future[Either[String, Seq[UserModel]]]

  def create(user: UserModel): Future[Option[UserModel]]

  def read(login: String): Future[Option[UserModel]]

  def readAny[T](field: String, value: T): Future[Option[UserModel]]

  def update(login: String, user: UserModel): Future[result.UpdateResult]

  def partialUpdate[T](login: String, field: String, value: T): Future[Option[UserModel]]

  def delete(login: String): Future[Either[String, result.DeleteResult]]
}

@Singleton
class UserRepository @Inject()(mongoComponent: MongoComponent, gitHubConnector: GitHubConnector)(
    implicit ec: ExecutionContext
) extends PlayMongoRepository[UserModel](
      collectionName = "userModels",
      mongoComponent = mongoComponent,
      domainFormat = UserModel.formats,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("login")
        )
      ),
      replaceIndexes = false
    )
    with UserRepoTrait {

  def getRepos(login: String): Future[Option[List[RepoModel]]] = {
    gitHubConnector.getRepos[RepoModel](login).map {
      case Right(repos: List[RepoModel]) => Some(repos)
      case Left(_) => None
    }
  }

  def getContents(login: String, repoName: String, path: Option[String]): Future[Option[List[ContentModel]]] = {
    gitHubConnector.getContents[ContentModel](login, repoName, None).map {
      case Right(contentList: List[ContentModel]) => Some(contentList)
      case Left(_) => None
    }
  }

  def index(): Future[Either[String, Seq[UserModel]]] =
    collection.find().toFuture().map {
      case users: Seq[UserModel] => Right(users)
      case _ => Left("Users cannot be found!")
    }

  private def byLogin(login: String): Bson =
    Filters.and(
      Filters.equal("login", login)
    )

  def create(user: UserModel): Future[Option[UserModel]] = {
    collection.find(byLogin(user.login)).headOption().flatMap {
      case Some(_) => Future(None)
      case _ => collection.insertOne(user).toFuture().map(_ => Some(user))
    }
  }

  def read(login: String): Future[Option[UserModel]] =
    collection.find(byLogin(login)).headOption().flatMap {
      case Some(data) =>
        Future(Some(data))
      case _ =>
        Future(None)
    }

  def update(login: String, user: UserModel): Future[result.UpdateResult] =
    collection
      .replaceOne(
        filter = byLogin(login),
        replacement = user,
        options = new ReplaceOptions().upsert(
          false
        )
      )
      .toFuture()

  def delete(login: String): Future[Either[String, result.DeleteResult]] = {
    collection.find(byLogin(login)).headOption().flatMap {
      case Some(_) => (collection.deleteOne(filter = byLogin(login)).toFuture().map(Right(_)))
      case _ => Future(Left("INFO: User not found, so no deletion made!"))
    }
  }

  def deleteAll(): Future[Unit] =
    collection.deleteMany(empty()).toFuture().map(_ => ())

  private def byAny[T](field: String, value: T): Bson = {
    Filters.and(
      Filters.equal(
        field match {
          case field if List("login", "id", "url", "node_id", "created_at", "followers", "following").contains(field) =>
            field
          case _ =>
            println("Invalid field name specified, defaulting to login!")
            "login"
        },
        value
      )
    )
  }

  def readAny[T](field: String, value: T): Future[Option[UserModel]] = {
    collection
      .find(byAny(field, value))
      .headOption()
      .flatMap {
        case Some(data) =>
          Future(Some(data))
        case _ =>
          Future(None)
      }
      .recover {
        case error =>
          None
      }
  }

  def partialUpdate[T](login: String, field: String, value: T): Future[Option[UserModel]] = {
    collection.find(byLogin(login)).headOption.flatMap {
      case Some(user) =>
        val updatedUser = field match {
          case "login" => user.copy(login = value.toString)
          case "id" => user.copy(id = value.asInstanceOf[Int])
          case "url" => user.copy(url = value.toString)
          case "node_id" => user.copy(node_id = value.toString)
          case "created_at" => user.copy(created_at = value.toString)
          case "followers" => user.copy(followers = value.asInstanceOf[Int])
          case "following" => user.copy(following = value.asInstanceOf[Int])
          case _ => user
        }
        update(login, updatedUser).map(thing => Some(updatedUser))
      case _ => Future.successful(None)
    }
  }

}
