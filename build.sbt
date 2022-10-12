import sbt._

lazy val ScribeVersion = "3.10.3"
lazy val FabricVersion = "1.6.1"
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

ThisBuild / version := "0.1.0"

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

lazy val scribeJsonExt = module("json-ext").settings(
  libraryDependencies ++= Seq(
    "com.outr" %% "scribe" % ScribeVersion
  )
)

lazy val scribeJsonExtEvent = module("json-ext-event").dependsOn(scribeJsonExt)

lazy val scribeJsonExtCirce = module("json-ext-circe")
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % CirceVersion
    )
  )
  .dependsOn(scribeJsonExt)

lazy val scribeJsonExtFabric = module("json-ext-fabric")
  .settings(
    libraryDependencies ++= Seq(
      "com.outr" %% "fabric-core" % FabricVersion,
      "com.outr" %% "fabric-io" % FabricVersion
    )
  )
  .dependsOn(scribeJsonExt)

lazy val scribeJsonExtTesting =
  module("json-ext-test")
    .settings(
      libraryDependencies ++= Seq(
        "com.outr" %% "scribe" % ScribeVersion % Test,
        "io.circe" %% "circe-core" % CirceVersion % Test,
        "io.circe" %% "circe-generic" % CirceVersion % Test,
        "io.circe" %% "circe-parser" % CirceVersion % Test,
        "com.outr" %% "fabric-core" % FabricVersion % Test,
        "com.outr" %% "fabric-io" % FabricVersion % Test,
        "org.scalatest" %% "scalatest" % "3.2.14" % Test
      )
    )
    .enablePlugins(NoPublishPlugin)
    .dependsOn(scribeJsonExt, scribeJsonExtCirce, scribeJsonExtFabric, scribeJsonExtEvent)

lazy val scribeExt = project
  .in(file("."))
  .aggregate(
    scribeJsonExt,
    scribeJsonExtCirce,
    scribeJsonExtFabric,
    scribeJsonExtEvent,
    scribeJsonExtTesting
  )
