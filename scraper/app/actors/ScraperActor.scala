package actors
import akka.actor._
import javax.inject._
import play.api.Logger
import akka.pattern.ask
import ScraperActor._
import play.api.libs.ws._
import scala.concurrent._
import ExecutionContext.Implicits.global

object ScraperActor {
  def props = Props[ScraperActor]

  case class CheckStatus()
}

class ScraperActor @Inject() (ws: WSClient) extends Actor {
  val baseUrlApi2 = "https://www.boardgamegeek.com/xmlapi2"
  val baseUrlApi1 = "https://www.boardgamegeek.com/xmlapi"
  Logger.info("create scraper actor");

  // allow other controllers to ask about the status of this actor. not strictly required...
  def receive = {
    case CheckStatus() =>
      sender() ! "Hello"
  }

  // get the top level forumlist info
  def getForumList(id: Int) = {
    val forumListUrl = baseUrlApi2 + "/forumlist?id=%d&type=thing".format(id)
    val request: WSRequest = ws.url(forumListUrl)
    request.get().map { response =>
      // do a little xml parsing here for fun
      val test = (response.xml \ "forum").mkString
      Logger.info(s"got some xml: $test")
    }
  }

  // start the scraper
  getForumList(10)
}
