package controllers

import baseSpec.BaseSpecWithApplication
import models.UserModel
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.http.Status.OK
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

  private val me2: UserModel = UserModel(
    login = "Vedant-N124",
    id = 147396311,
    url = "A different URL",
    node_id = "This is different",
    created_at = "2023-10-09T11:03:37Z",
    followers = 1,
    following = 0
  )

  private val baddy: String = "A very bad string indeed."

  "ApplicationController .index" should {
    "return 200 OK" in {
      beforeEach()
      val result = TestApplicationController.index()(FakeRequest())
      assert(status(result) == Status.OK)
    }
  }

  "ApplicationController .displayUser" should {
    "with a good request, return an OK status page and return the view page" in {
      beforeEach()
      val fetchedResult: Future[Result] = (TestApplicationController.displayUser("Vedant-N421")(FakeRequest()))
      assert(status(fetchedResult) == Status.OK)
//      assert(contentAsJson(fetchedResult).as[JsValue] == Json.toJson(me))
    }
  }

  "ApplicationController .create" should {
    "create a user in the database" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildPost("/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Future[Result] = (TestApplicationController.create()(request))
      assert(status(createdResult) == Status.CREATED)
      assert(contentAsJson(createdResult).as[JsValue] == Json.toJson(me))
    }
  }

  "ApplicationController .create with a bad request" should {
    "return an error" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildPost("/create").withBody[JsValue](Json.toJson(baddy))
      val createdResult: Result = await(TestApplicationController.create()(request))
      assert(createdResult.header.status == Status.BAD_REQUEST)
    }
  }

  "ApplicationController .create duplicate request" should {
    "create a user in the database and then prevent/catch the duplicate request" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildPost("/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      assert(createdResult.header.status == Status.CREATED)

      val anotherResult: Result = await(TestApplicationController.create()(request))
      assert(anotherResult.header.status == Status.BAD_REQUEST)

    }
  }

  "ApplicationController .read" should {
    "find a user in the database by id" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildPost(s"/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      assert(createdResult.header.status == Status.CREATED)

      val readResult: Future[Result] = TestApplicationController.read("Vedant-N421")(FakeRequest())
      assert(status(readResult) == Status.OK)
      assert(contentAsJson(readResult).as[JsValue] == Json.toJson(me))
    }
  }

  "ApplicationController .read with a bad request" should {
    "return an error" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildPost(s"/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      assert(createdResult.header.status == Status.CREATED)

      val readResult: Future[Result] = TestApplicationController.read(baddy)(FakeRequest())
      assert(status(readResult) == Status.BAD_REQUEST)
      assert(contentAsJson(readResult).as[JsValue] == Json.toJson("ERROR: Unable to read user's profile."))
    }
  }

  "ApplicationController .readAny" should {
    "find a user in the database by their id" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildPost(s"/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      assert(createdResult.header.status == Status.CREATED)

      val readResult: Future[Result] =
        TestApplicationController.readAny("id", 147396311)(FakeRequest())
      assert(status(readResult) == Status.OK)
      assert(contentAsJson(readResult).as[JsValue] == Json.toJson(me))
    }
  }

  "ApplicationController .readAny" should {
    "find a user in the database by node_id" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildPost(s"/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      assert(createdResult.header.status == Status.CREATED)

      val readResult: Future[Result] =
        TestApplicationController.readAny("node_id", "U_kgDOCMkW1w")(FakeRequest())
      assert(status(readResult) == Status.OK)
      assert(contentAsJson(readResult).as[JsValue] == Json.toJson(me))
    }
  }

  "ApplicationController .readAny with a bad request" should {
    "return an error" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildPost(s"/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      assert(createdResult.header.status == Status.CREATED)

      val readResult: Future[Result] =
        TestApplicationController.readAny("name", baddy)(FakeRequest())
      assert(status(readResult) == Status.BAD_REQUEST)
      assert(contentAsJson(readResult).as[JsValue] == Json.toJson("ERROR: Unable to read user's profile."))
    }
  }

  "ApplicationController .update()" should {
    "update a user in the database by login" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildPost(s"/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      assert(createdResult.header.status == Status.CREATED)

      val updateRequest: FakeRequest[JsValue] =
        buildPost(s"/update/${me.login}").withBody[JsValue](Json.toJson(me2))
      val updateResult: Result =
        await(TestApplicationController.update(login = "Vedant-N421")(updateRequest))

      // Check if request was accepted
      assert(updateResult.header.status == Status.ACCEPTED)

      // Need to make sure that field actually did change!
      val readResult: Future[Result] = TestApplicationController.read(me2.login)(FakeRequest())
      assert(
        contentAsJson(readResult).as[JsValue] == Json.toJson(me2)
      )

    }
  }

  "ApplicationController .update() with a bad request" should {
    "return an error" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildPost(s"/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      assert(createdResult.header.status == Status.CREATED)

      val updateRequest: FakeRequest[JsValue] =
        buildPost(s"/update/${me.login}").withBody[JsValue](Json.toJson(baddy))
      val updateResult: Result =
        await(TestApplicationController.update(baddy)(updateRequest))

      // Check if request was accepted
      assert(updateResult.header.status == Status.BAD_REQUEST)
    }
  }

  "ApplicationController .partialUpdate()" should {
    "partially update a field of a user in the database by login" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildPost(s"/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      assert(createdResult.header.status == Status.CREATED)

      val updateRequest: FakeRequest[JsValue] =
        buildPost(s"/update/${me.login}").withBody[JsValue](Json.toJson(me))
      val updateResult: Result =
        await(
          TestApplicationController.partialUpdate("Vedant-N421", "created_at", "replaced")(updateRequest)
        )
      // Check if request was accepted
      assert(updateResult.header.status == Status.ACCEPTED)

      // Need to make sure that field actually did change!
      val readResult: Future[Result] = TestApplicationController.read("Vedant-N421")(FakeRequest())
      assert(
        contentAsJson(readResult).as[JsValue] == Json.toJson(
          me.copy(created_at = "replaced")
        )
      )
    }
  }

  "ApplicationController .partialUpdate() with a request that does not find a user" should {
    "return a bad request and not update any users in the database" in {
      beforeEach()

      val request: FakeRequest[JsValue] =
        buildPost(s"/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      assert(createdResult.header.status == Status.CREATED)

      val updateRequest: FakeRequest[JsValue] =
        buildPost(s"/update/${me.login}").withBody[JsValue](Json.toJson(me))
      val updateResult: Result =
        await(
          TestApplicationController.partialUpdate(login = "hiafnadfjn", "created_at", "replaced")(
            updateRequest
          )
        )
      assert(updateResult.header.status == Status.BAD_REQUEST)
      // Verify nothing changed
      val readResult: Future[Result] = TestApplicationController.read(me.login)(FakeRequest())
      assert(
        contentAsJson(readResult).as[JsValue] == Json.toJson(me)
      )
    }
  }

  "ApplicationController .partialUpdate() with a good request but bad field change" should {
    "not make any updates to the user in the database" in {
      beforeEach()

      val request: FakeRequest[JsValue] =
        buildPost(s"/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      assert(createdResult.header.status == Status.CREATED)

      val updateRequest: FakeRequest[JsValue] =
        buildPost(s"/update/${me.login}").withBody[JsValue](Json.toJson(me))
      val updateResult: Result =
        await(
          TestApplicationController.partialUpdate(login = "Vedant-N421", "non-existent field", "replaced")(
            updateRequest
          )
        )
      // Check if update status was correct
      assert(updateResult.header.status == Status.ACCEPTED)

      // Check if book was not edited and stays the same
      val readResult: Future[Result] = TestApplicationController.read("Vedant-N421")(FakeRequest())
      assert(
        contentAsJson(readResult).as[JsValue] == Json.toJson(me)
      )
    }
  }

  "ApplicationController .delete()" should {
    "delete a user in the database by their login" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildPost(s"/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      // and we check that it actually does get made
      assert(createdResult.header.status == Status.CREATED)

      // and then we check if it gets deleted
      val deleteResult: Result =
        await(TestApplicationController.delete(me.login)(FakeRequest()))
      assert(deleteResult.header.status == Status.ACCEPTED)

    }
  }

  "ApplicationController .delete() with a bad request" should {
    "return an error" in {
      beforeEach()
      val request: FakeRequest[JsValue] =
        buildGet(s"/create").withBody[JsValue](Json.toJson(me))
      val createdResult: Result = await(TestApplicationController.create()(request))
      // and we check that it actually does get made
      assert(createdResult.header.status == Status.CREATED)

      // and the code will still return an accepted if it found nothing to delete
      val deleteResult: Result =
        await(TestApplicationController.delete(baddy)(FakeRequest()))
      assert(deleteResult.header.status == Status.BAD_REQUEST)
    }
  }

}
