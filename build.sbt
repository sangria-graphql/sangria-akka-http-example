name := "sangria-akka-http-example"
version := "0.1.0-SNAPSHOT"

description := "An example GraphQL server written with akka-http, circe and sangria."

scalaVersion := "2.13.3"
scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "2.0.1",
  "org.sangria-graphql" %% "sangria-slowlog" % "2.0.0-M1",
  "org.sangria-graphql" %% "sangria-circe" % "1.3.0",

  "com.typesafe.akka" %% "akka-http" % "10.1.8",
  "de.heikoseeberger" %% "akka-http-circe" % "1.28.0",

  "io.circe" %% "circe-core" % "0.12.3",
  "io.circe" %% "circe-parser" % "0.12.3",
  "io.circe" %% "circe-optics" % "0.12.0",

  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)

Revolver.settings
enablePlugins(JavaAppPackaging)
