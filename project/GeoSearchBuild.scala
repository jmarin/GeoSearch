import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform._
import spray.revolver.RevolverPlugin._

object BuildSettings {

  val buildOrganization = "cfpb"
  val buildVersion      = "1.0.0"
  val buildScalaVersion = "2.11.6"

  val buildSettings = Defaults.coreDefaultSettings ++
    scalariformSettings ++
    Revolver.settings ++
    Seq(
      organization  := buildOrganization,
      version       := buildVersion,
      scalaVersion  := buildScalaVersion,
      scalacOptions := Seq("-deprecation","-unchecked","-feature")
    )

}


object GeoSearchBuild extends Build {
  
  import Dependencies._
  import BuildSettings._

  val buildDeps = Seq(akkaActor, akkaStreams, akkaHttpCore, akkaHttp, akkaHttpJson, akkaHttpTestkit, scaleGeoJson, scaleShapefile)

  val test = Seq(scalaTest, scalaCheck)

  val deps = buildDeps ++ test

  lazy val geosearch = Project(
    "point-in-poly",
    file("."),
    settings = buildSettings ++ Seq(
      resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      libraryDependencies ++= deps
    )
  )

}
