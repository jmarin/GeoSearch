import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform._
import spray.revolver.RevolverPlugin._

object BuildSettings {

  val buildOrganization = "cfpb"
  val buildVersion      = "1.0.0"
  val buildScalaVersion = "2.11.5"

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


object PointInPolyBuilds extends Build {
  
  import Dependencies._
  import BuildSettings._

  lazy val pointinpoly = Project(
    "point-in-poly",
    file("."),
    settings = buildSettings
  )

}
