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

object TemplateLoader {
	
	def load(file: String): String = macro loadImpl
	
	def loadImpl(c: Context)(file: c.Expr[String]): c.Expr[String] = {
		import c.universe._
		val p = file.tree match {
			case Literal(Constant(p: String)) => p
			case _ => c.abort(c.enclosingPosition, "template only takes a literal value.")
		}
		val str = scala.io.Source.fromFile(p).mkString
		val foo = parseTemplate(str, c)
		c.Expr(foo)
	}

	def parseTemplate(str: String, c: Context) = {
		import c.universe._
		def oldParse(str: String, interp: Boolean, strAcc: String, treeAcc: c.Tree, expr: String): c.Tree = {
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
						oldParse(str.tail.tail, false, "", newAcc, "")
					} else {
						oldParse(str.tail, interp, strAcc, treeAcc, expr + cur)
					}
				} else {
					if (cur == '{' && next == '{') {
						oldParse(str.tail.tail, true, strAcc, treeAcc, "")
					} else {
						oldParse(str.tail, interp, strAcc + cur, treeAcc, "")
					}
				}
			} getOrElse {
				if (strAcc.isEmpty)
					treeAcc
				else
					Apply(Select(treeAcc, TermName("$plus")), List(Literal(Constant(strAcc))))
			}
		}
		def incAcc(tree: c.Tree, acc: c.Tree => c.Tree): c.Tree => c.Tree =
			((t: c.Tree) => acc(Apply(Select(tree, TermName("$plus")), List(t))))
		def init(str: String, acc: c.Tree => c.Tree, strAcc: String): c.Tree =
			str.headOption.getOrElse('EOF) match {
				case 'EOF => acc(Literal(Constant(strAcc)))
				case '{' => openBrace(str.tail, acc, strAcc)
				case c => init(str.tail, acc, strAcc + c)
			}
		def openBrace(str: String, acc: c.Tree => c.Tree, strAcc: String): c.Tree =
			str.headOption.getOrElse(throw new Error("Unexpected end of file.")) match {
				case '{' => interpolation(str.tail, incAcc(Literal(Constant(strAcc)), acc), "")
				case '#' => control(str.tail, incAcc(Literal(Constant(strAcc)), acc), "")
				case c => init(str.tail, acc, strAcc + '{' + c)
			}
		def interpolation(str: String, acc: c.Tree => c.Tree, exprAcc: String, braceCount: Int = 0): c.Tree =
			str.headOption.getOrElse(throw new Error("Unexpected end of file.")) match {
				case '}' if braceCount == 0 => interpCloseBrace(str.tail, acc, exprAcc)
				case '{' => interpolation(str.tail, acc, exprAcc + '{', braceCount + 1)
				case c => interpolation(str.tail, acc, exprAcc + c, braceCount)
			}
		def interpCloseBrace(str: String, acc: c.Tree => c.Tree, exprAcc: String): c.Tree =
			str.headOption.getOrElse(throw new Error("Unexpected end of file.")) match {
				case '}' => init(str.tail, incAcc(c.parse(exprAcc), acc), "")
				case c => throw new Error(s"Expected '}' but found '$c'.")
			}
		def control(str: String, acc: c.Tree => c.Tree, contAcc: String): c.Tree =
			str.headOption.getOrElse(throw new Error("Unexpected end of file.")) match {
				case ' ' if contAcc == "if" => ???
				case ' ' if contAcc == "opt" => ???
				case ' ' if contAcc == "for" => ???
				case ' ' if contAcc == "match" => ???
				case ' ' if contAcc == "let" => ???
				case ' ' if contAcc == "include" => ???
				case ' ' if contAcc == "apply" => ???
				case ' ' if contAcc == "raw" => ???
				case ' ' => throw new Error(s"Invalid command name '$contAcc'.")
				case c => control(str.tail, acc, contAcc + c)
			}
		init(str, ((a: c.Tree) => a), "")
	}
}
