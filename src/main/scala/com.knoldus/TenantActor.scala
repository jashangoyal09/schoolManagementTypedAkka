package com.knoldus

import java.time.Instant

import scala.concurrent.duration._
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import com.knoldus.SystemActor.SystemRequest

object TenantActor {

  final case class TenantState(tenantId:String,name: String, emailId: String) extends CborSerializable{

    def updateUser(name:String,emailId:String): TenantState ={
      copy(name = name,emailId = emailId)
    }
  }
  object TenantState {
    val empty = TenantState(tenantId = "", name = "", emailId = "")
  }
  sealed trait Command /*extends CborSerializable*/{
    def tenantId: String
  }
  final case object GracefulStop extends Command {
    // this message is intended to be sent directly from the parent shard, hence the orderId is irrelevant
    override def tenantId = ""
  }
  final case class AddTenant(tenantId:String,name:String,email:String) extends Command

  val commandHandler: (TenantState, Command) => Effect[Event, TenantState] = { (state, command) =>
    command match {
      case cmd:AddTenant => {
        print(s"Request to Add new Tenant ${cmd.name}")
        Effect.persist(TenantAdded(cmd.tenantId,cmd.name,cmd.email))
      }
      //        .thenReply(state=> cmd)
    }
  }

  sealed trait Event extends CborSerializable {
    def tenantId:String
  }

  final case class TenantAdded(tenantId:String, name:String, email:String) extends Event

  val eventHandler: (TenantState, Event) => TenantState = { (state, event) =>
    event match {
      case TenantAdded(tenantId,name,emailId) => {
        print(s"\nTenant ${name} has been persisted to the state\n")
        state.copy(tenantId = tenantId,name = name, emailId = emailId)
      }
    }
  }

  def apply(persistenceId: PersistenceId): Behavior[Command] = {
    print("\n\ninside the tenant apply\n\n")
    EventSourcedBehavior[Command, Event, TenantState](
      persistenceId,
      TenantState.empty,
      (state, command) =>commandHandler(state,command),
      (state, event) => eventHandler(state, event))
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
  }
}
