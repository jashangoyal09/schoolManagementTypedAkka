package com.knoldus

import akka.actor.typed.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer

object StartAdmission {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem[Nothing](StartProcessing(), "typed-processor")
    val kafkaConsumerSettings: ConsumerSettings[String, String] =
      ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers("localhost:9092")
        .withGroupId("groupId")
    implicit val classicSystem = system.classicSystem
    val kafkaSource: Source[ConsumerRecord[String, String], Consumer.Control] =
      Consumer.plainSource(kafkaConsumerSettings, Subscriptions.topics("topic"))
    kafkaSource.collect{x=>
      print("\n\n<<<<<<<>>>>>>>>>\n"+x.value())
      x
    }.runWith(Sink.ignore)
//    val sink: Sink[String, NotUsed] = ActorSink.actorRefWithBackpressure(
//      ref = actor,
//      onCompleteMessage = Complete,
//      onFailureMessage = Fail.apply,
//      messageAdapter = Message.apply,
//      onInitMessage = Init.apply,
//      ackMessage = Ack)
  }
}
