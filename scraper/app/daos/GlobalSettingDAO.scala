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

  def upsert(globalSetting: GlobalSetting): Future[Unit] = db.run(GlobalSettings.insertOrUpdate(globalSetting)).map { _ => () }

  def insert(globalSetting: GlobalSetting): Future[Unit] = db.run(GlobalSettings += globalSetting).map { _ => () }

  def get(key: String): Future[Option[GlobalSetting]] = db.run(GlobalSettings.filter(_.key === key).result.headOption)

  class GlobalSettingsTable(tag: Tag) extends Table[GlobalSetting](tag, "GLOBAL_SETTING") {
    def key = column[String]("KEY", O.PrimaryKey)
    def value = column[String]("VALUE")
    def * = (key, value) <> (GlobalSetting.tupled, GlobalSetting.unapply)
  }
}
