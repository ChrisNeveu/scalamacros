package prog

import scala.tools.nsc._

trait Foo

object LetExample extends App {
	
	override def main(args: Array[String]) = {
		TemplateMacro.template("file")
	}
}
