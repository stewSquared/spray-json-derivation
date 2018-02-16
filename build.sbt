name := "spray-json-derivation"

version in ThisBuild := {
  import sys.process._
  ("git describe --always --dirty=-SNAPSHOT --match v[0-9].*" !!).tail.trim
}

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "io.spray" %% "spray-json" % "1.3.4",
  "com.propensive" %% "magnolia" % "0.7.0",
  "org.scalatest" %% "scalatest" % "3.0.2" % "test"
)
