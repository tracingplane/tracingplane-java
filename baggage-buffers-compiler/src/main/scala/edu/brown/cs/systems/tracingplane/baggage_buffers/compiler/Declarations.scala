package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler

import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast._
import fastparse.all._
import org.apache.commons.lang3.StringUtils

object Declarations {
  
  /** Eats zero or more whitespace characters (space, tab) */
  val eatws: P[Unit] = P( CharIn(" \t").rep )
  
  /** Eats zero or more whitespace characters (space, tab, newline) */
  val eatnl: P[Unit] = P( CharIn(" \t\n").rep )

  /** Eats at least one whitespace character (space, tab) */
  val ws: P[Unit] = P( CharIn(" \t").rep(1) )

  /** Eats at least one newline character, plus all surrounding whitespaces and comments */
  val nl: P[Unit] = P( eatws ~ "\n" ~ eatnl)
  
  /** Eats at least one whitespace character (space, tab, newline) */
  val nlws: P[Unit] = P( CharIn(" \t\n").rep(1) )

  /** Match any lower or uppercase letter */
  val letter = P(lowercase | uppercase)
  val lowercase = P(CharIn('a' to 'z'))
  val uppercase = P(CharIn('A' to 'Z'))
  val digit = P(CharIn('0' to '9'))
  val name = P( letter ~ (letter | digit | "_").rep )
  
  /** Matches built-in primitive types */
  val primitiveType: P[PrimitiveType] = P(
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

  /** Matches built-in parameterized types */
  val parameterizedType: P[BuiltInType] = P(
      (("set<" | "Set<") ~ eatws ~/ primitiveType ~ eatws ~ ">").map(BuiltInType.Set(_)) |
      (("map<" | "Map<") ~ eatws ~/ primitiveType ~ eatws ~ "," ~ eatws ~/ fieldtype ~ eatws ~ ">").map { case (k, v) => BuiltInType.Map(k, v) } |
      (("Counter" | "counter") ~ "<>".?).map(_ => BuiltInType.Counter))
      
      

  val fqUserDefinedType: P[UserDefinedType] = P( name.!.rep( min = 2, sep = "." ) ).map { 
    case components => UserDefinedType(components.dropRight(1).mkString("."), components.last) 
  }
  val nonFqUserDefinedType: P[UserDefinedType] = P ( name.! ).map(UserDefinedType("", _))

  val fieldtype: P[FieldType] = P( fqUserDefinedType | primitiveType | parameterizedType | nonFqUserDefinedType )
  val fieldindex: P[Int] = P(CharIn('0' to '9').rep).!.map(_.toInt)

  val fieldDeclaration: P[FieldDeclaration] = P(fieldtype ~ ws ~ name.! ~ eatws ~/ "=" ~ eatws ~/ fieldindex ~ eatws ~/ ";").map { case (a, b, c) => FieldDeclaration(a, b, c) }
  val bagDeclaration: P[BagDeclaration] = P( "bag" ~ ws ~/ name.! ~ eatnl ~/ "{" ~ eatnl ~/ fieldDeclaration.rep(min=1, sep=eatnl) ~ eatnl ~/ "}" ).map { case (a, b) => BagDeclaration(a, b) }

  val importDeclaration: P[ImportDeclaration] = P( "import" ~ ws ~/ "\"" ~ CharsWhile(_ != '"').! ~ "\"" ~ eatws ~/ ";" ).map{ case a => ImportDeclaration(a) }
  
  val packagename: P[Seq[String]] = P( name.!.rep( min = 1, sep = "." ) )
  val packageDeclaration: P[PackageDeclaration] = P( "package" ~ ws ~/ packagename ~ eatws ~/ ";").map{ case a => PackageDeclaration(a) }
  
  val bbDeclaration: P[BaggageBuffersDeclaration] = P( eatnl ~ (packageDeclaration ~ nl).? ~ (importDeclaration ~ nl).rep ~ bagDeclaration.rep(min=1, sep=nl) ~ eatnl ~ End ).map { case (a,b,c) => BaggageBuffersDeclaration(a,b,c) }

  
}