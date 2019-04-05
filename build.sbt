lazy val akkaVersion = "2.5.22"
lazy val akkaHttpVersion = "10.1.8"
lazy val circeVersion = "0.11.1"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.clevercloud",
      scalaVersion := "2.12.8"
    )),
    resolvers ++= Seq(),
    name := "tetris-game-server",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,

      "io.circe" %% "circe-parser" % circeVersion,

      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",

      "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    )
  )

