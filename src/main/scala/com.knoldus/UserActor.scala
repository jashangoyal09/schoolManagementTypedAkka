package com.knoldus

import java.time.Instant

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.HashCodeNoEnvelopeMessageExtractor
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.knoldus.SystemActor.SystemRequest

object UserActor {
//  val systemActor = ActorSystem(SystemActor("ID123"),"system-actor")

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
//        systemActor ! SystemActor.AddSystem(userId,name,emailId)
        state.copy(userId = userId,name = name, emailId = emailId)
      }
    }
  }

  val TypeKey: EntityTypeKey[Command] =
    EntityTypeKey[Command]("UserActor")

  def apply(system: ActorRef[SystemRequest]): Behavior[Command] = {
    print("\n\ninside the apply of user actor\n\n")
    Behaviors.setup { context =>
      val sharding = ClusterSharding(context.system)
      val messageExtractor: HashCodeNoEnvelopeMessageExtractor[TenantActor.Command] =
        new HashCodeNoEnvelopeMessageExtractor[TenantActor.Command](numberOfShards = 30) {
          override def entityId(message: TenantActor.Command): String = message.tenantId
        }
      val TenantRequestHandlerTypeKey = EntityTypeKey[TenantActor.Command]("TenatRequestHandler")
      print("\n\nAfter the Tenant RequestHandler\n\n")

      val shardRegion: ActorRef[TenantActor.Command] =
        sharding.init(
          Entity(TenantRequestHandlerTypeKey) { context =>
            TenantActor(system,context.entityId,PersistenceId(context.entityTypeKey.name, context.entityId))
          }.withMessageExtractor(messageExtractor)
            .withStopMessage(TenantActor.GracefulStop))
      val counterOne: EntityRef[TenantActor.Command] = sharding.entityRefFor(TenantRequestHandlerTypeKey, "tenant-1")
      print("\n\nAfter the shardRegion\n\n")
      counterOne.!(TenantActor.AddTenant("tenant-id","name","email"))

      Behaviors.receiveMessage {
        case userRequest: AddUser =>
          print("\n\nInside the case add user\n\n\n")
          shardRegion ! TenantActor.AddTenant("tenant-id",userRequest.name,userRequest.email)
          Behaviors.same
        case _ => Behaviors.unhandled
      }
    }
  }

}
