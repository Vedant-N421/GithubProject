package services

import baseSpec.BaseSpec
import com.mongodb.client.result.UpdateResult
import models.UserModel
import org.mongodb.scala.result.DeleteResult
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.{DELETE, GET, POST, PUT}
import repositories._
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.{ExecutionContext, Future}

class RepositoryServiceSpec extends BaseSpec with MockFactory with ScalaFutures with GuiceOneAppPerSuite {

  val mockRepository: UserRepoTrait = mock[UserRepoTrait]
  implicit val executionContext: ExecutionContext =
    app.injector.instanceOf[ExecutionContext]
  val testService = new RepositoryService(mockRepository)

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

  ".index" should {
    "return an empty sequence of users" in {
      (() => mockRepository.index()).expects().returning(Future(Right(Seq()))).once()
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
        case Left(err) => assert(err == "ERROR: Duplicate found, user not created.")
        case Right(dataModel) => assert(Json.toJson(dataModel) == me)
      }
    }
  }

  "read" should {
    "return the user after succeeding in reading an entry in the DB" in {
      (mockRepository.read _)
        .expects("Vedant-N421")
        .returning(Future(Some(me.as[UserModel])))
        .once()
      whenReady(testService.read("Vedant-N421")) {
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
        .expects("Vedant-N421", me2.as[UserModel])
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
}
