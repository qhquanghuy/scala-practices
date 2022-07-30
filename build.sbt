
ThisBuild / scalaVersion := "3.1.3"
ThisBuild / version      := "0.1.0"


ThisBuild / run / fork := true
lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.16.0" % Test,
      "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
    ),
  )


Test / testOptions += Tests.Argument(TestFrameworks.ScalaCheck, "-maxSize", "1", "-workers", "1", "-verbosity", "1")