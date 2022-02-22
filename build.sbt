version := "0.0.1-SNAPSHOT"
name := "scala-akka-http-server"
organization := "arudenko.work"
scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.2.7",
  "com.typesafe.akka" %% "akka-stream" % "2.6.18",
  "com.typesafe" %% "ssl-config-core" % "0.6.0",
  "org.scalikejdbc" %% "scalikejdbc"       % "4.0.0",
  "ch.qos.logback"  %  "logback-classic"   % "1.2.10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "org.postgresql"  %  "postgresql"     % "42.3.2",
  "com.typesafe" % "config" % "1.4.2",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.70",
  "net.debasishg" %% "redisclient" % "3.42",
  "io.suzaku" %% "boopickle" % "1.4.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.1",
  "org.scalactic" %% "scalactic" % "3.2.10"% "test",
  "org.scalatest" %% "scalatest" % "3.2.10" % "test"
)

enablePlugins(GatlingPlugin)
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.7.4" % "test"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % "3.7.4" % "test"
