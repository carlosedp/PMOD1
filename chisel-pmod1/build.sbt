// See README.md for license details.

lazy val pmod1 = (project in file("."))
  .settings(
    organization := "com.carlosedp",
    name := "Chisel-PMOD1",
    version := "0.0.1",
    scalaVersion := "2.13.6",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    maxErrors := 3
  )

// Library default versions
val defaultVersions = Map(
  "chisel3"          -> "3.5-SNAPSHOT",
  "chiseltest"       -> "0.5-SNAPSHOT",
  "scalatest"        -> "3.2.9",
  "organize-imports" -> "0.5.0"
)
// Import libraries
libraryDependencies ++= Seq("chisel3", "chiseltest").map { dep: String =>
  "edu.berkeley.cs" %% dep % sys.props
    .getOrElse(dep + "Version", defaultVersions(dep))
}
libraryDependencies += "org.scalatest"                     %% "scalatest"        % defaultVersions("scalatest")
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % defaultVersions("organize-imports")

// Aliases
addCommandAlias("com", "all compile test:compile")
addCommandAlias("rel", "reload")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll;all compile:scalafix test:scalafix")
addCommandAlias("deps", "dependencyUpdates")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % defaultVersions("chisel3") cross CrossVersion.full)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-language:reflectiveCalls",
  "-feature",
  "-Xcheckinit",
  "-Xfatal-warnings",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-Ywarn-unused"
)
