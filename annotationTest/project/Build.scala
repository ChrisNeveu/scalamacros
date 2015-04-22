import sbt._
import sbt.Keys._
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform._
import ScalariformKeys._

object Build extends Build {

   lazy val root: Project = Project(
      "root",
      file("."),
      settings = commonSettings ++ Seq(
         version := "0.1-SNAPSHOT",
			resolvers += Resolver.sonatypeRepo("releases"),
         libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _),
			libraryDependencies += compilerPlugin(
				"org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
      )
   )

   def commonSettings = Defaults.defaultSettings ++ scalariformSettings ++
      Seq(
         organization := "tbox",
         version      := "0.0.1-SNAPSHOT",
         scalaVersion := "2.11.6",
         scalacOptions ++= Seq(
            "-unchecked",
            "-deprecation",
            "-feature",
            "-language:higherKinds",
            "-language:postfixOps"
         ),
         ScalariformKeys.preferences := ScalariformKeys.preferences.value
            .setPreference(IndentSpaces, 3)
            .setPreference(SpaceBeforeColon, true)
            .setPreference(PreserveDanglingCloseParenthesis, true)
            .setPreference(RewriteArrowSymbols, true)
            .setPreference(DoubleIndentClassDeclaration, true)
            .setPreference(AlignParameters, true)
            .setPreference(AlignSingleLineCaseStatements, true)
       )
}
