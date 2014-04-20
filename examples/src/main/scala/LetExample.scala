package prog

import prog.mac.config.LoadedConf

object LetExample extends App {

	def repeat3(s: String) = s + s + s
	
	override def main(args: Array[String]) = {
		val foobar = "cats!"
		val list = List("apples", "oranges", "bananas")
		val bool = false
		val optional = None
		val template: String = TemplateLoader.load("file")
		println(template)
	}
}
