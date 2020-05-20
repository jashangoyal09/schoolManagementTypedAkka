package com.knoldus

import java.time.Instant

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.HashCodeNoEnvelopeMessageExtractor
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
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
        print(s"\nRequest to Add new user ${cmd.name}\n")
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
      context.log.info("Starting user actor", entityId)
      val sharding = ClusterSharding(context.system)

      val messageExtractor: HashCodeNoEnvelopeMessageExtractor[UserActor.Command] =
        new HashCodeNoEnvelopeMessageExtractor[UserActor.Command](numberOfShards = 30) {
          override def entityId(message: UserActor.Command): String = message.userId
        }


      val shardRegion: ActorRef[UserActor.Command] =
        sharding.init(
          Entity(TypeKey) { context =>
            UserActor("",PersistenceId("",""))
          }.withMessageExtractor(messageExtractor))

      print("\n\n\nHitting Add User\n\n\n")
      shardRegion ! AddUser("user 123", "jashan", "jashan@gmail.com")



      EventSourcedBehavior(persistenceId, emptyState = UserState("","",""), commandHandler, eventHandler)
    }
  }

}
