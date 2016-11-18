name := "sangria-akka-http-example"
version := "0.1.0-SNAPSHOT"

description := "An example GraphQL server written with akka-http and sangria."

scalaVersion := "2.11.8"
scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.0.0-RC3",
  "org.sangria-graphql" %% "sangria-spray-json" % "0.3.2",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.11",

  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

Revolver.settings
enablePlugins(JavaAppPackaging)