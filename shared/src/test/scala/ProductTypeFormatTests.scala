package spray.json

import org.scalatest._

class ProductTypeFormatTests
    extends FlatSpec
    with FormatTests
    with DerivedJsonProtocol {

  case class A()
  case class B(x: Int, b: String, mp: Map[String, Int])
  case class C(b: B)
  case object D
  case class E(d: D.type)
  case class FF(x: Int)
  case class G(f: FF)

  implicit val aFormat: RootJsonFormat[A] = jsonFormat[A]
  implicit val bFormat: RootJsonFormat[B] = jsonFormat[B]
  implicit val cFormat: RootJsonFormat[C] = jsonFormat[C]
  implicit val dFormat: RootJsonFormat[D.type] = jsonFormat[D.type]
  implicit val eFormat: RootJsonFormat[E] = jsonFormat[E]

  "No-parameter product" should behave like checkRoundtrip(A(), "{}")

  "Simple parameter product" should behave like checkRoundtrip(
    B(42, "Hello World", Map("a" -> 1, "b" -> -1024)),
    """{ "x": 42, "b": "Hello World", "mp": { "a": 1, "b": -1024 } }"""
  )

  "Nested parameter product" should behave like checkRoundtrip(
    C(B(42, "Hello World", Map("a" -> 1, "b" -> -1024))),
    """{"b" :{ "x": 42, "b": "Hello World", "mp": { "a": 1, "b": -1024 } } }"""
  )

  "Case object" should behave like checkRoundtrip(
    D,
    "{}"
  )

  "Case object as parameter" should behave like checkRoundtrip(
    E(D),
    """{"d":{}}"""
  )

  // custom format for F, that inverts the value of parameter x
  implicit val fFormat: RootJsonFormat[FF] = new RootJsonFormat[FF] {
    override def write(f: FF): JsValue = JsObject("y" -> f.x.toJson)
    override def read(js: JsValue): FF =
      FF(js.asJsObject.fields("y").convertTo[Int])
  }

  "Overriding with a custom format" should behave like checkRoundtrip(
    FF(2),
    """{"y":2}"""
  )

  implicit val gFormat: RootJsonFormat[G] = jsonFormat[G]

  "Derving a format with a custom child format" should behave like checkRoundtrip(
    G(FF(2)),
    """{"f": {"y":2}}"""
  )

  case class H(x: Boolean)
  case class I(h: H)

  implicit val hFormat = jsonFormat[H]
  implicit val iFormat: RootJsonFormat[I] = jsonFormat[I]

  "Deriving a format that has no implicit child formats available" should behave like checkRoundtrip(
    I(H(true)),
    """{"h": {"x":true}}"""
  )

  case class Typed[T](t: T)

  implicit def typed[T: JsonFormat] = jsonFormat[Typed[T]]

  "Class with a type parameter" should behave like checkRoundtrip(
    Typed[Int](42),
    """{"t":42}"""
  )

  "Class with nested types" should behave like checkRoundtrip(
    Typed[Typed[String]](Typed("hello world")),
    """{"t":{"t":"hello world"}}"""
  )

  case class Phantom[T](x: Int)

  // no json format required for T
  implicit def phantom[T]: RootJsonFormat[Phantom[T]] = jsonFormat[Phantom[T]]

  "Phantom types without a format" should behave like checkRoundtrip(
    Phantom[Int => String](42), // the given type parameter should not have a format
    """{"x":42}"""
  )

  implicit val vc = jsonFormat[ProductTypeFormatTests.Wrapper]
  "Value classes" should behave like checkRoundtrip(
    ProductTypeFormatTests.Wrapper(42),
    """42"""
  )

  implicit def vcg[T: JsonFormat] = jsonFormat[ProductTypeFormatTests.WrapperGeneric[T]]
  "Value classes with generic parameters" should behave like checkRoundtrip(
    ProductTypeFormatTests.WrapperGeneric[FF](FF(42)),
    """{"y": 42}"""
  )

  implicit def vcp[T] = jsonFormat[ProductTypeFormatTests.WrapperPhantom[T]]
  "value classes with unused generic parameters" should behave like checkRoundtrip(
    ProductTypeFormatTests.WrapperPhantom[Int => String](42),
    """42"""
  )

}

object ProductTypeFormatTests {
  case class Wrapper(x: Int) extends AnyVal
  case class WrapperGeneric[A](x: A) extends AnyVal
  case class WrapperPhantom[A](x: Int) extends AnyVal
}
