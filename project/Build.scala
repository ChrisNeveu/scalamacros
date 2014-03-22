import sbt._
import Keys._

object BuildSettings {

	val scalaV = "2.10.2"
	
	val buildSettings = Defaults.defaultSettings ++ Seq (
		organization	:= "com.chrisneveu",
		version			 := "0.0.1-SNAPSHOT",
		scalaVersion	:= scalaV,
		scalacOptions += "",
		mainClass in (Compile,run) := Some("prog.LetExample"),
		resolvers += Resolver.sonatypeRepo("releases")
	)
}

object ScalaMacroDebugBuild extends Build {
	import BuildSettings._

	addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.0-M3" cross CrossVersion.full)

	lazy val root: Project = Project(
		"root",
		file("."),
		settings = buildSettings
	) aggregate(macros, examples)

	lazy val macros: Project = Project(
		"macros",
		file("macros"),
		settings = buildSettings ++ Seq(
			libraryDependencies := Seq(
				("org.scala-lang" % "scala-compiler" % scalaV),
				"com.twitter" %% "util-eval" % "6.12.1"))
	)

	lazy val examples: Project = Project(
		"examples",
		file("examples"),
		settings = buildSettings
	) dependsOn(macros)
}
