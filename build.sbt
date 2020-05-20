name := "school-management"

version := "0.1"

scalaVersion := "2.13.2"
def akkaVersion = "2.6.5"
//resolvers += Seq("com.lightbend.akka" %% "akka-split-brain-resolver" % "1.1.14")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3"

)