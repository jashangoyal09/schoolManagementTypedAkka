package com.knoldus

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.{ClusterSingleton, SingletonActor}
import akka.persistence.typed.PersistenceId

object StartProcessing {
  def apply() = Behaviors.setup[Nothing] { context =>
//
//    val sys = ActorSystem(SchoolManagement("123"), "admission")
//    sys ! SchoolManagement.AddSubject("CSE101","DataStructures",aRef)
//    sys ! SchoolManagement.Get(aRef)
//    sys ! SchoolManagement.Admission(aRef)
    for (i <- 1 to 10) {
      val a = context.spawn(UserActor("", PersistenceId("", "")), s"creditCardProcessor-$i")
      a ! UserActor.AddUser("user 123", "jashan", "jashan@gmail.com")
    }
    Behaviors.empty
  }
}
