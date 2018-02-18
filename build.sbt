name := "sangria-akka-http-example"
version := "0.1.0-SNAPSHOT"

description := "An example GraphQL server written with akka-http and sangria."

scalaVersion := "2.12.4"
scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.3.3",
  "org.sangria-graphql" %% "sangria-spray-json" % "1.0.1",
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.10",

  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

Revolver.settings
enablePlugins(JavaAppPackaging)
