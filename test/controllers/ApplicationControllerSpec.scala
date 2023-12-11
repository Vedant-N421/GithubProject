package controllers

import baseSpec.BaseSpecWithApplication
import models.UserModel
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsJson, defaultAwaitTimeout, status}

import scala.concurrent.Future

class ApplicationControllerSpec extends BaseSpecWithApplication with BeforeAndAfterEach {

  override def beforeEach(): Unit = await(repository.deleteAll())

  val TestApplicationController = new ApplicationController(
    component,
    repoService,
    connector
  )

  private val me: UserModel = UserModel(
    login = "Vedant-N421",
    id = 147396311,
    url = "https://api.github.com/users/Vedant-N421",
    node_id = "U_kgDOCMkW1w",
    created_at = "2023-10-09T11:03:37Z",
    followers = 1,
    following = 0
  )

  "ApplicationController .displayUser" should {
    "with a good request, return the JSON body for a valid user" in {
      beforeEach()
      val fetchedResult: Future[Result] = (TestApplicationController.displayUser("Vedant-N421")(FakeRequest()))
      assert(status(fetchedResult) == Status.OK)
      assert(contentAsJson(fetchedResult).as[JsValue] == Json.toJson(me))
    }
  }

}
