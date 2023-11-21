//ThisBuild / version := "0.1.0-SNAPSHOT"
//
//ThisBuild / scalaVersion := "2.13.12"
//
//val akkaVersion = "2.6.20"
//
//lazy val root = (project in file("."))
//  .settings(
//    name := "neptune-csv-spike",


Global / lintUnusedKeysOnLoad := false

val akkaVersion = "2.6.20"

val excludeApacheCommonConfiguration =
  ExclusionRule(organization = "org.apache.commons", name = "commons-configuration2")

lazy val root = (project in file("."))
  .settings(
    version := "2.0.1",
    name := "neptune-bulk-spike",
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-text" % "1.10.0",
      "software.amazon.awssdk" % "neptunedata" % "2.21.26",
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion
    ),
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    excludeDependencies += "com.lightbend.cinnamon",
    excludeDependencies += "com.lightbend.akka" %% "akka-diagnostics",
    Test / testOptions += Tests.Argument("-oD"),
  )

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation",
  "-Xlint",
  "-nowarn",
  "-Xasync"
)
javaOptions += "-Dakka.http.parsing.max-header-value-length=16k"
javaOptions += "-Dakka.http.parsing.max-uri-length=8k"

