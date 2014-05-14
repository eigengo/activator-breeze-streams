name := "breeze-streams"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-experimental" % "0.2",
  "org.scalanlp" %% "breeze-natives" % "0.7",
  "org.scalanlp" %% "breeze" % "0.7",
  "org.scalanlp" %  "nak" % "1.2.1"
)

