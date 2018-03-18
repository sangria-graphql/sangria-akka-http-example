name := "sangria-akka-http-example"
version := "0.1.0-SNAPSHOT"

description := "An example GraphQL server written with akka-http, circe and sangria."

scalaVersion := "2.12.4"
scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.4.0",
  "org.sangria-graphql" %% "sangria-circe" % "1.2.1",

  "com.typesafe.akka" %% "akka-http" % "10.1.0",
  "de.heikoseeberger" %% "akka-http-circe" % "1.20.0",

  "io.circe" %%	"circe-core" % "0.9.2",
  "io.circe" %% "circe-parser" % "0.9.2",
  "io.circe" %% "circe-optics" % "0.9.2",

  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

Revolver.settings
enablePlugins(JavaAppPackaging)
