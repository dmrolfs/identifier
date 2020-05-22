import Dependencies._

lazy val scala212               = "2.12.10"
lazy val scala213               = "2.13.1"
lazy val supportedScalaVersions = List(scala212, scala213)

organization := "com.github.dmrolfs"
name := "identifier"
scalaVersion := scala213

lazy val publishSettings = Seq(
  licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  homepage := Some(url("https://github.com/dmrolfs/identifier")),
  scmInfo := Some(
    ScmInfo(url("https://github.com/dmrolfs/identifier"), "scm:git@github.com:dmrolfs/identifier.git")),
  developers := List(
    Developer(
      "dmrolfs",
      "Damon Rolfs",
      "drolfs@gmail.com",
      url("https://github.com/dmrolfs")
    )
  )
)

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
    "-Ywarn-unused",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
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
  .settings(publishSettings)

scalafmtOnCompile := true

// scalacOptions := scalacBuildOptions

resolvers += "omen-bintray" at "https://dl.bintray.com/omen/maven"

testOptions in Test += Tests.Argument( "-oDF" )

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false, includeDependency = false)

assemblyJarName in assembly := s"${organizationName.value}-${name.value}-${version.value}.jar"

// parallelExecution in MultiJvm := false