package daos

import scala.concurrent.{ ExecutionContext, Future }
import javax.inject.Inject

import models.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

class UserDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  val Users = TableQuery[UsersTable]

  def all(): Future[Seq[User]] = db.run(Users.result)

  def insert(user: User): Future[Unit] = db.run(Users += user).map { _ => () }

  class UsersTable(tag: Tag) extends Table[User](tag, "USER") {
    def id = column[String]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def * = (id, name) <> (User.tupled, User.unapply)
  }
}
