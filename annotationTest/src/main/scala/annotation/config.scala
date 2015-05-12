package com.chrisneveu

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly

@compileTimeOnly("Enable macro paradise to expand macro annotations.")
class config extends StaticAnnotation {
   def macroTransform(annottees : Any*) = macro config.impl
}

object config {
   def impl(c : Context)(annottees : c.Expr[Any]*) : c.Expr[Any] = {
      import c.universe._
      import Flag._

      def newName : TermName = c.fresh(newTermName(""))

      val output = annottees.head.tree match {
         case ModuleDef(mods, name, impl) ⇒
            val membs = impl.body.map {
               case tree @ DefDef(mods, name, tparams, vparamss, tpt, rhs) ⇒
                  val typedRhs = c.typecheck(rhs)
                  if (typedRhs.tpe <:< typeOf[Option[_]]) {
                     val Modifiers(flags, pw, ann) = mods
                     val nme = newName
                     (DefDef(Modifiers(PRIVATE | flags, pw, ann), nme, tparams, vparamss, tpt, rhs), List(name -> nme))
                  } else
                     (DefDef(mods, name, tparams, vparamss, tpt, rhs), Nil)
               case tree @ ValDef(mods, name, tpt, rhs) ⇒
                  val typedRhs = c.typecheck(rhs)
                  if (typedRhs.tpe <:< typeOf[Option[_]]) {
                     val Modifiers(flags, pw, ann) = mods
                     val nme = newName
                     (ValDef(Modifiers(PRIVATE | flags, pw, ann), nme, tpt, rhs), List(name -> nme))
                  } else
                     (ValDef(mods, name, tpt, rhs), Nil)
               case t ⇒ (t, Nil)
            }
            val names : List[(TermName, TermName)] = membs.map(_._2).flatten
            val newMembers : List[Tree] = membs.map(_._1)

            val tupName = newName

            val (_, accessors) = names.foldLeft((1, List.empty[Tree])) {
               case ((num, ts), (name, _)) ⇒
                  val acc = newTermName("_" + num)
                  (num + 1, q"""val $name = $tupName.$acc""" :: ts)
            }
            val newBody = names match {
               case (name, newName) :: Nil ⇒
                  newMembers :+ q"""val $name = $newName"""
               case names ⇒
                  val tupled = q"""private val $tupName = (..${names.map(_._2)})"""
                  newMembers ++ (tupled :: accessors)
            }
            ModuleDef(
               mods,
               name,
               Template(impl.parents, impl.self, newBody))
         case _ ⇒ c.abort(NoPosition, "Configuration must be an object.")
      }
      println(output)
      c.Expr[Any](Block(List(output), Literal(Constant(()))))
   }
}
