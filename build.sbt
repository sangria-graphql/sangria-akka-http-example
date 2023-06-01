name := "sangria-akka-http-example"
version := "0.1.0-SNAPSHOT"

description := "An example GraphQL server written with akka-http, circe and sangria."

ThisBuild / crossScalaVersions := Seq("2.13.10")
ThisBuild / scalaVersion := crossScalaVersions.value.last
ThisBuild / githubWorkflowPublishTargetBranches := List()

scalacOptions ++= Seq("-deprecation", "-feature", "-Xsource:3")

val akkaVersion = "2.6.20"
val circeVersion = "0.14.5"
val circeOpticsVersion = "0.14.1"
val sangriaAkkaHttpVersion = "0.0.3"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "4.0.0",
  "org.sangria-graphql" %% "sangria-slowlog" % "2.0.5",
  "org.sangria-graphql" %% "sangria-circe" % "1.3.2",

  "org.sangria-graphql" %% "sangria-akka-http-core" % sangriaAkkaHttpVersion,
  "org.sangria-graphql" %% "sangria-akka-http-circe" % sangriaAkkaHttpVersion,

  "com.typesafe.akka" %% "akka-http" % "10.2.10",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-optics" % circeOpticsVersion,

  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)

Revolver.settings
enablePlugins(JavaAppPackaging)
