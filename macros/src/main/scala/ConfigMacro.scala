package prog.mac.config

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.reflect.runtime.{universe => ru}
import org.yaml.snakeyaml.Yaml

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
