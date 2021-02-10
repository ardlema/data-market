package v1.dataproduct

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class DataProductFormInput(title: String, body: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class DataProductController @Inject()(cc: DataProductControllerComponents)(
    implicit ec: ExecutionContext)
    extends DataProductBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[DataProductFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "body" -> text
      )(DataProductFormInput.apply)(DataProductFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = PostAction.async { implicit request =>
    logger.trace("index: ")
    postResourceHandler.find.map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  def process: Action[AnyContent] = PostAction.async { implicit request =>
    logger.trace("process: ")
    processJsonPost()
  }

  def show(id: String): Action[AnyContent] = PostAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      postResourceHandler.lookup(id).map { post =>
        Ok(Json.toJson(post))
      }
  }

  private def processJsonPost[A]()(
      implicit request: DataProductRequest[A]): Future[Result] = {
    def failure(badForm: Form[DataProductFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: DataProductFormInput) = {
      postResourceHandler.create(input).map { post =>
        Created(Json.toJson(post)).withHeaders(LOCATION -> post.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
