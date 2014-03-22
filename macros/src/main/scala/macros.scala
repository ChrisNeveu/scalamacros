package prog

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

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

object TemplateMacro {
	
	def template(file: String): String = macro templateImpl
	
	def templateImpl(c: Context)(file: c.Expr[String]): c.Expr[String] = {
		import c.universe._
		val p = file.tree match {
			case Literal(Constant(p: String)) => p
			case _ => c.abort(c.enclosingPosition, "template only takes a literal value.")
		}
		val str = scala.io.Source.fromFile(p).mkString
		val foo = parseTemplate(str, c)
		println("```\n\n\n")
		println(showCode(foo))
		println("```\n\n\n")
		c.Expr(foo)
	}

	def parseTemplate(str: String, c: Context) = {
		import c.universe._
		def parse(str: String, interp: Boolean, strAcc: String, treeAcc: c.Tree, expr: String): c.Tree = {
			str.headOption.map { cur =>
				val next = try str.tail.head catch {
					case e: java.lang.UnsupportedOperationException => ""
					case e: java.util.NoSuchElementException => ""
				}
				if (interp) {
					if (cur == '}' && next == '}') {
						val rawExpr = c.parse(expr)
						val rawStr = Literal(Constant(strAcc))
						val accPlusStr = Apply(Select(treeAcc, TermName("$plus")), List(rawStr))
						val newAcc = Apply(Select(accPlusStr, TermName("$plus")), List(rawExpr))
						parse(str.tail.tail, false, "", newAcc, "")
					} else {
						parse(str.tail, interp, strAcc, treeAcc, expr + cur)
					}
				} else {
					if (cur == '{' && next == '{') {
						parse(str.tail.tail, true, strAcc, treeAcc, "")
					} else {
						parse(str.tail, interp, strAcc + cur, treeAcc, "")
					}
				}
			} getOrElse {
				if (strAcc.isEmpty)
					treeAcc
				else
					Apply(Select(treeAcc, TermName("$plus")), List(Literal(Constant(strAcc))))
			}
		}
		parse(str, false, "", Literal(Constant("template : ")), "")
	}
}
