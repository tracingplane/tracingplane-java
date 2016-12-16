package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler

import scala.collection.JavaConverters._
import scala.collection.mutable.Map
import fastparse.all._

object Ast {

  sealed trait FieldType
  
  sealed abstract class ParameterizedType(parameters: List[FieldType]) extends FieldType {
    def getParameters(): java.util.List[FieldType] = {
      return parameters.asJava
    }
  }
  
  case class UserDefinedType(name: String) extends FieldType
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
    
    case class Set(of: FieldType) extends ParameterizedType(List[FieldType](of)) with BuiltInType
    
  }
  
  
  case class FieldDeclaration(fieldtype: FieldType, name: String, index: Int)
  
  case class BagDeclaration(name: String, fields: Seq[FieldDeclaration]) {
    def fullyQualifiedName(packageDeclaration: Option[PackageDeclaration]): String = {
      packageDeclaration match {
        case Some(decl) => return fullyQualifiedName(decl)
        case None => return name
      }
    }
    def fullyQualifiedName(packageDeclaration: PackageDeclaration): String = {
      return packageDeclaration.getFullyQualifiedBagName(name)
    }
    def getFieldDeclarations(): java.util.List[FieldDeclaration] = {
      return fields.asJava
    }
  }
  
  case class PackageDeclaration(packageName: Seq[String]) {
    def getPackageName(): java.util.List[String] = {
      return packageName.asJava
    }
    def getPackageNameString(): String = {
      return packageName.mkString(".")
    }
    def getFullyQualifiedBagName(bagName: String): String = {
      return (packageName + bagName).mkString(".")
    }
  }
  
  case class ImportDeclaration(filename: String)
  
  case class BaggageBuffersDeclaration(packageDeclaration: Option[PackageDeclaration], imports: Seq[ImportDeclaration], bagDeclarations: Seq[BagDeclaration]) {
    def getBagDeclarations(): java.util.List[BagDeclaration] = {
      return bagDeclarations.asJava
    }
    def getImportDeclarations(): java.util.List[ImportDeclaration] = {
      return imports.asJava
    }
    def getPackageDeclaration(): PackageDeclaration = {
      return packageDeclaration.getOrElse(null)
    }
    def isEmpty(): Boolean = {
      return packageDeclaration.isEmpty && imports.isEmpty && bagDeclarations.isEmpty
    }
  }
  
}