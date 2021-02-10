package v1.dataproduct

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

final case class DataProductData(id: DataProductId, title: String, body: String)

class DataProductId private(val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object DataProductId {
  def apply(raw: String): DataProductId = {
    require(raw != null)
    new DataProductId(Integer.parseInt(raw))
  }
}

class DataProductExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the PostRepository.
  */
trait DataProductRepository {
  def create(data: DataProductData)(implicit mc: MarkerContext): Future[DataProductId]

  def list()(implicit mc: MarkerContext): Future[Iterable[DataProductData]]

  def get(id: DataProductId)(implicit mc: MarkerContext): Future[Option[DataProductData]]
}

/**
  * A trivial implementation for the Post Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class DataProductRepositoryImpl @Inject()()(implicit ec: DataProductExecutionContext)
    extends DataProductRepository {

  private val logger = Logger(this.getClass)

  private val postList = List(
    DataProductData(DataProductId("1"), "title 1", "blog post 1"),
    DataProductData(DataProductId("2"), "title 2", "blog post 2"),
    DataProductData(DataProductId("3"), "title 3", "blog post 3"),
    DataProductData(DataProductId("4"), "title 4", "blog post 4"),
    DataProductData(DataProductId("5"), "title 5", "blog post 5")
  )

  override def list()(
      implicit mc: MarkerContext): Future[Iterable[DataProductData]] = {
    Future {
      logger.trace(s"list: ")
      postList
    }
  }

  override def get(id: DataProductId)(
      implicit mc: MarkerContext): Future[Option[DataProductData]] = {
    Future {
      logger.trace(s"get: id = $id")
      postList.find(post => post.id == id)
    }
  }

  def create(data: DataProductData)(implicit mc: MarkerContext): Future[DataProductId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
