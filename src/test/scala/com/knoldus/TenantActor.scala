package com.knoldus

import java.util.UUID

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.typed.PersistenceId
import org.scalatest.wordspec.AnyWordSpecLike

class TenantActor extends ScalaTestWithActorTestKit(s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
    """) with AnyWordSpecLike{

  "The Tenant Actor " should {
    "add tenant" in {
      val tenant = testKit.spawn(TenantActor(PersistenceId("TenantData","Tenant-ID")))
      tenant ! TenantActor.AddTenant("tenantID","tenantName","emailID")
    }
  }

}
