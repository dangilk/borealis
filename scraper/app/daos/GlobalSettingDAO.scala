package daos

import scala.concurrent.{ ExecutionContext, Future }
import javax.inject.Inject

import models.GlobalSetting
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

class GlobalSettingDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  val GlobalSettings = TableQuery[GlobalSettingsTable]

  def all(): Future[Seq[GlobalSetting]] = db.run(GlobalSettings.result)

  def insert(globalSetting: GlobalSetting): Future[Unit] = db.run(GlobalSettings += globalSetting).map { _ => () }

  class GlobalSettingsTable(tag: Tag) extends Table[GlobalSetting](tag, "GLOBAL_SETTING") {
    def key = column[String]("KEY", O.PrimaryKey)
    def value = column[String]("VALUE")
    def * = (key, value) <> (GlobalSetting.tupled, GlobalSetting.unapply)
  }
}
