package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.ws._
import scala.concurrent.Future
import scala.concurrent._
import ExecutionContext.Implicits.global
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) (ws: WSClient) extends AbstractController(cc) {

  val baseUrlApi2 = "https://www.boardgamegeek.com/xmlapi2"
  val baseUrlApi1 = "https://www.boardgamegeek.com/xmlapi"
  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def getForumList() = Action.async {
    val forumListUrl = baseUrlApi2 + "/forumlist?id=%d&type=thing".format(10)
    val request: WSRequest = ws.url(forumListUrl)
    request.get().map { response =>
      // do a little xml parsing here for fun
      Ok((response.xml \ "forum").mkString)
    }
  }

  /**
  func getXml(url string, processor XmlProcessor) {
	getXmlRecursive(url, processor, 0)
}

func getXmlRecursive(url string, processor XmlProcessor, retries int) {
	if retries > 100 {
		logToFile("gave up after too many retries")
		return
	}
	// throttle requests a little
	time.Sleep(5 * time.Second)
	response, err := http.Get(url)
	if err != nil {
		retryGetXml(err, "error getting response - waiting for retry", url, processor, 30, retries)
		return
	} else {
		defer response.Body.Close()
		statusCode := response.StatusCode
		if statusCode == 200 {
			body, err := ioutil.ReadAll(response.Body)
			if err != nil {
				retryGetXml(err, "error reading response - waiting for retry", url, processor, 30, retries)
			} else {
				processor(body)
			}
		} else if statusCode == 202 {
			retryGetXml(err, "received 202 - waiting for retry", url, processor, 5, retries)
		} else if statusCode == 400 {
			logToFile("received error 400 - aborting")
		} else {
			retryGetXml(err, fmt.Sprintf("server error %d - waiting for retry", statusCode), url, processor, 30, retries)
		}
	}
}

func retryGetXml(err error, retryMsg string, url string, processor XmlProcessor, sleepSeconds int, retries int) {
	if err != nil {
		logToFile(err, retryMsg)
	}
	time.Sleep(time.Duration(sleepSeconds) * time.Second)
	getXmlRecursive(url, processor, 1+retries)
}*/
}
