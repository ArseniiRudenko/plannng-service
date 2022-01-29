version := "1.0.0"
name := "swagger-scala-akka-http-server"
organization := "io.swagger"
scalaVersion := "2.12.15"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.2.7",
  "com.typesafe.akka" %% "akka-stream" % "2.6.18",
  "com.typesafe" %% "ssl-config-core" % "0.6.0",
  "org.scalikejdbc" %% "scalikejdbc"       % "4.0.0",
  "ch.qos.logback"  %  "logback-classic"   % "1.2.10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "org.postgresql"  %  "postgresql"     % "42.3.1",
  "com.typesafe" % "config" % "1.4.1",
  "org.scalactic" %% "scalactic" % "3.2.10"% "test",
  "org.scalatest" %% "scalatest" % "3.2.10" % "test"
)
