addSbtPlugin( "com.eed3si9n" % "sbt-buildinfo" % "0.9.0" )

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.2.2")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")
//addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.5")

addSbtPlugin("com.timushev.sbt"                  % "sbt-updates"            % "0.5.3")

addSbtPlugin("org.scoverage"                     % "sbt-scoverage"          % "1.7.3")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.30"

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")
