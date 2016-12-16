package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler

import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast._
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Declarations._
import fastparse.all._
import org.apache.commons.lang3.StringUtils

object Parser {
  
  /** One-line comment beginning with double slash */
  val comment1 = P( "//" ~ CharsWhile(_ != '\n') ~ "\n" ).map{ case _ => "\n" }
  val comment2 = P( "/*" ~ (!"*/" ~ AnyChar).rep ~ ("*/" | End) ).map{ case _ => "" }
  val trailingComment1 = P( "//" ~ AnyChar.rep ~ End ).map{ case _ => "" }
  val removeComments = P ( (comment1 | trailingComment1 | comment2 | AnyChar.!).rep ).map(_.mkString(""))
 
  @throws[ParseError]
  def parse[T](parser: P[T], text: String): T = {
    parser.parse(text) match {
      case failure: Parsed.Failure => throw ParseError(failure)
      case Parsed.Success(res, i) => {
        if (i == text.length()) {
          return res
        } else {
          val unexpected = StringUtils.abbreviate(text.substring(i), 20);
          throw new Exception("Unexpected text at index " + i + " \"" + unexpected + "\"");
        }
      }
    }
  }
  
  def removeComments(text: String): String = {
    return parse(removeComments, text);
  }
  
  @throws[ParseError]
  def parseBaggageBuffersFile(text: String): BaggageBuffersDeclaration = {
    val bbDecl = parse(bbDeclaration, removeComments(text))
    if (bbDecl.isEmpty) {
      throw new Exception("No BaggageBuffers declaration to parse")
    } else {
      return bbDecl
    }
  }
  
}