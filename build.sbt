version := "1.0.0"
name := "swagger-scala-akka-http-server"
organization := "io.swagger"
scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.2.6",
  "com.typesafe.akka" %% "akka-stream" % "2.6.18",
  "io.getquill" %% "quill-ndbc-postgres" % "3.12.0",
  "org.postgresql"  %  "postgresql"     % "42.3.1",
  "com.typesafe" % "config" % "1.4.1"
)
