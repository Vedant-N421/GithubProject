package services

import baseSpec.BaseSpec
import com.mongodb.client.result.UpdateResult
import connectors.GitHubConnector
import models.{ContentModel, RepoModel, UserModel}
import org.mongodb.scala.result.DeleteResult
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.{DELETE, GET, POST, PUT}
import repositories._
import viewmodels.{ContentViewModel, RepoListViewModel}

import scala.concurrent.{ExecutionContext, Future}

class RepositoryServiceSpec extends BaseSpec with MockFactory with ScalaFutures with GuiceOneAppPerSuite {

  val mockRepository: UserRepoTrait = mock[UserRepoTrait]
  val gitHubConnector: GitHubConnector = mock[GitHubConnector]
  implicit val executionContext: ExecutionContext =
    app.injector.instanceOf[ExecutionContext]
  val testService = new RepositoryService(mockRepository, gitHubConnector)

  def buildPost(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(POST, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def buildGet(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def buildPut(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(PUT, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def buildDelete(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(DELETE, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  private val me: JsValue = Json.obj(
    "login" -> "Vedant-N421",
    "id" -> 147396311,
    "url" -> "https://api.github.com/users/Vedant-N421",
    "node_id" -> "U_kgDOCMkW1w",
    "created_at" -> "2023-10-09T11:03:37Z",
    "followers" -> 1,
    "following" -> 0
  )

  private val me2: JsValue = Json.obj(
    "login" -> "ChangedLogin",
    "id" -> 87685,
    "url" -> "https://api.github.com/users/Vedant-N421",
    "node_id" -> "changednodeid",
    "created_at" -> "changeddate",
    "followers" -> 99,
    "following" -> 100
  )

  private val badJsonObj: JsValue = Json.obj(
    "hehehe" -> "ChangedLogin",
    "i changed this bad boy" -> 87685,
    "url" -> "https://api.github.com/users/Vedant-N421",
    "node_id" -> "changednodeid",
    "created_at" -> "changeddate",
    "followers" -> 99,
    "following" -> 100
  )

  ".index" should {
    "return an empty sequence of users" in {
      (() => mockRepository.index()).expects().returning(Future(Right(Seq()))).once()
      whenReady(testService.index()) {
        case Left(err) => assert(err == "Users cannot be found!")
        case Right(users) => assert(users == Seq())
      }
    }
  }

  "A bad .index request" should {
    "return an error" in {
      (() => mockRepository.index()).expects().returning(Future(Left("Users cannot be found!"))).once()
      whenReady(testService.index()) {
        case Left(err) => assert(err == "Users cannot be found!")
        case Right(users) => assert(users == Seq())
      }
    }
  }

  "create" should {
    "return the user data after succeeding in creating an entry in the DB" in {
      (mockRepository.create _)
        .expects(me.as[UserModel])
        .returning(Future(Some(me.as[UserModel])))
        .once()
      val request: FakeRequest[JsValue] =
        buildPost("/create")
          .withBody[JsValue](Json.toJson(me))
      whenReady(testService.create(request)) {
        case Left(err) => assert(err == "ERROR: User not created.")
        case Right(dataModel) => assert(Json.toJson(dataModel) == me)
      }
    }
  }

  "a bad create request" should {
    "return an error string after failing to create an entry in the DB" in {
      (mockRepository.create _)
        .expects(*)
        .returning(Future(None))
        .once()
      val request: FakeRequest[JsValue] =
        buildPost("/create")
          .withBody[JsValue](Json.toJson(me))
      whenReady(testService.create(request)) {
        case Left(err) => assert(err == "ERROR: User not created.")
        case Right(userModel) => assert(Json.toJson(userModel) == me)
      }
    }
  }

  "a bad json obj validation for the create request" should {
    "return an error string after failing to create an entry in the DB" in {
      val request: FakeRequest[JsValue] =
        buildPost("/create")
          .withBody[JsValue](Json.toJson(badJsonObj))
      whenReady(testService.create(request)) {
        case Left(err) => assert(err == "ERROR: User not created.")
        case Right(userModel) => assert(Json.toJson(userModel) == me)
      }
    }
  }

  "read" should {
    "return the user after succeeding in reading an entry in the DB" in {
      (mockRepository.read _)
        .expects(*)
        .returning(Future(Some(me.as[UserModel])))
        .once()
      whenReady(testService.read("Vedant-N421")) {
        case Left(err) => assert(err == "ERROR: Unable to read user's profile.")
        case Right(dataModel) => assert(Json.toJson(dataModel) == me)
      }
    }
  }

  "a good readAny request" should {
    "return the user after succeeding in reading an entry in the DB" in {
      (mockRepository.readAny _)
        .expects(*, *)
        .returning(Future(Some(me.as[UserModel])))
        .once()
      whenReady(testService.readAny("id", 147396311)) {
        case Left(err) => assert(err == "ERROR: Unable to read user's profile.")
        case Right(dataModel) => assert(Json.toJson(dataModel) == me)
      }
    }
  }

  "a bad read request" should {
    "return an error string after failing to read an entry in the DB" in {
      (mockRepository.read _)
        .expects("blah")
        .returning(Future(None))
        .once()
      whenReady(testService.read("blah")) {
        case Left(err) => assert(err == "ERROR: Unable to read user's profile.")
        case Right(dataModel) => assert(Json.toJson(dataModel) == me)
      }
    }
  }

  "update" should {
    "return the new user data after succeeding in updating an entry in the DB" in {
      val mockUpdateResult = mock[UpdateResult]
      (mockRepository
        .update(_: String, _: UserModel))
        .expects(*, *)
        .returning(Future.successful(mockUpdateResult))
        .once()
      val updateRequest: FakeRequest[JsValue] =
        buildPost(s"/update/${me.as[UserModel].login}")
          .withBody[JsValue](Json.toJson(me2.as[UserModel]))
      whenReady(testService.update("Vedant-N421", request = updateRequest)) {
        case Left(err: String) => assert(err == "ERROR: User not updated.")
        case Right(userModel: UserModel) => assert(Json.toJson(userModel) == me2)
      }
    }
  }

  "a bad update request" should {
    "return an error message, failing to update an entry in the DB" in {
      val mockUpdateResult = mock[UpdateResult]
      (mockRepository
        .update(_: String, _: UserModel))
        .expects(*, *)
        .returning(Future.successful(mockUpdateResult))
        .once()
      val updateRequest: FakeRequest[JsValue] =
        buildPost(s"/update/${me.as[UserModel].login}")
          .withBody[JsValue](Json.toJson(me2.as[UserModel]))
      whenReady(testService.update("Vedant-N421", request = updateRequest)) {
        case Left(err: String) => assert(err == "ERROR: User not updated.")
        case Right(userModel: UserModel) => assert(Json.toJson(userModel) == me2)
      }
    }
  }

  "a bad json obj validation for the update request" should {
    "return an error string after failing to update an entry in the DB" in {
      val updateRequest: FakeRequest[JsValue] =
        buildPost(s"/update/${badJsonObj}")
          .withBody[JsValue](Json.toJson(badJsonObj))
      whenReady(testService.update("Vedant-N421", updateRequest)) {
        case Left(err) => assert(err == "ERROR: User not updated.")
        case Right(userModel) => assert(Json.toJson(userModel) == badJsonObj)
      }
    }
  }

  "partialUpdate with a good request" should {
    "should partially update an entry in the DB" in {
      val mockUpdateResult = mock[UpdateResult]
      (mockRepository
        .partialUpdate(_: String, _: String, _: String))
        .expects(*, *, *)
        .returning(Future(None))
        .once()

      whenReady(testService.partialUpdate("Vedant-N421", "id", 420)) {
        case Left(err: String) => assert(err == "ERROR: User not updated.")
        case Right(userModel: UserModel) => assert(Json.toJson(userModel) == me2)
      }
    }
  }

  "delete" should {
    "return the result of deleting an entry in the DB" in {
      val mockDeleteResult = mock[DeleteResult]
      (mockRepository.delete _)
        .expects("Vedant-N421")
        .returning(Future.successful(Right(mockDeleteResult)))
        .once()

      whenReady(testService.delete("Vedant-N421")) {
        case Left(err) => assert(err == "INFO: User not found, so no deletion made!")
        case Right(res) => assert(res == "INFO: User was deleted successfully.")
      }
    }
  }

  "a bad delete request" should {
    "return an error string informing the user that nothing was changed" in {
      val mockDeleteResult = mock[DeleteResult]
      (mockRepository.delete _)
        .expects(*)
        .returning(Future.successful(Left("INFO: User not found, so no deletion made!")))
        .once()

      whenReady(testService.delete("nothinghere")) {
        case Left(err) => assert(err == "INFO: User not found, so no deletion made!")
        case Right(res) => assert(res == "INFO: User was deleted successfully.")
      }
    }
  }

  "getContents given a single file" should {
    "should return the view model with a good request" in {
      val another = mock[ContentModel]
      (gitHubConnector
        .getContents(_: String, _: String, _: String)(_: OFormat[ContentModel], _: ExecutionContext))
        .expects(*, *, *, *, *)
        .returning(Future.successful(Right(List(another))))
        .once()

      whenReady(testService.getContents("Vedant-N421", "Scala-Training", "/Scala-101/build.sbt")) {
        case Right(cvm: ContentViewModel) => assert(cvm.path == "/Scala-101/build.sbt")
        case Left(err) => assert(err == "Could not connect to Github API.")
      }
    }
  }

  "getContents given multiple files" should {
    "should still return the relevant view model with a good request" in {
      val another = mock[ContentModel]
      val onemore = mock[ContentModel]
      val listFiles = List(another, onemore)
      (gitHubConnector
        .getContents(_: String, _: String, _: String)(_: OFormat[ContentModel], _: ExecutionContext))
        .expects(*, *, *, *, *)
        .returning(Future.successful(Right(listFiles)))
        .once()

      whenReady(testService.getContents("Vedant-N421", "Scala-Training", "/Scala-101/build.sbt")) {
        case Right(cvm: ContentViewModel) => assert(cvm.file == None)
        case Left(err) => assert(err == "Could not connect to Github API.")
      }
    }
  }

  "getContents given a bad request" should {
    "return an error" in {
      (gitHubConnector
        .getContents(_: String, _: String, _: String)(_: OFormat[ContentModel], _: ExecutionContext))
        .expects(*, *, *, *, *)
        .returning(Future(Left("Could not connect to Github API.")))
        .once()

      whenReady(testService.getContents("Vedant-N421", "Scala-Training", "/Scala-101/build.sbt")) {
        case Right(cvm: ContentViewModel) => assert(cvm.path == "/Scala-101/build.sbt")
        case Left(err) => assert(err == "Could not connect to Github API.")
      }
    }
  }

  "getRepos given a valid login" should {
    "return the view model for a list of repositories" in {
      val another = mock[RepoModel]
      (gitHubConnector
        .getRepos(_: String)(_: OFormat[RepoModel], _: ExecutionContext))
        .expects(*, *, *)
        .returning(Future(Right(List(another))))
        .once()

      whenReady(testService.getRepos("Vedant-N421")) {
        case Right(rlvm: RepoListViewModel) => assert(true)
        case Left(err) => assert(err == "Could not connect to Github API.")
      }
    }
  }

  "getRepos given an invalid login" should {
    "return the view model for a list of repositories" in {
      val another = mock[RepoModel]
      (gitHubConnector
        .getRepos(_: String)(_: OFormat[RepoModel], _: ExecutionContext))
        .expects(*, *, *)
        .returning(Future(Left("Could not connect to Github API.")))
        .once()

      whenReady(testService.getRepos("dsjofnskdofknsdnf")) {
        case Right(rlvm: RepoListViewModel) => assert(false)
        case Left(err) => assert(err == "Could not connect to Github API.")
      }
    }
  }
}
