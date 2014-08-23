package schrine.template

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.collection.immutable.Stack
import scala.util.parsing.combinator.RegexParsers
import scala.xml.Utility.escape

package object html {
		
	def loadTemplate(file: String): String = macro loadImpl
		
	def loadImpl(c: Context)(file: c.Expr[String]): c.Expr[String] = {
		import c.universe._
		val p = file.tree match {
			case Literal(Constant(p: String)) => p
			case _ => c.abort(c.enclosingPosition, "Template only takes a literal value.")
		}
		val str = scala.io.Source.fromFile(p).mkString
		val parser = new SchrineParser[c.type](c)
		val foo = parser.parse(str)
		c.Expr(foo)
	}
	
	import annotation.implicitNotFound
	
	@implicitNotFound("Value of type ${T} could not be converted to Html.")
	trait toHtml[-T] {
		def renderHtml(html: T): String
	}

	def renderHtml[T:toHtml](html: T) =
		implicitly[toHtml[T]].renderHtml(html)
	
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
	
	class SchrineParser[C <: Context](val c: C) extends RegexParsers { 
		
		import c.universe._
	
		override def skipWhitespace = false
	
		def parse(str: String): c.Tree = parseAll(program, str).get 
	
		def joinNodes(nodes: List[c.Tree]) =
			nodes.reduceLeft((acc: c.Tree, n: c.Tree) =>
				Apply(Select(acc, newTermName("$plus")), List(n)))
	
		def program: Parser[c.Tree] = rep(node) ^^ joinNodes _
	
		def text: Parser[c.Tree] =
			"""(?:(?!\{\{|\{#|\{--)(.|\n))+""".r ^^
				(strLiteral => Literal(Constant(strLiteral)))
		
		def node: Parser[c.Tree] =
			text |
			interp |
			ifExpr |
			rawExpr |
			matchExpr |
			applyExpr |
			letExpr |
			optExpr |
			forExpr |
			comment
	
		def rawText: Parser[c.Tree] =
			"""((.|\n)+)(?=\{#endraw\})""".r ^^
				(strLiteral => Literal(Constant(strLiteral)))
		
		def interp: Parser[c.Tree] =
			"""\{\{(.*?)\}\}""".r ^^
				(expr => Apply(Ident(newTermName("renderHtml")), List(c.parse(expr))))
	
		def ifExpr: Parser[c.Tree] =
			(("{#if " ~> """[^}]+""".r <~ "}") ~
			 rep(node) ~
			 opt("{#else}" ~> rep(node))) <~
			"{#endif}" ^^ {
				case expr ~ nodes ~ elseN =>
					If(c.parse(expr),
						joinNodes(nodes),
						elseN.map(joinNodes _).getOrElse(Literal(Constant(""))))
			}
	
		def rawExpr: Parser[c.Tree] =
			"{#raw}" ~> rawText <~ "{#endraw}"
		
		def matchExpr: Parser[c.Tree] =
			("{#match " ~> """[^}]+""".r <~ "}") ~
			rep(caseExpr) <~ """(\s)*\{#endmatch\}""".r ^^ {
				case expr ~ cases => Match(c.parse(expr), cases)
			}
		
		def caseExpr: Parser[CaseDef] =
			("""(\s)*\{#case """.r ~> """([^}]+)""".r <~ """\}""".r) ~
			rep(node) <~ "{#endcase}" ^^ {
				case expr ~ nodes => c.parse("Unit match { case " + expr + " => Unit }") match {
					case Match(_, (CaseDef(pat, guard, body) :: _)) =>
						CaseDef(pat, guard, joinNodes(nodes))
				}
			}
	
		def applyExpr: Parser[c.Tree] =
			("{#apply " ~> """[^}]+""".r <~ "}") ~
			 rep(node) <~
			"{#endapply}" ^^ {
				case func ~ nodes => Apply(Ident(newTermName(func)), List(joinNodes(nodes)))
			}
	
		def letExpr: Parser[c.Tree] =
			("{#let " ~> """[a-zA-z][0-9a-zA-z_-]*""".r <~ """ ?= ?""".r) ~
			("""[^}]+""".r <~ "}") ~
			 rep(node) <~
			"{#endlet}" ^^ {
				case name ~ value ~ nodes =>
					Block(
						List(ValDef(Modifiers(), newTermName(name), TypeTree(), c.parse(value))),
						joinNodes(nodes))
			}
	
		def optExpr: Parser[c.Tree] =
			("{#opt " ~> """[a-zA-z][0-9a-zA-z_-]*""".r <~ " as ") ~
			("""[a-zA-z][0-9a-zA-z_-]*""".r <~ "}") ~
			rep(node) ~
			opt("{#none}" ~> rep(node)) <~
			"{#endopt}" ^^ {
				case option ~ some ~ nodes ~ optNodes =>
					Apply(Select(
						Apply(Select(Ident(newTermName(option)), newTermName("map")),
							List(Function(
								List(ValDef(
									Modifiers(Flag.PARAM),
									newTermName(some),
									TypeTree(),
									EmptyTree)),
								joinNodes(nodes)))),
						newTermName("getOrElse")),
						List(optNodes.map(joinNodes _).getOrElse(Literal(Constant("")))))
			}
	
		def forExpr: Parser[c.Tree] =
			("{#for " ~> """[a-zA-z][0-9a-zA-z_-]*""".r <~ " in ") ~
			("""[a-zA-z][0-9a-zA-z_-]*""".r <~ "}") ~
			rep(node) <~
			"{#endfor}" ^^ {
				case item ~ list ~ nodes =>
					Select(
						Apply(Select(Ident(newTermName(list)), newTermName("map")),
							List(Function(
								List(ValDef(
									Modifiers(Flag.PARAM),
									newTermName(item),
									TypeTree(),
									EmptyTree)),
								joinNodes(nodes)))),
						newTermName("mkString"))
			}
	
		def comment: Parser[c.Tree] =
			"""\{--(.*?)--\}""".r ^^ (_ => Literal(Constant("")))
	}
}
