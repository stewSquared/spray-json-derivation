// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}
import com.typesafe.tools.mima.core._

lazy val sprayJsonDerivation =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Full)
    .in(file("."))
    .settings(
      name := "spray-json-derivation",
      version in ThisBuild := {
        import sys.process._
        ("git describe --always --dirty=-SNAPSHOT --match v[0-9].*" !!).tail.trim
      },
      scalaVersion := crossScalaVersions.value.head,
      scalacOptions ++= Seq(
        "-feature",
        "-deprecation",
        "-Xlint",
        "-Xfatal-warnings"
      ),
      libraryDependencies ++= Seq(
        "io.crashbox" %%% "spray-json" % "1.3.4-1",
        "com.propensive" %%% "magnolia" % "0.10.0"
      )
    )
    .platformsSettings(JVMPlatform, JSPlatform)(
      libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.5" % "test"
    )
    .jvmSettings(
      mimaPreviousArtifacts := Set(
        "xyz.driver" %% "spray-json-derivation" % "0.5.0"),
      crossScalaVersions := "2.12.6" :: "2.11.12" :: Nil
    )
    .jsSettings(
      crossScalaVersions := "2.12.6" :: "2.11.12" :: Nil
    )
    .nativeSettings(
      crossScalaVersions := "2.11.12" :: Nil,
      sources in (Compile, doc) := Seq.empty,
      unmanagedSourceDirectories in Test := Seq.empty
    )

lazy val sprayJsonDerivationJVM = sprayJsonDerivation.jvm
lazy val sprayJsonDerivationJS = sprayJsonDerivation.js
lazy val sprayJsonDerivationNative = sprayJsonDerivation.native

lazy val root = (project in file("."))
  .aggregate(sprayJsonDerivationJVM,
             sprayJsonDerivationJS,
             sprayJsonDerivationNative)
  .settings(
    publish := {},
    publishLocal := {}
  )
