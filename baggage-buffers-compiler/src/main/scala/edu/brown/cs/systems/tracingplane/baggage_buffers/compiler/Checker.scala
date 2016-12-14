package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler

import scala.collection.mutable.Set
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Declarations._
import fastparse.all._
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.FieldType.BuiltInType
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.FieldType.UserDefined.UserDefinedType
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast._
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.BBC.Settings
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map

class Checker(settings: Settings) {
  
    
  val filesSeen: Set[String] = Set[String]()
  val currentlyParsing: ArrayBuffer[String] = ArrayBuffer[String]()
  
  val bags: Map[String, Bag] = Map[String, Bag]()
  
  
  
  class Bag(packageName: String, bagName: String) {
    val fullyQualifiedName = s"$packageName.$bagName"
    
  }
  
  def parse(filename: String, fileContents: String): Unit = {
    currentlyParsing += filename
    bbDeclaration.parse(fileContents) match {
      case failure: Parsed.Failure => {
        val error = ParseError(failure)
        throw new ByteBufferCompilerException("Error parsing "+filename+": "+error.getMessage, error)  
      }
      case Parsed.Success(res, _) => addBBDeclaration(res)
    }   
    currentlyParsing.dropRight(1)
  }
  
  def processImport(decl: ImportDeclaration) = {
    if (currentlyParsing contains decl.filename) {
      throw new ByteBufferCompilerException("Circular dependency between "+decl.filename+" and "+currentlyParsing.last)
    }
    if (!(filesSeen contains decl.filename)) {
      filesSeen += decl.filename
      parse(decl.filename, settings.loadImport(decl.filename))
    }
  }
    
  def addBBDeclaration(decl: BaggageBuffersDeclaration) = {
    println(s"Adding BB declaration $decl");
    
    decl.imports.foreach { importDecl => processImport(importDecl) }
    
    
  }
  
}