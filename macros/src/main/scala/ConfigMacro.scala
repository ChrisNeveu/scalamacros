package prog.mac.config

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.reflect.runtime.{universe => ru}
import org.yaml.snakeyaml.Yaml
import scala.annotation.StaticAnnotation

/*
object ConfigLoader {

	private val yaml = new Yaml
	
	def load(file: String): Config = macro loadImpl
	
	def loadImpl(c: Context)(file: c.Expr[String]): c.Expr[Config] = {
		import c.universe._
		val p = file.tree match {
			case Literal(Constant(p: String)) => p
			case _ => c.abort(c.enclosingPosition, "load only takes a literal value.")
		}
		val str = scala.io.Source.fromFile(p).mkString
		val config: Object = yaml.load(str)
		c.Expr(q"""case class Foo(hello: Int) extends Config; new Foo(25)""")
	}
}

abstract class Config
*/
class LoadedConf extends StaticAnnotation {
	def macroTransform(annottees: Any*) = macro identityMacro.impl
}

object identityMacro {
	def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
		import c.universe._
		val inputs = annottees.map(_.tree).toList
		val (annottee, expandees) = inputs match {
			case (param: ValDef) :: (rest @ (_ :: _)) => (param, rest)
			case (param: TypeDef) :: (rest @ (_ :: _)) => (param, rest)
			case _ => (EmptyTree, inputs)
		}
		println("\n\nAnnotation macro: ")
		println("Annotation macro: " + (annottee, expandees) + "\n\n")
		println("\n\n")
		val outputs = expandees
		c.Expr[Any](Block(outputs, Literal(Constant(()))))
	}
}
