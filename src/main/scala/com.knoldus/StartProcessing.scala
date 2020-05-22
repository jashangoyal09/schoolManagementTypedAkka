package com.knoldus

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.{ClusterSingleton, SingletonActor}
import akka.persistence.typed.PersistenceId

object StartProcessing {
    def apply() = Behaviors.setup[Nothing] { context =>

      val system: ActorRef[SystemActor.SystemRequest] =
        ClusterSingleton(context.system).init(SingletonActor(
          SystemActor("System-Id"), "system-actor"))

      for (i <- 1 to 10) {
        context.spawn(UserActor(system), s"handling$i")
      }
      Behaviors.empty
    }
  }
