package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler

import scala.collection.JavaConverters._
import scala.collection.mutable.Map
import fastparse.all._
import scala.reflect.runtime.universe._


object Ast {

  sealed trait FieldType {
    def isValid: Boolean = true
  }
  
  sealed abstract class ParameterizedType(parameters: List[FieldType]) extends FieldType {
    def getParameters(): java.util.List[FieldType] = {
      return parameters.asJava
    }
    override def toString(): String = {
      return s"${this.getClass.getSimpleName}<${parameters.mkString(", ")}>"
    }
  }
  
  case class UserDefinedType(var packageName: String, name: String, var structType: Boolean = false) extends FieldType {
    def isStructType(): Boolean = {
      return structType
    }
    def hasPackageName(): Boolean = {
      return packageName != null && !packageName.equals("")
    }
    override def toString(): String = {
      if (hasPackageName()) {
        return s"$packageName.$name"
      } else {
        return name
      }
    }
  }
  
  sealed trait BuiltInType extends FieldType
  
  sealed trait PrimitiveType extends BuiltInType {
    override def toString(): String = {
      val className = this.getClass.getName
      return className.substring(BuiltInType.getClass.getName.length(), className.length()-1)
//      return this.getClass.getSimpleName // SI-2034
    }
  }
  
  object BuiltInType {
    sealed trait Numeric extends PrimitiveType
    sealed trait Float extends Numeric
    sealed trait Signed extends Numeric
    sealed trait Unsigned extends Numeric

    case object taint extends Unsigned
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
    
    case object string extends PrimitiveType
    case object bytes extends PrimitiveType
    
    case object Counter extends BuiltInType
    
    case class Set(of: FieldType) extends ParameterizedType(List[FieldType](of)) with BuiltInType {
      override def isValid(): Boolean = {
        of match {
          case t: PrimitiveType => return true
          case UserDefinedType(_,_,isStructType) => return isStructType
          case _ => return false
        }
      }
    }
    case class Map(keyType: PrimitiveType, valueType: FieldType) extends ParameterizedType(List[FieldType](keyType, valueType)) with BuiltInType
    
    
  }
  
  
  case class FieldDeclaration(fieldtype: FieldType, name: String, index: Int) {
    override def toString(): String = {
      return s"$fieldtype $name = $index"
    }
  }
  
  case class StructFieldDeclaration(fieldtype: FieldType, name: String) {
    override def toString(): String = {
      return s"$fieldtype $name"
    }
  }
  
  abstract class ObjectDeclaration(val name: String) {
    var packageName: String = ""; // Filled in later
    def fullyQualifiedName(packageDeclaration: Option[PackageDeclaration]): String = {
      packageDeclaration match {
        case Some(decl) => return fullyQualifiedName(decl)
        case None => return name
      }
    }
    def fullyQualifiedName(packageDeclaration: PackageDeclaration): String = {
      return packageDeclaration.getFullyQualifiedBagName(name)
    }
  }
  
  case class BagDeclaration(override val name: String, fields: Seq[FieldDeclaration]) extends ObjectDeclaration(name) {
    fields.sortWith(_.index < _.index)
    
    def getFieldDeclarations(): java.util.List[FieldDeclaration] = {
      return fields.asJava
    }
  }
  
  case class StructDeclaration(override val name: String, fields: Seq[StructFieldDeclaration]) extends ObjectDeclaration(name) {
    def getFieldDeclarations(): java.util.List[StructFieldDeclaration] = {
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
  
  case class BaggageBuffersDeclaration(packageDeclaration: Option[PackageDeclaration], imports: Seq[ImportDeclaration], objectDeclarations: Seq[ObjectDeclaration]) {
    val bagDeclarations: Seq[BagDeclaration] = objectDeclarations.filter(_.isInstanceOf[BagDeclaration]).map(_.asInstanceOf[BagDeclaration])
    val structDeclarations: Seq[StructDeclaration] = objectDeclarations.filter(_.isInstanceOf[StructDeclaration]).map(_.asInstanceOf[StructDeclaration])
    def getObjectDeclarations(): java.util.List[ObjectDeclaration] = {
      return objectDeclarations.asJava
    }
    def getBagDeclarations(): java.util.List[BagDeclaration] = {
      return bagDeclarations.asJava
    }
    def getStructDeclarations(): java.util.List[StructDeclaration] = {
      return structDeclarations.asJava
    }
    def getImportDeclarations(): java.util.List[ImportDeclaration] = {
      return imports.asJava
    }
    def getPackageDeclaration(): PackageDeclaration = {
      return packageDeclaration.getOrElse(null)
    }
    def getPackageNameString(): String = {
      packageDeclaration match {
        case Some(decl) => return decl.getPackageNameString()
        case _ => return ""
      }
    }
    def isEmpty(): Boolean = {
      return packageDeclaration.isEmpty && imports.isEmpty && bagDeclarations.isEmpty && structDeclarations.isEmpty
    }
  }
  
}