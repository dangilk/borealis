package utils

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
class NetworkQueue @Inject() (ws: WSClient) {
  case class QueuedRequest(request: WSRequest, task: Runnable)
}
