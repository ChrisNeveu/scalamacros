import sbt._
import Keys._

object BuildSettings {

	val scalaV = "2.11.0"
	val paradiseVersion = "2.0.0-M3"

	def cached(cacheBaseDirectory: File, inStyle: FilesInfo.Style)(action: => Unit): Set[File] => Unit = {
		import Path._
		lazy val inCache = Difference.inputs(cacheBaseDirectory / "in-cache", inStyle)
		inputs => {
			inCache(inputs) { inReport =>
				println("Cached function " + !inReport.modified.isEmpty + "\n\n")
				if(!inReport.modified.isEmpty) action
			}
		}
	}
	val recompileWhenFileChanges = taskKey[Unit]("Recompiles the project when a file changes")
	
	val buildSettings = Defaults.defaultSettings ++ Seq (
		organization	:= "com.chrisneveu",
		version			 := "0.0.1-SNAPSHOT",
		scalaVersion	:= scalaV,
		scalacOptions += "-Ymacro-debug-lite",
		resolvers += Resolver.sonatypeRepo("snapshots"),
		resolvers += Resolver.sonatypeRepo("releases"),
		mainClass in (Compile,run) := Some("prog.example.LetExample"),
		watchSources <++= baseDirectory map { path => ((path / "templates") ** "*.schrine").get },
    	recompileWhenFileChanges := {
       	val base = baseDirectory.value
       	val mySpecialFile = baseDirectory.value / "templates" / "main.schrine"
			println("my file " + mySpecialFile + "\n\n")
       	val cache = cacheDirectory.value / "my_cache_dir"
       	val cachedFunction = cached(cache, FilesInfo.lastModified)(IO.delete((classDirectory in Compile).value))
       	cachedFunction(mySpecialFile.get.toSet)
       },
		compile <<= ((compile in Compile) dependsOn recompileWhenFileChanges)
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
				"org.scala-lang" % "scala-parser-combinators" % "2.11.0-M4",
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
