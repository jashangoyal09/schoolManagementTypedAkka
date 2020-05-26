package com.knoldus

import akka.actor
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.typed.scaladsl.{ActorMaterializer, ActorSource}
import com.knoldus.model.KafkaConfig

trait Protocol
case class Message(msg: String) extends Protocol
case object Complete extends Protocol
case class Fail(ex: Exception) extends Protocol


object KafkaConsumer {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "ActorSourceSinkExample")
  implicit val classicSystem: actor.ActorSystem = system.classicSystem

  val kafkaConfig = KafkaConfig("expense-stream-client","expense-stream-group","localhost:9092",
    "transactions-source","transactions-destination")
  val kafkaIO = new KafkaIO(kafkaConfig)


  val source: Source[Protocol, ActorRef[Protocol]] = ActorSource.actorRef[Protocol](completionMatcher = {
      case Complete =>
    }, failureMatcher = {
      case Fail(ex) => ex
    }, bufferSize = 8, overflowStrategy = OverflowStrategy.fail)

    val ref = source
      .collect {
        case Message(msg) => msg
      }
      .to(Sink.foreach(println))
      .run()

    ref ! Message("msg1")
  }