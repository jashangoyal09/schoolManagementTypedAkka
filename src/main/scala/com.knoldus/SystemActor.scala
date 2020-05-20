package com.knoldus

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}

import scala.concurrent.duration._

object SystemActor {

  val commandHandler: (SystemState, Command) => Effect[Event, SystemState] = { (state, command) =>
    command match {
      case cmd: AddSystem => {
        print(s"Request to Add new system ${cmd.name}")
        Effect.persist(SystemAdded(cmd.systemId, cmd.name, cmd.email))
      }
      //        .thenReply(state=> cmd)
    }
  }
  val eventHandler: (SystemState, Event) => SystemState = { (state, event) =>
    event match {
      case SystemAdded(systemId, name, emailId) => {
        print(s"\nSystem ${name} has been persisted to the state\n")
        state.copy(systemId = systemId, name = name, emailId = emailId)
      }
    }
  }

  def apply(systemId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, SystemState](
      PersistenceId("System", systemId),
      SystemState.empty,
      (state, command) => commandHandler(state, command),
      (state, event) => eventHandler(state, event))
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
  }

  sealed trait Command extends CborSerializable

  sealed trait Event extends CborSerializable {
    def systemId: String
  }

  final case class SystemState(systemId: String, name: String, emailId: String) extends CborSerializable {

    def updateUser(name: String, emailId: String): SystemState = {
      copy(name = name, emailId = emailId)
    }
  }

  final case class AddSystem(systemId: String, name: String, email: String) extends Command

  final case class SystemAdded(systemId: String, name: String, email: String) extends Event

  object SystemState {
    val empty = SystemState(systemId = "", name = "", emailId = "")
  }
}
