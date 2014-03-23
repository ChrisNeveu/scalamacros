import sbt._
import Keys._

object BuildSettings {

	val scalaV = "2.11.0-RC3"
	
	val buildSettings = Defaults.defaultSettings ++ Seq (
		organization	:= "com.chrisneveu",
		version			 := "0.0.1-SNAPSHOT",
		scalaVersion	:= scalaV,
		scalacOptions += "-Ymacro-debug-lite",
		mainClass in (Compile,run) := Some("prog.LetExample")
	)
}

object ScalaMacroDebugBuild extends Build {
	import BuildSettings._

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
				"org.scala-lang" % "scala-reflect" % scalaV,
				"org.scala-lang" % "scala-xml" % "2.11.0-M4",
				"org.yaml" % "snakeyaml" % "1.13"))
	)

	lazy val examples: Project = Project(
		"examples",
		file("examples"),
		settings = buildSettings
	) dependsOn(macros)
}
