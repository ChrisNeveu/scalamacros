package prog

case class A(ab: Int)

trait X {
	val b: String
}

object Test {
	def func(a: A with X): String = a.b
	def resolve(a: A): A with X =
		new A(a.ab) with X { val b = a.ab.toString }
}
