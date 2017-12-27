package actors
import akka.actor._
import javax.inject._
import play.api.Logger
import akka.pattern.ask
import ScraperActor._
import play.api.libs.ws._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
// we can use a mutable queue because we only modify it within atomic message sends
import scala.collection.mutable.{ Queue => Q }
import scala.collection.mutable.Set

object ScraperActor {
  def props = Props[ScraperActor]

  case class CheckStatus()
  case class DequeueNetworkRequest()
  case class EnqueueNetworkRequest(msg: Any)
  case class GetForumList(id: String)
  case class GetForum(id: String)
  case class GetThread(id: String)
  case class GetUser(username: String)
  case class GetCollection(username: String)
}

class ScraperActor @Inject() (ws: WSClient, as: ActorSystem) extends Actor {
  val baseUrlApi2 = "https://www.boardgamegeek.com/xmlapi2"
  val baseUrlApi1 = "https://www.boardgamegeek.com/xmlapi"
  var requestQueue: Q[Any] = Q[Any]()
  val testMode = true
  val exploredUsers: Set[String] = Set[String]()

  Logger.info("create scraper actor");

  override def preStart(): Unit = {
    as.scheduler.schedule(
      initialDelay = 1 seconds,
      interval = 5 seconds,
      receiver = self,
      message = DequeueNetworkRequest()
    )
  }

  def receive = {
    case CheckStatus() =>
      sender() ! "Hello"
    case DequeueNetworkRequest() => {
      if (!requestQueue.isEmpty) {
        val msg = requestQueue.dequeue
        self ! msg
      }
    }
    case EnqueueNetworkRequest(msg) => {
      requestQueue.enqueue(msg)
    }
    case GetForumList(id) =>
      getForumList(id)
    case GetForum(id) =>
      getForum(id)
    case GetThread(id) =>
      getThread(id)
    case GetUser(username) =>
      getUser(username)
    case GetCollection(username) =>
      getCollection(username)
  }

  // get the top level forumlist info
  def getForumList(id: String) = {
    Logger.info(s"getForumList($id)")
    val forumListUrl = baseUrlApi2 + "/forumlist?id=%s&type=thing".format(id)
    val request: WSRequest = ws.url(forumListUrl)
    val response: Future[WSResponse] = request.get()
    response.map { response =>
      val xml = response.xml
      val forumsElem = xml \ "forum"
      forumsElem.zipWithIndex foreach { case(n, i) =>
        if (!testMode || i == 0) {
          val idElem = n \ "@id"
          val id = idElem.text
          Logger.info(s"forumId: $id")
          //getForum(id)
          self ! EnqueueNetworkRequest(GetForum(id))
        }
      }
    }
  }

  def getForum(id: String) = {
    Logger.info(s"getForum($id)")
    val forumUrl = baseUrlApi2 + "/forum?id=%s".format(id)
    val request: WSRequest = ws.url(forumUrl)
    request.get().map { response =>
      val xml = response.xml
      val forumIdElem = xml \ "@id"
      val forumId = forumIdElem.text
      if (forumId == "0") {
        // reached end of list, reset current forum list
      }
      val threadsElem = xml \ "threads" \ "thread"
      threadsElem.zipWithIndex foreach { case(n, i) =>
        if (!testMode || i == 0) {
          val idElem = n \ "@id"
          val id = idElem.text
          Logger.info(s"threadId: $id")
          self ! EnqueueNetworkRequest(GetThread(id))
        }
      }
    }
  }

  def getThread(id: String) = {
    val threadUrl = baseUrlApi2 + "/thread?id=%s".format(id)
    val request: WSRequest = ws.url(threadUrl)
    request.get().map { response =>
      val xml = response.xml
      val articlesElems = xml \ "articles" \ "article"
      articlesElems.zipWithIndex foreach { case(n, i) =>
        if (!testMode || i == 0) {
          val usernameElem = n \ "@username"
          val username = usernameElem.text
          self ! EnqueueNetworkRequest(GetUser(username))
        }
      }
    }
  }

  def getUser(username: String) {
    Logger.info(s"getUser $username")
    val userUrl = baseUrlApi2 + "/user?name=%s".format(username)
    val request: WSRequest = ws.url(userUrl)
    request.get().map { response =>
      val xml = response.xml
      Logger.info(s"user xml: $xml")
      val nameElem = xml \ "@name"
      val name = nameElem.text
      val idElem = xml \ "@id"
      val id = idElem.text
      if (name != null && !name.isEmpty && !exploredUsers.contains(id)) {
        exploredUsers.add(id)
        self ! EnqueueNetworkRequest(GetCollection(username))
        // also explore user buddies here
      }
    }
  }

  def getCollection(username: String) {

  }

  // start the scraper
  self ! EnqueueNetworkRequest(GetForumList("10"))
}
