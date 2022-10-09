import sbt._

lazy val ScribeVersion = "3.10.3"
lazy val CirceVersion = "0.14.3"

val publishSettings = List(
  organization := "io.github.greyplane",
  homepage := Some(url("https://github.com/GreyPlane/scribe-extension")),
  licenses := List(sbt.librarymanagement.License.MIT),
  developers := List(
    Developer(
      "GreyPlane",
      "Liu Ji",
      "greyplane@gmail.com",
      url("https://github.com/GreyPlane")
    )
  ),
  tlSonatypeUseLegacyHost := false
)

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

inThisBuild(
  publishSettings
)
def module(path: String): Project = {
  Project(path.capitalize, file(s"modules/$path")).settings(
    moduleName := s"scribe-$path",
    name := s"scribe-$path"
  )
}

lazy val scribeExtendableJson = module("extendable-json").settings(
  libraryDependencies ++= Seq(
    "com.outr" %% "scribe" % ScribeVersion
  )
)

lazy val scribeExtendableJsonEvent = module("extendable-json-event").dependsOn(scribeExtendableJson)

lazy val scribeExtendableJsonCirce = module("extendable-json-circe")
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % CirceVersion
    )
  )
  .dependsOn(scribeExtendableJson)

lazy val scribeExtendableJsonTesting =
  module("extendable-json-test")
    .settings(
      libraryDependencies ++= Seq(
        "com.outr" %% "scribe" % ScribeVersion % Test,
        "io.circe" %% "circe-core" % CirceVersion % Test,
        "io.circe" %% "circe-generic" % CirceVersion % Test,
        "io.circe" %% "circe-parser" % CirceVersion % Test,
        "org.scalatest" %% "scalatest" % "3.2.14" % Test
      )
    )
    .enablePlugins(NoPublishPlugin)
    .dependsOn(scribeExtendableJson, scribeExtendableJsonCirce, scribeExtendableJsonEvent)

lazy val scribeExtension = project
  .in(file("."))
  .aggregate(
    scribeExtendableJson,
    scribeExtendableJsonCirce,
    scribeExtendableJsonEvent,
    scribeExtendableJsonTesting
  )
