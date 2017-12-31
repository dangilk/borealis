package daos

import scala.concurrent.{ ExecutionContext, Future }
import javax.inject.Inject

import models.UserCollection
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

class UserCollectionDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, userDAO: UserDAO, gameDAO: GameDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val UserCollections = TableQuery[UserCollectionsTable]

  def all(): Future[Seq[UserCollection]] = db.run(UserCollections.result)

  def upsert(userCollection: UserCollection): Future[Unit] = db.run(UserCollections.insertOrUpdate(userCollection)).map { _ => () }

  def insert(userCollection: UserCollection): Future[Unit] = db.run(UserCollections += userCollection).map { _ => () }

  private class UserCollectionsTable(tag: Tag) extends Table[UserCollection](tag, "USER_COLLECTION") {
    def id = column[String]("ID", O.PrimaryKey)
    def userId = column[String]("USER_ID")
    def gameId = column[String]("GAME_ID")
    def numPlays = column[Int]("NUM_PLAYS")
    def own = column[Boolean]("OWN")
    def prevOwned = column[Boolean]("PREV_OWNED")
    def forTrade = column[Boolean]("FOR_TRADE")
    def want = column[Boolean]("WANT")
    def wantToPlay = column[Boolean]("WANT_TO_PLAY")
    def wantToBuy = column[Boolean]("WANT_TO_BUY")
    def wishList = column[Boolean]("WISHLIST")
    def wishListPriority = column[Int]("WISHLIST_PRIORITY")
    def preOrdered = column[Boolean]("PRE_ORDERED")
    def lastModified = column[String]("LAST_MODIFIED")
    def userRating = column[Double]("USER_RATING")
    def * = (id, userId, gameId, numPlays, own, prevOwned,
      forTrade, want, wantToPlay, wantToBuy, wishList, wishListPriority,
      preOrdered, lastModified, userRating) <> (UserCollection.tupled, UserCollection.unapply)

    def user = foreignKey("USER", userId, userDAO.Users)(_.id)
    def game = foreignKey("GAME", gameId, gameDAO.Games)(_.id)
  }
}
