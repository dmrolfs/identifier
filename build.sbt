import Dependencies._

lazy val scala212               = "2.12.13"
lazy val scala213               = "2.13.5"
lazy val supportedScalaVersions = List(scala212, scala213)

ThisBuild / organization := "io.github.dmrolfs"
ThisBuild / name := "identifier"
ThisBuild / organizationName := "dmrolfs"
ThisBuild / organizationHomepage := Some(url("https://io.github.dmrolfs"))

ThisBuild / scmInfo := Some(ScmInfo(
  url("https://github.com/dmrolfs/identifier"), "scm:git@github.com:dmrolfs/identifier.git"
))

ThisBuild / developers := List(
  Developer(
    id = "dmrolfs",
    name = "Damon Rolfs",
    email = "drolfs@gmail.com",
    url = url("https://github.com/dmrolfs")
  )
)

ThisBuild / description := "Small, focused toolkit for defining typed and tagged entity identifiers."
ThisBuild / licenses := List("MIT" -> url("https://opensource.org/licenses/MIT"))
ThisBuild / homepage := Some(url("https://github.com/dmrolfs/identifier"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

ThisBuild / scalaVersion := scala213

ThisBuild / versionScheme := Some("early-semver")

lazy val scalacOptionsOnly212 = Seq("-Ypartial-unification", "-Xfuture", "-Yno-adapted-args")
scalacOptions ++=
  Seq(
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-language:experimental.macros",
    "-unchecked",
    "-Ywarn-dead-code",
    "-Ywarn-unused:explicits",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Ywarn-macros:after",
    "-deprecation"
  ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) => scalacOptionsOnly212
    case _             => Seq()
  })

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++=
  logging.all ++
  circe.all ++
  Seq(
    cats.core,
    snowflake,
    newtype,
    scalaUuid,
    codec,
  ) ++
  commonTestDependencies



// val kindProjectorVersion       = "0.11.0"
// addCompilerPlugin(("org.typelevel" % "kind-projector" % kindProjectorVersion).cross(CrossVersion.full))

// val betterMonadicForVersion    = "0.3.1"
// addCompilerPlugin("com.olegpy" %% "better-monadic-for" % betterMonadicForVersion)

lazy val root = (project in file("."))
  // .enablePlugins(MultiJvmPlugin)
  // .configs(MultiJvm)
  // .settings(multiJvmSettings: _*)
  .settings(parallelExecution in Test := false)
  .settings(crossScalaVersions := supportedScalaVersions)

scalafmtOnCompile := true

// scalacOptions := scalacBuildOptions

testOptions in Test += Tests.Argument( "-oDF" )

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false, includeDependency = false)

assemblyJarName in assembly := s"${organizationName.value}-${name.value}-${version.value}.jar"

// parallelExecution in MultiJvm := false
