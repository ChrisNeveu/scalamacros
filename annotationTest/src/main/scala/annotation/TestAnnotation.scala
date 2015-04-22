package com.chrisneveu

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly

@compileTimeOnly("enable macro paradise to expand macro annotations")
class toMap extends StaticAnnotation {
   def macroTransform(annottees : Any*) = macro toMapMacro.impl
}

object toMapMacro {
   def impl(c : Context)(annottees : c.Expr[Any]*) : c.Expr[Any] = {
      import c.universe._
      import Flag._
      val input = annottees.head.tree
      val expandee = input match {
         case tree @ ClassDef(mods, name, tparams, impl) ⇒
            val members : List[TermName] = impl.body.collect {
               case ValDef(mods, name, tpt, rhs) if !mods.hasFlag(PRIVATE) ⇒ name
               case DefDef(mods, name, tparams, vparamss, tpt, rhs) if !mods.hasFlag(PRIVATE) &&
                  name.decoded != "<init>" ⇒ name
            }
            val pairs = members.map(name ⇒
               q"""(${name.decoded}, $name)""")
            val toMap = q"""def toMap = scala.collection.immutable.Map(..$pairs)"""
            ClassDef(mods, name, tparams, Template(impl.parents, impl.self, impl.body :+ toMap))
      }
      val outputs : List[Tree] = annottees match {
         case _ :: tail ⇒ expandee :: tail.map(_.tree)
         case _         ⇒ expandee :: Nil
      }
      c.Expr[Any](Block(outputs, Literal(Constant(()))))
   }
}
