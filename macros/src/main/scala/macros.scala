package prog

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.tools.nsc.interpreter.IMain
import com.twitter.util.Eval
import scala.annotation.StaticAnnotation
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.interactive.Response
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.nsc.io.VirtualFile

/*
case class PresentationCompiler() {
	// need to be adjusted in order to work with
	// sbt. See this question.
	val settings = new Settings()
	settings.bootclasspath.value +=scala.tools.util.PathResolver.Environment.javaBootClassPath + java.io.File.pathSeparator + "lib/scala-library.jar"

	// can be replaced by a custom instance
	// of AbstractReporter to gain control.
	val reporter = new ConsoleReporter(settings)

	val compiler = new Global(settings, reporter)

	def compile(code: String) {
		val source = new BatchSourceFile(new VirtualFile("unnamed"), code.toSeq)    
		val response = new Response[Unit]()    
		compiler.askReload(List(source), response)    
		response.get(10000) match {// wait 10 seconds for a response
			case Some(Left(_)) => // success
			case Some(Right(e)) => // handle exception e
			case None => // timed out
		}
	}
	def parseTree(code: String) = {
		val sourceCode = "object Foo {\n" + code + "\n}"
		val source = new BatchSourceFile(new VirtualFile("unnamed"), sourceCode.toSeq)    
		compiler.parseTree(source)
	}
}*/

/*
import scala.tools.nsc._
import io._

object MkTree {
	
	val settings = new Settings()
	settings.bootclasspath.value +=scala.tools.util.PathResolver.Environment.javaBootClassPath + java.io.File.pathSeparator + "lib/scala-library.jar"
	
	val reporter = new ConsoleReporter(settings)
	
	object Compiler extends Global(settings, reporter) {
		new Run() // have to initialize the compiler

		def parse(code: String) = {
			val bfs = new util.BatchSourceFile("", code)
			val unit =
				new CompilationUnit(new util.ScriptSourceFile(bfs, code.toArray, 0))
			val scanner = new syntaxAnalyzer.UnitParser(unit)
			scanner.templateStatSeq(false)._2
		}
		def print(t: Tree) = println(t)
	}

	def main(args: Array[String]): Unit = {
		val ts = Compiler.parse(args mkString " ")
		ts map Compiler.nodeToString foreach println
	}
}
*/
class let extends StaticAnnotation {
	def macroTransform(annottees: Any*) = macro ???
}

object FileMacro {
	def printFile(file: String): Unit = macro printFileImpl
	
	def printFileImpl(c: Context)(file: c.Expr[String]): c.Expr[Unit] = {
		import c.universe._
		val p = file.tree match {
			case Literal(Constant(p: String)) => p
			case _ => c.abort(c.enclosingPosition, "printFile only takes a literal value.")
		}
		val str = scala.io.Source.fromFile(p).mkString
		val strLiteral = c.Expr(Literal(Constant(str)))
		reify { println(strLiteral.splice) }
	}
}

object TemplateMacro {

	import scala.tools.nsc._
	
	//val settings = new Settings
	
	def template(file: String): Unit = macro templateImpl
	
	def templateImpl(c: Context)(file: c.Expr[String]): c.Expr[Unit] = {
		import c.universe._
		val p = file.tree match {
			case Literal(Constant(p: String)) => p
			case _ => c.abort(c.enclosingPosition, "template only takes a literal value.")
		}   
		val foo = c.parse("5")
		println("really? " + foo)
		//val i = new Interpreter(settings)
		//val bar = i.parse("2 + 3")
		val str = scala.io.Source.fromFile(p).mkString
		val strLiteral = c.Expr(Literal(Constant(str)))
		c.Expr(c.parse("5"))
	}
}
