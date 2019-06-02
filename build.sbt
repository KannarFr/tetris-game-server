lazy val akkaVersion = "2.5.22"
lazy val akkaHttpVersion = "10.1.8"
lazy val circeVersion = "0.11.1"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.clevercloud",
      scalaVersion := "2.12.8"
    )),
    name := "tetris-game-server",
    scalaSource in Compile := baseDirectory.value / "src" / "main",
    scalaSource in Test := baseDirectory.value / "src" / "test",
    resolvers ++= Seq(
      Resolver.bintrayRepo("hseeberger", "maven")
    ),
    libraryDependencies ++= Seq(
      jdbc,
      guice,
      ws,
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,

      "org.postgresql" % "postgresql" % "42.2.5",

      "org.playframework.anorm" %% "anorm" % "2.6.2",

      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    )
  )
  .enablePlugins(PlayScala)

