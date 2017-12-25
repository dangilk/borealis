package services

import java.time.{Clock, Instant}
import javax.inject._
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import scala.concurrent.Future
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.ws._
import play.api.Logger


@Singleton
class Scraper @Inject() (ws: WSClient) {
  val baseUrlApi2 = "https://www.boardgamegeek.com/xmlapi2"
  val baseUrlApi1 = "https://www.boardgamegeek.com/xmlapi"

  def getForumList() = {
    val forumListUrl = baseUrlApi2 + "/forumlist?id=%d&type=thing".format(10)
    val request: WSRequest = ws.url(forumListUrl)
    request.get().map { response =>
      // do a little xml parsing here for fun
      val test = (response.xml \ "forum").mkString
      Logger.info(s"got some xml: $test")
    }
  }

  getForumList()
}
