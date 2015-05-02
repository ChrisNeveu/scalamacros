package com.chrisneveu

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly

@compileTimeOnly("Enable macro paradise to expand macro annotations.")
class enum extends StaticAnnotation {
   def macroTransform(annottees : Any*) = macro enum.impl
}

object enum {
   def impl(c : Context)(annottees : c.Expr[Any]*) : c.Expr[Any] = {
      import c.universe._
      import Flag._
      val (input : Tree, companion : Option[Tree]) = annottees match {
         case clazz :: obj :: Nil ⇒ (clazz.tree, Some(obj.tree))
         case clazz :: Nil        ⇒ (clazz.tree, None)
         case _                   ⇒ c.abort(NoPosition, "Enum must be a class.")
      }
      val outputs = input match {
         case ClassDef(mods, enumName, tparams, impl) ⇒
            impl.body.foreach {
               case Ident(_) ⇒
               case DefDef(_, name, _, _, _, _) if name.decoded == "<init>" ⇒
               case t ⇒
                  println(showRaw(t))
                  c.abort(t.pos, "An enum may only contain identifiers.")
            }
            val init = impl.body.find {
               case DefDef(_, name, _, _, _, _) if name.decoded == "<init>" ⇒ true
            }.get
            val cases = impl.body.collect {
               case Ident(name) ⇒
                  val enumType = enumName.toTypeName
                  q"""case object ${name.toTermName} extends $enumType"""
            }
            val companionObj = companion match {
               case Some(ModuleDef(mods, objName, objImpl)) ⇒ ModuleDef(
                  mods,
                  objName,
                  Template(objImpl.parents, objImpl.self, cases ++ objImpl.body))
               case None ⇒ ModuleDef(
                  Modifiers(),
                  enumName.toTermName,
                  Template(impl.parents, impl.self, init :: cases))
            }
            List(
               q"sealed abstract class ${enumName.toTypeName} extends Product with Serializable",
               companionObj)
         case _ ⇒ c.abort(NoPosition, "Enum must be a class.")
      }
      println(outputs)
      c.Expr[Any](Block(outputs, Literal(Constant(()))))
   }
}
