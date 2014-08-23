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
		private val text: String) {

		override def toString =
			if (name.toLowerCase == "src")
				HtmlUrl(text).toString
			else
				escape(text)
	}

	case class HtmlSeq(val self: Seq[Html]) extends Html {
		override def toString = self.map(_.toString).mkString("\n")
	}

	case class HtmlUrl(private val text: String) {

		private def percentEncode(t: String) = t

		override def toString = percentEncode(text)
	}
	
	import annotation.implicitNotFound
	
	@implicitNotFound("Value of type ${T} could not be converted to Html.")
	trait toHtml[-T] {
		def toHtml(t: T): Html
	}
}

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
