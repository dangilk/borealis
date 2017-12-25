import com.google.inject.AbstractModule
import java.time.Clock
import play.api._
import services.{Scraper}
import play.api.libs.concurrent.AkkaGuiceSupport
import actors.{ScraperActor}
/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    //bind(classOf[Scraper]).asEagerSingleton()
    bindActor[ScraperActor]("scraper-actor")
  }

}
