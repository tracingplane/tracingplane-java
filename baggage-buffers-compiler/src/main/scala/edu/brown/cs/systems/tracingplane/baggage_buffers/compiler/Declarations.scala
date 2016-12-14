package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler

import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast._
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.FieldType.BuiltInType
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.FieldType.UserDefined.UserDefinedType
import fastparse.all._

object Declarations {

  /** Eats the following whitespace characters: space, tab, comment2 */
  val ws: P[Unit] = P( (CharIn(" \t") | comment2).rep)

  /** Eats the following whitespace characters: space, tab, newline, comment1, comment2 */
  val nlws: P[Unit] = P((CharIn(" \t\n") | comment1 | comment2).rep)

  /** Match any lower or uppercase letter */
  val letter = P(lowercase | uppercase)
  val lowercase = P(CharIn('a' to 'z'))
  val uppercase = P(CharIn('A' to 'Z'))
  val digit = P(CharIn('0' to '9'))
  
  /** One-line comment beginning with double slash */
  val comment1 = P( "//" ~ CharsWhile(_ != '\n') ~ "\n" )
  val comment2 = P( "/*" ~ (!"*/" ~ AnyChar).rep ~ "*/" )
  
  /** Matches built-in primitive types */
  val primitiveType: P[BuiltInType] = P(
    "bool".!.map(_ => BuiltInType.bool) |
      "int32".!.map(_ => BuiltInType.int32) |
      "int64".!.map(_ => BuiltInType.int64) |
      "sint32".!.map(_ => BuiltInType.sint32) |
      "sint64".!.map(_ => BuiltInType.sint64) |
      "fixed32".!.map(_ => BuiltInType.fixed32) |
      "fixed64".!.map(_ => BuiltInType.fixed64) |
      "sfixed32".!.map(_ => BuiltInType.sfixed32) |
      "sfixed64".!.map(_ => BuiltInType.sfixed64) |
      "float".!.map(_ => BuiltInType.float) |
      "double".!.map(_ => BuiltInType.double) |
      "string".!.map(_ => BuiltInType.string) |
      "bytes".!.map(_ => BuiltInType.bytes))

  val userDefinedType: P[UserDefinedType] = P(letter.rep(1).!.map(name => UserDefinedType(name)))

  /** Matches built-in parameterized types */
  val parameterizedType: P[BuiltInType] = P(
      ("set<" ~/ (primitiveType | userDefinedType) ~ ">").map(of => BuiltInType.Set(of)))

  val fieldtype: P[FieldType] = P(primitiveType | parameterizedType | userDefinedType)
  val fieldname: P[String] = P((letter | "_") ~ (letter | digit | "_").rep).!
  val fieldindex: P[Int] = P(CharIn('0' to '9').rep).!.map(_.toInt)

  val fieldDeclaration: P[FieldDeclaration] = P(fieldtype ~ ws ~ fieldname ~ ws.? ~ "=" ~ ws.? ~ fieldindex ~ ws.? ~ ";").map { case (a, b, c) => FieldDeclaration(a, b, c) }
  val bagDeclaration: P[BagDeclaration] = P( "bag" ~ ws ~/ letter.rep(1).! ~ ws.? ~ "{" ~ (nlws.? ~ fieldDeclaration).rep ~ nlws.? ~ "}" ).map { case (a, b) => BagDeclaration(a, b) }

  val importDeclaration: P[ImportDeclaration] = P( "import" ~ ws ~/ "\"" ~ CharsWhile(_ != '"').! ~ "\"" ~ ws.? ~ ";" ).map{ case a => ImportDeclaration(a) }
  
  val packagename: P[Seq[String]] = P( (letter ~ (letter | digit | "_").rep).!.rep( min = 1, sep = "." ) )
  val packageDeclaration: P[PackageDeclaration] = P( "package" ~ ws ~ "\"" ~ packagename ~ "\"" ~ ws.? ~ ";").map{ case a => PackageDeclaration(a) }
  
//  val bbDeclaration: P[BaggageBuffersDeclaration] = P 
  
}