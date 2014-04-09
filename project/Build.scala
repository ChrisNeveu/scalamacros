import sbt._
import Keys._

object BuildSettings {

	val scalaV = "2.10.4"
	val paradiseVersion = "2.0.0-M3"
	
	val buildSettings = Defaults.defaultSettings ++ Seq (
		organization	:= "com.chrisneveu",
		version			 := "0.0.1-SNAPSHOT",
		scalaVersion	:= scalaV,
		scalacOptions += "-Ymacro-debug-lite",
		resolvers += Resolver.sonatypeRepo("snapshots"),
		resolvers += Resolver.sonatypeRepo("releases"),
		addCompilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full),
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
				"org.yaml" % "snakeyaml" % "1.13") ++
				(if (scalaV.startsWith("2.10"))
					List("org.scalamacros" % "quasiquotes" % paradiseVersion cross CrossVersion.full)
				else Nil))
	)

	lazy val examples: Project = Project(
		"examples",
		file("examples"),
		settings = buildSettings
	) dependsOn(macros)
}
