import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "hush",
    version := "0.1",
    scalaVersion := "2.9.0-1",
    platformName in Android := "android-8"
  )

  lazy val fullAndroidSettings =
    General.settings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    AndroidMarketPublish.settings ++ Seq (
      keyalias in Android := "change-me",
      libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"
    )
}

object AndroidBuild extends Build {
  import heroic.Plugin._

  lazy val main = Project (
    "hush",
    file("."),
    settings = General.fullAndroidSettings ++ Seq(
      libraryDependencies += "net.databinder" %% "dispatch-http" % "0.7.8"
    )
  )

  lazy val hushSvr = Project(
    "hush-server",
    file("server"),
    settings = Defaults.defaultSettings ++ Seq(
      libraryDependencies ++= Seq(
        "net.databinder" %% "unfiltered-netty-server" % "0.5.1",        
        "com.codahale" %% "jerkson" % "0.5.0",
        "net.sf.opencsv" % "opencsv" % "2.3",
        "com.mongodb.casbah" %% "casbah" % "2.1.5-1"
      ),
      resolvers += "coda" at "http://repo.codahale.com"
    ) ++ heroicSettings
  )

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = General.settings ++ AndroidTest.androidSettings
  ) dependsOn main
}
