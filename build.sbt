name := """swipetup"""

name := """swipetup"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.novus" %% "salat" % "1.9.9",
  "net.ceedubs" %% "ficus" % "1.1.1"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
    jdbc,
    cache,
    ws
)
