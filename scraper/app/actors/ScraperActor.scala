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
import daos._
import models._
import scala.util.{Failure, Success}

object ScraperActor {
  def props = Props[ScraperActor]

  case class CheckStatus()
  case class DequeueNetworkRequest()
  case class EnqueueNetworkRequest(msg: Any)
  case class GetForumList(id: String)
  case class GetForum(id: String)
  case class GetThread(id: String)
  case class GetUser(username: String)
  case class GetCollection(user: User)
}

class ScraperActor @Inject() (ws: WSClient, as: ActorSystem, gameDAO: GameDAO, userDAO: UserDAO, userCollectionDAO: UserCollectionDAO,
  globalSettingsDAO: GlobalSettingDAO) extends Actor {
    val baseUrlApi2 = "https://www.boardgamegeek.com/xmlapi2"
    val baseUrlApi1 = "https://www.boardgamegeek.com/xmlapi"
    var requestQueue: Q[Any] = Q[Any]()
    val testMode = false
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
      case GetCollection(user) =>
      getCollection(user)
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
        val nameElem = xml \ "@name"
        val name = nameElem.text
        val idElem = xml \ "@id"
        val id = idElem.text
        if (name != null && !name.isEmpty && !exploredUsers.contains(id)) {
          val user : User = User(id, name)
          exploredUsers.add(id)
          userDAO.insert(user)
          self ! EnqueueNetworkRequest(GetCollection(user))
          // also explore user buddies here
        }
      }
    }

    def toBoolean(string: String) : Boolean = {
      !(string == null || "".equals(string) || "0".equals(string))
    }

    def getCollection(user: User) {
      val collectionUrl = baseUrlApi1 + "/collection/%s".format(user.name)
      val request: WSRequest = ws.url(collectionUrl)
      request.get().map { response =>
        if (response.status == 202) {
          // got a retry response
          self ! EnqueueNetworkRequest(GetCollection(user))
        } else if (response.status == 200) {
          try {
            val xml = response.xml
            //Logger.info(s"got collection: $xml")
            val itemElems = xml \ "item"
            Logger.info("items length: " + itemElems.length)
            itemElems.zipWithIndex foreach { case(item, i) =>
              if (!testMode || i == 0) {
                // game data:

                val gameId = (item \ "@objectid").text
                val name = (item \ "name").text
                Logger.info(s"game name: $name")
                val yearPublished = (item \ "yearpublished").text.toInt
                Logger.info(s"year published: $yearPublished")
                val imageUrl = (item \ "image").text
                val subType = (item \ "@subtype").text
                val statsElem = (item \ "stats")
                val minPlayers = (statsElem \ "@minplayers").text.toInt
                val maxPlayers = (statsElem \ "@maxplayers").text.toInt
                val minPlaytime = (statsElem \ "@minplaytime").text.toInt
                val maxPlaytime = (statsElem \ "@maxplaytime").text.toInt
                val playingTime = (statsElem \ "@playingtime").text.toInt
                val numOwned = (statsElem \ "@numowned").text.toInt
                val ratingElem = statsElem \ "rating"
                val usersRated = (ratingElem \ "usersrated" \ "@value").text.toInt
                val averageRating = (ratingElem \ "average" \ "@value").text.toDouble
                val bayesAverageRating = (ratingElem \ "bayesaverage" \ "@value").text.toDouble
                val stdDevRating = (ratingElem \ "stddev" \ "@value").text.toDouble
                val medianRating = (ratingElem \ "median" \ "@value").text.toDouble
                gameDAO.insert(Game(gameId, name, yearPublished, imageUrl,
                  subType, minPlayers, maxPlayers, minPlaytime, maxPlaytime,
                  playingTime, numOwned, usersRated, averageRating, bayesAverageRating,
                  stdDevRating, medianRating)).onComplete({case Success(value) => Logger.info("inserted game" + value)
                  case Failure(e) => Logger.info("error inserting game", e)})
                  Logger.info(s"inserted game $name")

                  // user collection data:
                  val collectionId = (item \ "@collid").text
                  val numPlays = (item \ "numplays").text.toInt
                  val statusElem = item \ "status"
                  val own = toBoolean((statusElem \ "@own").text)
                  val prevOwned = toBoolean((statusElem \ "@prevowned").text)
                  val forTrade = toBoolean((statusElem \ "@fortrade").text)
                  val want = toBoolean((statusElem \ "@want").text)
                  val wantToPlay = toBoolean((statusElem \ "@wanttoplay").text)
                  val wantToBuy = toBoolean((statusElem \ "@wanttobuy").text)
                  val wishlist = toBoolean((statusElem \ "@wishlist").text)
                  val wishlistPriority = if (wishlist) (statusElem \ "@wishlistpriority").text.toInt else -1
                  val preOrdered = toBoolean((statusElem \ "@preordered").text)
                  val lastModified = (statusElem \ "@lastModified").text
                  val userRating = (ratingElem \ "@value").text.toDouble
                  userCollectionDAO.insert(UserCollection(collectionId, user.id, gameId, numPlays,
                    own, prevOwned, forTrade, want, wantToPlay, wantToBuy,
                    wishlist, wishlistPriority, preOrdered, lastModified, userRating))
                    Logger.info(s"inserted collection $collectionId")
                  }
                }
              } catch {
                case e: Throwable => {
                  Logger.info("error", e)
                  self ! EnqueueNetworkRequest(GetCollection(user))
                }
              }
            }
          }
        }

        // start the scraper
        self ! EnqueueNetworkRequest(GetForumList("10"))
      }
