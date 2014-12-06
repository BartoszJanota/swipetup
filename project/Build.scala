import play.PlayImport.PlayKeys
import play.PlayImport.PlayKeys._
import play.twirl.sbt.Import._
import sbt.Keys._
import sbt._


object ApplicationBuild extends Build {

  val appName = "swipetup"
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