name := "sangria-akka-http-example"
version := "0.1.0-SNAPSHOT"

description := "An example GraphQL server written with akka-http and sangria."

scalaVersion := "2.11.7"
scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "0.4.0",
  "com.typesafe.akka" %% "akka-http-experimental" % "1.0",

  // Use json4s in this example
  "org.json4s" %% "json4s-native" % "3.2.11",
  "de.heikoseeberger" %% "akka-http-json4s" % "1.0.0"

  // you can also use this one, but at the moment it depends
  // on outdated version of spray-json
  // "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "1.0"
)

Revolver.settings
enablePlugins(JavaAppPackaging)