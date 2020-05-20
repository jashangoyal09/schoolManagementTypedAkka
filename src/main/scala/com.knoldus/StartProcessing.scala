package com.knoldus

import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId

object StartProcessing {
  def apply() = Behaviors.setup[Nothing] { context =>
//
    for (i <- 1 to 10) {
      val a = context.spawn(UserActor("", PersistenceId("", "")), s"userData-$i")
      a ! UserActor.AddUser("user 123", "jashan", "jashan@gmail.com")
    }
//    val sys = ActorSystem(SchoolManagement("123"), "admission")
//    sys ! SchoolManagement.AddSubject("CSE101","DataStructures",aRef)
//    sys ! SchoolManagement.Get(aRef)
//    sys ! SchoolManagement.Admission(aRef)

    Behaviors.empty
  }
}
