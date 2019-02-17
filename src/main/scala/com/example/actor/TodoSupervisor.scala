package com.example.actor

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{ Actor, ActorLogging, OneForOneStrategy, Props, SupervisorStrategy, Terminated }

import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

object TodoSupervisor {
  def props(maxRetries: Int, timeRange: Duration) = Props(new TodoSupervisor(maxRetries, timeRange))
  final case class RegistrationCommand(props: Props)
}

class TodoSupervisor(maxRetries: Int, timeRange: Duration) extends Actor with ActorLogging {

  import TodoSupervisor._

  // 例外はログに出して継続
  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = maxRetries, withinTimeRange = timeRange) {
      case NonFatal(ex) =>
        log.error(s"supervisor caught error. resume children. error: ${ex.getMessage}")
        Resume
    }

  override def receive: Receive = {
    case RegistrationCommand(props) =>
      log.info("receive registration command.")
      context.watch(context.actorOf(props, TODO_ACTOR_NAME))

    case cmd: TodoActor.Command =>
      context.child(TODO_ACTOR_NAME).foreach(_ forward cmd)

    case Terminated(child) =>
      log.warning(s"terminated child. path: ${child.path}")

    case unknown =>
      log.error(s"receive unknown type. type: ${unknown.getClass.getName}")
  }

}
