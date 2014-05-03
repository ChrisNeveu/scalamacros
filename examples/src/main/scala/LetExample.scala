package prog.example

import schrine.template.html._

object LetExample extends App {

	def repeat3(s: String) = s + s + s
	
	override def main(args: Array[String]) = {
		val foobar = "cats!"
		val list = List("apples", "oranges", "bananas")
		val bool = false
		val optional: Option[String] = None
		val template: String = loadTemplate("file")
		println(template)
	}
}
