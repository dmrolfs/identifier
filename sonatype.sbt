sonatypeProfileName := "io.github.dmrolfs"
publishMavenStyle := true
licenses := Seq("MIT" -> url("https://mit-license.org/"))

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("dmrolfs", "identifier", "drolfs@gmail.com"))

sonatypeCredentialHost := "s01.oss.sonatype.org"
