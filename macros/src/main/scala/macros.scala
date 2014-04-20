package prog

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.collection.immutable.Stack
import scala.util.parsing.combinator.RegexParsers

class let extends StaticAnnotation {
	def macroTransform(annottees: Any*) = macro ???
}

object FileMacro {
	def printFile(file: String): Unit = macro printFileImpl
	
	def printFileImpl(c: Context)(file: c.Expr[String]): c.Expr[Unit] = {
		import c.universe._
		val p = file.tree match {
			case Literal(Constant(p: String)) => p
			case _ => c.abort(c.enclosingPosition, "printFile only takes a literal value.")
		}
		val str = scala.io.Source.fromFile(p).mkString
		val strLiteral = c.Expr(Literal(Constant(str)))
		reify { println(strLiteral.splice) }
	}
}

/**
 * Eventually this should be a typed class "class TemplateLoader[T]"
 * where T is the type that the interpolated statements must conform to.
 *
 * trait fromTemplate[T] {
 * 	fromTemplate(str: String): T
 * }
 */

object TemplateLoader {
	
	def load(file: String): String = macro loadImpl
	
	def loadImpl(c: Context)(file: c.Expr[String]): c.Expr[String] = {
		import c.universe._
		val p = file.tree match {
			case Literal(Constant(p: String)) => p
			case _ => c.abort(c.enclosingPosition, "template only takes a literal value.")
		}
		val str = scala.io.Source.fromFile(p).mkString
		val parser = new TemplateParser[c.type](c)
		val foo = parser.parse(str)
		c.Expr(foo)
	}
}

class TemplateParser[C <: Context](val c: C) extends RegexParsers {
	import c.universe._

	override def skipWhitespace = false

	def parse(str: String): c.Tree = parseAll(program, str).get

	def joinNodes(nodes: List[c.Tree]) =
		nodes.reduceLeft((acc: c.Tree, n: c.Tree) =>
			Apply(Select(acc, newTermName("$plus")), List(n)))

	def program: Parser[c.Tree] = rep(node) ^^ joinNodes _
	
	def node: Parser[c.Tree] =
		text |
		interp |
		ifExpr |
		rawExpr |
		matchExpr |
		applyExpr |
		letExpr |
		optExpr |
		forExpr

	def text: Parser[c.Tree] =
		"""(?:(?!\{\{|\{#)(.|\n))+""".r ^^ { strLiteral => 
			println("TEXT " + strLiteral + "\n\n")
			Literal(Constant(strLiteral))
		}

	def rawText: Parser[c.Tree] =
		"""((.|\n)+)(?=\{#endraw\})""".r ^^
			(strLiteral => Literal(Constant(strLiteral)))
	
	def interp: Parser[c.Tree] =
		"""\{\{(.*?)\}\}""".r ^^ { expr =>
			println("INTERP " + expr + "\n\n")
			c.parse(expr)
		}

	def ifExpr: Parser[c.Tree] =
		(("{#if " ~> """[^}]+""".r <~ "}") ~
		 rep(node) ~
		 opt("{#else}" ~> rep(node))) <~
		"{#endif}" ^^ {
			case expr ~ nodes ~ elseN => {
				println("COND " + expr + "\n\n")
				println("IFNODES " + nodes + "\n\n")
				println("ELSENODES " + elseN + "\n\n")
				If(c.parse(expr),
					joinNodes(nodes),
					elseN.map(joinNodes _).getOrElse(Literal(Constant(""))))
			}
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
/*

	def comment: Parser[Any] = """{--(.*?)--}""".r

	Apply(Select(Ident(newTermName("foo")), newTermName("map")), List(Function(List(ValDef(Modifiers(PARAM), newTermName("x"), TypeTree(), EmptyTree)), Literal(Constant(4)))))
	*/
}

trait Schrine {

	case class ScalaExpr(self: String)
	
	//case class IfExpr(self: String)
}

object Html extends RegexParsers {

	val Id = """([a-zA-z]+)""".r

	val Attr = Id ~ """="([^""]*)"""".r

	def openTag = "<" ~ Id ~ rep(Attr) ~ ">"

	def closeTag = "</" ~ Id ~ ">"

	def node: Parser[Any] = text | element
	def text: Parser[Any] = """([^<>&])""".r
	def element: Parser[Any] = openTag ~ rep(node) ~ closeTag
}

trait SchrineParser extends RegexParsers {

	def node = ???
	
	val ScalaId = """([a-zA-z0-9\-_]+)""".r
	
	def interp: Parser[Any] = """{{(.*?)}}""".r
	def ifExpr: Parser[Any] = """{#if (.*?)}""".r ~ rep(node) ~ "{#endif}"

	def matchExpr: Parser[Any] = """{#match (.*?)}""".r ~ rep(caseExpr) ~ "{#endcase}"
	def caseExpr: Parser[Any] = """{#case (.*?)}""".r ~ rep(node) ~ "{#endcase}"

	def optExpr: Parser[Any] =
		"{#opt" ~ ScalaId ~ "as" ~ ScalaId ~ "}" ~
		rep(node) ~
		opt("{#none}" ~ rep(node)) ~
		"{#endopt}"

	def comment: Parser[Any] = """{--(.*?)--}""".r
}
