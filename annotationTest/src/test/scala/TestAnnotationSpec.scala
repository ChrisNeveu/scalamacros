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

object foo {
   @enum
   class Eff {
      One
   }
}

@config object Config {
   val foo = 5
   val bar = Option(4)
   val baz = Option(3)
}
