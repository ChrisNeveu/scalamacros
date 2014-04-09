package prog

import scala.xml.Utility.escape
import scala.collection.generic.SeqFactory

package object html {

	sealed abstract class Html {
		def toString: String
	}

	case class HtmlTag(
		val name: String,
		val attrs: List[HtmlAttr],
		val children: HtmlSeq) extends Html {
		
		override def toString = '<' + name + "></" + name + '>'
	}

	case class HtmlText(
		private[HtmlText] val self: String) extends Html {
	
		override def toString = escape(self)
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

	case class HtmlSeq(val self: Seq[Html]) extends Html

	object conversions {
		def createHtmlSeq(seq: Seq[Html]) = new HtmlSeq(seq)
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
		//def a = new HtmlTag("a", List.empty, new HtmlSeq())
	}
}
