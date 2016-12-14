package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler

import scala.collection.mutable.Map
import fastparse.all._

object Ast {

  sealed trait FieldType
  object FieldType {
  
    sealed trait BuiltInType extends FieldType
    object BuiltInType {
  
      sealed trait Numeric extends BuiltInType
      sealed trait Float extends Numeric
      sealed trait Signed extends Numeric
      sealed trait Unsigned extends Numeric
  
      case object bool extends Unsigned
      case object int32 extends Unsigned
      case object int64 extends Unsigned
      case object sint32 extends Signed
      case object sint64 extends Signed
      case object fixed32 extends Unsigned
      case object fixed64 extends Unsigned
      case object sfixed32 extends Signed
      case object sfixed64 extends Signed
      case object float extends Float
      case object double extends Float
      
      case object string extends BuiltInType
      case object bytes extends BuiltInType
      
      case class Set(of: FieldType) extends BuiltInType
      
    }
    
    sealed trait UserDefined extends FieldType
    object UserDefined {
      case class UserDefinedType(name: String) extends FieldType 
      case class UserDefinedParameterizedType(name: String, parameters: Seq[FieldType]) extends FieldType
    }
  }
  
  case class FieldDeclaration(instanceof: FieldType, name: String, index: Int)
  
  case class BagDeclaration(name: String, fields: Seq[FieldDeclaration])
  
  case class PackageDeclaration(packageName: Seq[String])
  
  case class ImportDeclaration(filename: String)
  
  case class BaggageBuffersDeclaration(packageDeclaration: Option[PackageDeclaration], imports: Seq[ImportDeclaration], bagDeclarations: Seq[BagDeclaration])
  
}