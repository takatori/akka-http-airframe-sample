package com.example.actor

import akka.actor.{ Actor, ActorRef, Status }
import akka.pattern.PipeToSupport
import scalaz.{ -\/, \/, \/- }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

// Future[\/[L, R]]をpipeで送れるようにサポート
trait EitherPipeToSupport extends PipeToSupport {

  final class EitherPipeableFuture[L <: Throwable, R](val future: Future[\/[L, R]])(
    implicit
    ec: ExecutionContext) {

    // EitherがLeftの場合はakka.actor.Status.Failureへ変換
    def pipeTo(recipient: ActorRef)(implicit sender: ActorRef = Actor.noSender): Future[\/[L, R]] = {
      future andThen {
        case Success(\/-(r)) => recipient ! r
        case Success(-\/(r)) => recipient ! Status.Failure(r)
        case Failure(f) => recipient ! Status.Failure(f)
      }
    }

    def to(recipient: ActorRef): EitherPipeableFuture[L, R] = to(recipient, Actor.noSender)

    def to(recipient: ActorRef, sender: ActorRef): EitherPipeableFuture[L, R] = {
      pipeTo(recipient)(sender)
      this
    }
  }

  // Future[\/[L, R]] -> EitherPipeableFuture
  implicit def eitherPipe[L <: Throwable, R](future: Future[\/[L, R]])(implicit ec: ExecutionContext): EitherPipeableFuture[L, R] =
    new EitherPipeableFuture(future)

}
