package com.knoldus

import java.time.Instant

import scala.concurrent.duration._

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.RetentionCriteria
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior

object SchoolManagement{
  final case class State(subjects: Map[String, String], admissionDate: Option[Instant]) extends CborSerializable {

    def hasSubjects(subjectId: String): Boolean =
      subjects.contains(subjectId)

    def isAdmit: Boolean =
      admissionDate.isDefined

    def isEmpty: Boolean =
      subjects.isEmpty

    def updateSubjects(subjectId: String, name: String): State = {
      copy(subjects = subjects + (subjectId -> name))
    }

    def admission(now: Instant): State =
      copy(admissionDate = Some(now))

    def toSummary: Summary =
      Summary(subjects, isAdmit)
  }

  object State {
    val empty = State(subjects = Map.empty, admissionDate = None)
  }

  sealed trait Command extends CborSerializable

  sealed trait Confirmation extends CborSerializable

  final case class AddSubject(subjectId: String, name: String, replyTo: ActorRef[Confirmation]) extends Command

  final case class Admission(replyTo: ActorRef[Confirmation]) extends Command

  final case class Get(replyTo: ActorRef[Summary]) extends Command

  final case class Summary(subjects: Map[String, String], admissionDate: Boolean) extends CborSerializable

  final case class Accepted(summary: Summary) extends Confirmation

  final case class Rejected(reason: String) extends Confirmation

  sealed trait Event extends CborSerializable {
    def admissionId: String
  }

  final case class SubjectAdded(admissionId: String, subjectId: String, name: String) extends Event

  final case class Admitted(admissionId: String, eventTime: Instant) extends Event

  def apply(admissionId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, State](
      PersistenceId("SchoolAdmission", admissionId),
      State.empty,
      (state, command) =>
        if (state.isAdmit) admissionDetails(admissionId, state, command)
        else getAdmission(admissionId, state, command),
      (state, event) => handleEvent(state, event))
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
  }

  private def admissionDetails(admissionId: String, state: State, command: Command): Effect[Event, State] =
    command match {
      case Get(replyTo) =>
        replyTo ! state.toSummary
        Effect.none
      case cmd: AddSubject =>
        cmd.replyTo ! Rejected("Can't add an subject after the admission")
        Effect.none
      case cmd: Admission =>
        cmd.replyTo ! Rejected("Admission had been done")
        Effect.none
    }

  private def getAdmission(admissionId: String, state: State, command: Command): Effect[Event, State] =
    command match {
      case AddSubject(subjectId, name, replyTo) =>
        print("\n\nInside the AddSubjects Command\n\n\n")
        if (state.hasSubjects(subjectId)) {
          replyTo ! Rejected(s"Student already applied for '$subjectId'")
          Effect.none
        } else {
          print("\n\nInside else case\n\n")
          Effect
            .persist(SubjectAdded(admissionId, subjectId, name))
            .thenRun(updatedSubjects => replyTo ! Accepted(updatedSubjects.toSummary))
        }

      case Admission(replyTo) =>
        print("\n\nInside the Admission case\n\n")
        if (state.isEmpty) {
          replyTo ! Rejected("Cannot admit student without details")
          Effect.none
        } else {
          Effect
            .persist(Admitted(admissionId, Instant.now()))
            .thenRun(updatedCart => replyTo ! Accepted(updatedCart.toSummary))
        }

      case Get(replyTo) =>
        print("\n\nInside the Get case\n\n")
        replyTo ! state.toSummary
        Effect.none
    }

  private def handleEvent(state: State, event: Event) = {
    event match {
      case SubjectAdded(_, subjectId, name) => state.updateSubjects(subjectId, name)
      case Admitted(_, eventTime) => state.admission(eventTime)
    }
  }
}