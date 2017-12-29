package daos

import scala.concurrent.{ ExecutionContext, Future }
import javax.inject.Inject

import models.Game
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

class GameDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  val Games = TableQuery[GamesTable]

  def all(): Future[Seq[Game]] = db.run(Games.result)

  def insert(game: Game): Future[Unit] = db.run(Games += game).map { _ => () }

  class GamesTable(tag: Tag) extends Table[Game](tag, "GAME") {
    def id = column[String]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def yearPublished = column[Int]("YEAR_PUBLISHED")
    def imageUrl = column[String]("IMAGE_URL")
    def subType = column[String]("SUBTYPE")
    def minPlayers = column[Int]("MIN_PLAYERS")
    def maxPlayers = column[Int]("MAX_PLAYERS")
    def minPlaytime = column[Int]("MIN_PLAYTIME")
    def maxPlaytime = column[Int]("MAX_PLAYTIME")
    def playingTime = column[Int]("PLAYING_TIME")
    def numOwned = column[Int]("NUM_OWNED")
    def ratingCount = column[Int]("RATING_COUNT")
    def averageRating = column[Double]("AVERAGE_RATING")
    def bayesAverageRating = column[Double]("BAYES_AVERAGE_RATING")
    def stdDevRating = column[Double]("STD_DEV_RATING")
    def medianRating = column[Double]("MEDIAN_RATING")
    def * = (id, name, yearPublished, imageUrl, subType, minPlayers,
      maxPlayers, minPlaytime, maxPlaytime, playingTime, numOwned,
      ratingCount, averageRating, bayesAverageRating, stdDevRating,
      medianRating) <> (Game.tupled, Game.unapply)
  }
}
