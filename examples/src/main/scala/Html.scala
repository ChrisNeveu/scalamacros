package prog

import scala.xml.Utility.escape
import scala.collection.generic.SeqFactory
/*
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
*/
package object simplehtml {
	
	import annotation.implicitNotFound
	
	@implicitNotFound("Value of type ${T} could not be converted to Html.")
	trait toHtml[-T] {
		def renderHtml(html: T): String
	}
	
	trait HtmlSerializable[-T] {
		def renderHtml: String
	}
	
	implicit def createHtml[T:toHtml](a: T) =
		new HtmlSerializable[T] {
			def renderHtml = implicitly[toHtml[T]].renderHtml(a)
		}
	
	type Html = HtmlSerializable[_]
	
	implicit object String_Html extends toHtml[String] {
		def renderHtml(html: String) = escape(html)
	}
	
	implicit object Int_Html extends toHtml[Int] {
		def renderHtml(i: Int) = i.toString
	}
	
	implicit object Float_Html extends toHtml[Float] {
		def renderHtml(f: Float) = f.toString
	}
	
	implicit object Double_Html extends toHtml[Double] {
		def renderHtml(d: Double) = d.toString
	}
	
	implicit object Iter_Html extends toHtml[Iterable[Html]] {
		def renderHtml(htmls: Iterable[Html]) = htmls.map(_.renderHtml).mkString
	}
}
