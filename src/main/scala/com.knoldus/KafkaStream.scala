//package com.knoldus
//
//import akka.NotUsed
//import akka.actor.typed.{ActorSystem, SpawnProtocol}
//import akka.kafka.{ConsumerMessage, ProducerMessage}
//import akka.kafka.ConsumerMessage.CommittableOffset
//import akka.kafka.scaladsl.Consumer
//import akka.stream.Materializer
//import akka.stream.scaladsl.{Sink, Source}
//import akka.util.Timeout
//import com.knoldus.model.UserData
//import org.apache.kafka.common.TopicPartition
//
//import scala.concurrent.ExecutionContext
//
//class KafkaStream(kafkaIO: KafkaIO)(implicit system: ActorSystem[SpawnProtocol.Command],
//                                    ec: ExecutionContext, t: Timeout) {
//
//  type KafkaEvent    = (CommittableOffset, UserData)
//  type KafkaEnvelope = ProducerMessage.Envelope[String, String, CommittableOffset]
//  type KafkaSource   = Source[ConsumerMessage.CommittableMessage[String, String], NotUsed]
//
//  private implicit val mat: Materializer = Materializer(system)
//  private val partitionedKafkaSource: Source[(TopicPartition, Source[kafkaIO.KafkaMessage, NotUsed]), Consumer.Control]
//  = kafkaIO.makeSource
//
//  partitionedKafkaSource.collect {
//    case UserData(msg) => msg
//  }
//    .to(Sink.foreach(println))
//    .run()
//}
