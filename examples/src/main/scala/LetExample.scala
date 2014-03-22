package prog

import prog.config.Bar._

object LetExample extends App {
	
	override def main(args: Array[String]) = {
		val template: String = TemplateMacro.template("file")
		println(template)
	}
}
