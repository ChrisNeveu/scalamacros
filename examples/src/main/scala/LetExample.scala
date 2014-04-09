package prog

import prog.mac.config.LoadedConf

object LetExample extends App {
	
	override def main(args: Array[String]) = {
		val foobar = "cats!"
		val template: String = TemplateLoader.load("file")
		println(template)
	}
}
