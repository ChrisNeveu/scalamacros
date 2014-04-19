package prog

import prog.mac.config.LoadedConf

object LetExample extends App {

	def repeat3(s: String) = s + s + s
	
	override def main(args: Array[String]) = {
		val foobar = "cats!"
		val bool = false
		val template: String = TemplateLoader.load("file")
		println(template)
	}
}
