package com.chrisneveu

@toMap
case class Foo(a : String, b : Int)

@enum
class Bar {
   Red
   Blue
   Black
}
object Bar {
   def stringify(b : Bar) = b match {
      case Red   ⇒ "red"
      case Blue  ⇒ "blue"
      case Black ⇒ "black"
   }
}
