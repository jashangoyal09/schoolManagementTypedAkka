package com.knoldus

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.knoldus.SchoolManagement.Confirmation

object StartAdmission {
  def main(args: Array[String]): Unit = {

    ActorSystem[Nothing](StartProcessing(), "typed-processor")

  }
}
