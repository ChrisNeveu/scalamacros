package prog.html

import scala.xml.Utility.escape

abstract class Html {
	def toString: String
	//def ++(other: Html): Html
}

case class HtmlTag(
	val name: String,
	val attrs: List[HtmlAttr]) extends Html {
	
	override def toString = '<' + name + "></" + name + '>'
}

case class HtmlText(
	private[HtmlText] val text: String) extends Html {
	
	override def toString = escape(text)
}

case class HtmlAttr(
	val name: String,
	private[HtmlAttr] val text: String) extends Html {
	
	override def toString =
		if (name.toLowerCase == "src")
			escape(new Url(text).toString)
		else
			escape(text)
	
}

trait HtmlSerializable[T] {
	def toHtml(a: T): Html
}

object HtmlSerializable {

	implicit object HtmlSerializableString extends HtmlSerializable[String] {
		def toHtml(a: String) = HtmlText(a)
	}

	implicit object HtmlSerializableInt extends HtmlSerializable[Int] {
		def toHtml(a: Int) = HtmlText(a.toString)
	}
}

case class Url(private val text: String) {

	private def percentEncode(t: String) = t

	override def toString = percentEncode(text)
}

object Html {
	def a = new HtmlTag("a", List.empty)
}
