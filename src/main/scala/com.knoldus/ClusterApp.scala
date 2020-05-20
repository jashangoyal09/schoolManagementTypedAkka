package com.knoldus

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.persistence.typed.PersistenceId
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

class ClusterApp(system: ActorSystem[_]) {

}
