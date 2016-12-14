package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler


import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Declarations._
import fastparse.all._
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.FieldType.BuiltInType
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.FieldType.UserDefined.UserDefinedType
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast._

class Checker {
    
  val filesSeen: Set[String] = Set[String]() 
  val declaredBags: Set[String] = Set[String]()
  
  def parse(fileContents: String) = {
    bbDeclaration.parse(fileContents) match {
      case failure: Parsed.Failure => { 
        val error = ParseError(failure)
        println("Error: " + error.getMessage)
        throw error
      }
      case Parsed.Success(res, _) => addBBDeclaration(res)
    }   
  }
    
  def addBBDeclaration(decl: BaggageBuffersDeclaration) = {
    println(s"Adding BB declaration $decl");
  }
  
}