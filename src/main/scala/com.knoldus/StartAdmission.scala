package com.knoldus

import akka.actor.typed.ActorSystem

object StartAdmission {
  def main(args: Array[String]): Unit = {

    ActorSystem[Nothing](StartProcessing(), "typed-processor")

  }
}
