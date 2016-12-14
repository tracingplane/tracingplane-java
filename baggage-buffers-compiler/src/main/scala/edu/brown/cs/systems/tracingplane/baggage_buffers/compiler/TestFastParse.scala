package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler

import fastparse.all._
import org.scalatest.Assertions._

object TestFastParse extends App {

  object BB {

    sealed trait fieldtype

    sealed trait primitive extends fieldtype
    sealed trait numeric extends primitive;
    sealed trait float extends numeric
    sealed trait signed extends numeric
    sealed trait unsigned extends numeric

    object bool extends unsigned
    object int32 extends unsigned
    object int64 extends unsigned
    object sint32 extends signed
    object sint64 extends signed
    object fixed32 extends unsigned
    object fixed64 extends unsigned
    object sfixed32 extends signed
    object sfixed64 extends signed
    object float extends float
    object double extends float
    object string extends primitive
    object bytes extends primitive

    sealed case class custom(name: String) extends fieldtype

    sealed case class Field(fieldType: fieldtype, fieldName: String, fieldIndex: Int)
    sealed case class Bag(name: fieldtype, fields: Seq[Field])
  }

  val ws: P[Unit] = P(CharIn(" \t").rep)
  val nl: P[Unit] = P(ws.? ~ "\n" ~ ws.?)

  val letter = P(lowercase | uppercase)
  val lowercase = P(CharIn('a' to 'z'))
  val uppercase = P(CharIn('A' to 'Z'))
  val digit = P(CharIn('0' to '9'))

  val primitive: P[BB.primitive] = P(
    "bool".!.map(_ => BB.bool) |
    "int32".!.map(_ => BB.int32) |
    "int64".!.map(_ => BB.int64) |
    "sint32".!.map(_ => BB.sint32) |
    "sint64".!.map(_ => BB.sint64) |
    "fixed32".!.map(_ => BB.fixed32) |
    "fixed64".!.map(_ => BB.fixed64) |
    "sfixed32".!.map(_ => BB.sfixed32) |
    "sfixed64".!.map(_ => BB.sfixed64) |
    "float".!.map(_ => BB.float) |
    "double".!.map(_ => BB.double) |
    "string".!.map(_ => BB.string) |
    "bytes".!.map(_ => BB.bytes)
    )
  
  val custom: P[BB.custom] = P( letter.rep.!.map(name => BB.custom(name)) )
  
  val fieldtype: P[BB.fieldtype] = P ( primitive | custom ) 
  val fieldname: P[String] = P((letter | "_") ~ (letter | digit | "_").rep).!
  val fieldindex: P[Int] = P(("0" | (CharIn('1' to '9') ~ CharIn('0' to '9').rep)).!.map(_.toInt))
  
  def fieldindex2(i: Int) = fieldindex.filter(_ > i)

  val fieldDeclaration: P[BB.Field] = P(fieldtype ~ ws ~ fieldname ~ ws.? ~ "=" ~ ws.? ~ fieldindex ~ ws.? ~ ";").map { case (a, b, c) => BB.Field(a, b, c) }
  def fieldDeclaration2(i: Int): P[BB.Field] = P(fieldtype ~ ws ~ fieldname ~ ws.? ~ "=" ~ ws.? ~ fieldindex.filter(_ > i) ~ ws.? ~ ";").map { case (a, b, c) => BB.Field(a, b, c) }

  val fieldDeclarations = P({
                            var previousIndex = -1;
                            for {
                              field <- fieldDeclaration.filter(_.fieldIndex > previousIndex)
                              _ <- nl
                              previousIndex = field.fieldIndex
                            } yield field
  })
  
  val bagDeclaration = P( "bag" ~ ws ~ custom ~ ws.? ~ "{" ~ nl ~ fieldDeclarations.rep ~ "}" )

  val field1 = "bool test = 1;"
  val field2 = "blahblah jon = 2;"
  val field3 = "float bad = 0;"
  
  val fields = field1 + "\n" + field2 + "\n"
  
//  fieldDeclaration.parse(field1) match {
//    case Parsed.Success(a, b) => println("a is " + a.getClass())
//  }
//  
//  fieldDeclarations.rep.parse(fields) match {
//    case Parsed.Success(a, b) => println("a is " + a.getClass() + " and... " + a)
//  }
  
  val declaration = """bag MyBag {
        bool test = 1;
        blahblah jon = 2;
        int32 bad = 0;
    }
  """
  
  val result = bagDeclaration.parse(declaration)
  println(result)
  
  result match {
    case Parsed.Success(a, b) => println(a._2)
    case Parsed.Failure(a, b, c) => println(a, b, c)
  }

}