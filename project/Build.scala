import play.PlayImport.PlayKeys
import play.Play.autoImport._
import play.twirl.sbt.Import._
import sbt._
import Keys._
import PlayKeys._


object ApplicationBuild extends Build {

  val appName = "myproject"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "se.radley" %% "play-plugins-salat" % "1.5.0"
  )

  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies,
    routesImport += "se.radley.plugin.salat.Binders._",
    TwirlKeys.templateImports += "org.bson.types.ObjectId",
    resolvers += Resolver.sonatypeRepo("snapshots")
  )

}