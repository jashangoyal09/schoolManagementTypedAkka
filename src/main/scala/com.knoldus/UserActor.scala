package com.knoldus

import java.time.Instant

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

object UserActor {
  val systemActor = ActorSystem(SystemActor("ID123"),"system-actor")

  final case class UserState(userId:String,name: String, emailId: String) extends CborSerializable{

    def updateUser(name:String,emailId:String): UserState ={
      copy(name = name,emailId = emailId)
    }
  }

  sealed trait Command extends CborSerializable{
    def userId:String
  }

  final case class AddUser(userId:String,name:String,email:String) extends Command

  val commandHandler: (UserState, Command) => Effect[Event, UserState] = { (state, command) =>
    command match {
      case cmd:AddUser => {
        print(s"Request to Add new user ${cmd.name}")
        Effect.persist(UserAdded(cmd.userId,cmd.name,cmd.email))
      }
//        .thenReply(state=> cmd)
    }
  }

  sealed trait Event extends CborSerializable {
    def userId:String
  }

  final case class UserAdded(userId:String, name:String, email:String) extends Event

  val eventHandler: (UserState, Event) => UserState = { (state, event) =>
    event match {
      case UserAdded(userId,name,emailId) => {
        print(s"\nUser ${name} has been persisted to the state\n")
        systemActor ! SystemActor.AddSystem(userId,name,emailId)
        state.copy(userId = userId,name = name, emailId = emailId)
      }
    }
  }

  val TypeKey: EntityTypeKey[Command] =
    EntityTypeKey[Command]("UserActor")

  def apply(entityId: String, persistenceId: PersistenceId): Behavior[Command] = {
    Behaviors.setup { context =>
      context.log.info("Starting HelloWorld {}", entityId)
      EventSourcedBehavior(persistenceId, emptyState = UserState("","",""), commandHandler, eventHandler)
    }
  }

}
