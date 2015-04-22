package prog.example

import schrine.template.html._
import prog.EvalMacro._

import scala.concurrent.duration.DurationInt

object LetExample extends App {

	def repeat3(s: String) = s + s + s
	
	override def main(args: Array[String]) = {
		val foobar = "cats!?"
		val list = List("apples", "oranges", "bananas")
		val bool = false
		val optional: Option[String] = None
		val bar = ""
		val template: String = loadTemplate("templates/main.schrine")
		println(template)

		val test: Option[Int] = -|(Some(5))
		println("eval result: " + test + "\n")
	}
}
