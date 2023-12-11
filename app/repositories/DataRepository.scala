package repositories

import models.UserModel
import org.mongodb.scala.model.Filters.empty
import org.mongodb.scala.model.{IndexModel, Indexes}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataRepository @Inject()(mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[UserModel](
      collectionName = "dataModels",
      mongoComponent = mongoComponent,
      domainFormat = UserModel.formats,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("_id")
        )
      ),
      replaceIndexes = false
    ) {

  def deleteAll(): Future[Unit] =
    collection.deleteMany(empty()).toFuture().map(_ => ())
}
