package v1.dataproduct

import javax.inject.{Inject, Provider}

import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

/**
  * DTO for displaying post information.
  */
case class DataProductResource(id: String, link: String, title: String, body: String)

object DataProductResource {
  /**
    * Mapping to read/write a PostResource out as a JSON value.
    */
    implicit val format: Format[DataProductResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[DataProductResource]]
  */
class DataProductResourceHandler @Inject()(
                                     routerProvider: Provider[DataProductRouter],
                                     postRepository: DataProductRepository)(implicit ec: ExecutionContext) {

  def create(dataProductInput: DataProductFormInput)(
      implicit mc: MarkerContext): Future[DataProductResource] = {
    val data = DataProductData(DataProductId("999"), dataProductInput.title, dataProductInput.body)
    // We don't actually create the post, so return what we have
    postRepository.create(data).map { id =>
      createPostResource(data)
    }
  }

  def lookup(id: String)(
      implicit mc: MarkerContext): Future[Option[DataProductResource]] = {
    val postFuture = postRepository.get(DataProductId(id))
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createPostResource(postData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[DataProductResource]] = {
    postRepository.list().map { postDataList =>
      postDataList.map(postData => createPostResource(postData))
    }
  }

  private def createPostResource(p: DataProductData): DataProductResource = {
    DataProductResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.body)
  }

}
