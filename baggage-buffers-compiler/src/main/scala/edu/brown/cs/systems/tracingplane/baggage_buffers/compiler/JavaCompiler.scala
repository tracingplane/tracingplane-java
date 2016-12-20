package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler

import scala.collection.mutable.Set
import scala.collection.mutable.LinkedHashSet
import scala.collection.mutable.LinkedHashMap
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast._
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.BuiltInType._

/** Compiles BaggageBuffers declarations to Java */
class JavaCompiler extends Compiler {
  
  override def compile(outputDir: String, bagDecl: BagDeclaration): Unit = {
    JavaCompiler.compile(outputDir, bagDecl)
  }
  
  object JavaCompiler {
    
    def compile(bagDecl: BagDeclaration): String = {
      return JavaCompilerUtils.formatIndentation(new BagToCompile(bagDecl).declaration, "    ");
    }
    
    def compile(outputDir: String, bagDecl: BagDeclaration): Unit = {
      val toCompile = new BagToCompile(bagDecl)
      val text = JavaCompilerUtils.formatIndentation(toCompile.declaration, "    ");
      JavaCompilerUtils.writeOutputFile(outputDir, toCompile.PackageName, toCompile.Name, text)
    }
    
    // Baggage layer
    val BagKey = "edu.brown.cs.systems.tracingplane.baggage_layer.BagKey"
    val Parser = "edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Parser"
    val Serializer = "edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Serializer"
    val BaggageReader = "edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader"
    val BaggageWriter = "edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter"
    
    // Baggage buffers helpers
    val ReaderHelpers = "edu.brown.cs.systems.tracingplane.baggage_buffers.impl.ReaderHelpers"
    val WriterHelpers = "edu.brown.cs.systems.tracingplane.baggage_buffers.impl.WriterHelpers"
    val Parsers = "edu.brown.cs.systems.tracingplane.baggage_buffers.impl.Parsers"
    val Serializers = "edu.brown.cs.systems.tracingplane.baggage_buffers.impl.Serializers"
    
    
    def javaName(name: String): String = JavaCompilerUtils.formatCamelCase(name)
    
    def javaType(fieldType: FieldType): String = {
      fieldType match {
        case BuiltInType.bool => return "Boolean"
        case BuiltInType.int32 => return "Integer"
        case BuiltInType.sint32 => return "Integer"
        case BuiltInType.fixed32 => return "Integer"
        case BuiltInType.sfixed32 => return "Integer"
        case BuiltInType.int64 => return "Long"
        case BuiltInType.sint64 => return "Long"
        case BuiltInType.fixed64 => return "Long"
        case BuiltInType.sfixed64 => return "Long"
        case BuiltInType.float => return "Float"
        case BuiltInType.double => return "Double"
        case BuiltInType.string => return "String"
        case BuiltInType.bytes => return "java.nio.ByteBuffer"
        
        case BuiltInType.Set(of) => return s"java.util.Set<${javaType(of)}>"
        case BuiltInType.Map(k, v) => return s"java.util.Map<${javaType(k)}, ${javaType(v)}>"
        
        case UserDefinedType(packageName, name) => return s"$packageName.${javaName(name)}"
      }
    }
    
    def parser(fieldType: FieldType): String = {
      fieldType match {
        case prim: PrimitiveType => return s"$Parsers.${prim}Parser()"
        case UserDefinedType(packageName, name) => return s"$packageName.$name._parser"
        case BuiltInType.Set(of) => return s"$Parsers.setParser(${parser(of)})"
        case BuiltInType.Map(k, v) => return s"$Parsers.mapParser(${keyParser(k)}, ${parser(v)})"
      }
    }
    
    def serializer(fieldType: FieldType): String = {
      fieldType match {
        case prim: PrimitiveType => return s"$Serializers.${prim}Serializer()"
        case UserDefinedType(packageName, name) => return s"$packageName.$name._serializer"
        case BuiltInType.Set(of) => return s"$Serializers.setSerializer(${serializer(of)})"
        case BuiltInType.Map(k, v) => return s"$Serializers.mapSerializer(${keySerializer(k)}, ${serializer(v)})"
      }
    }
    
    def keyParser(primitiveType: PrimitiveType): String = {
      return s"$ReaderHelpers.to_$primitiveType"
    }
    
    def keySerializer(primitiveType: PrimitiveType): String = {
      return s"$WriterHelpers.from_$primitiveType"    
    }
    
    class FieldToCompile(decl: FieldDeclaration) {
      
      val Name: String = javaName(decl.name)
      val Type: String = javaType(decl.fieldtype)
      val DefaultValue: String = "null"
      
      val BagKeyName = s"_${Name}Key"
      val ParserName = s"_${Name}Parser"
      val SerializerName = s"_${Name}Serializer"
      
      val fieldDeclaration = s"public $Type $Name = $DefaultValue;"
      val bagKeyFieldDeclaration = s"private static final $BagKey $BagKeyName = $BagKey.indexed(${decl.index});"
      val parserFieldDeclaration = s"private static final $Parser<$Type> $ParserName = ${parser(decl.fieldtype)};"
      val serializerFieldDeclaration = s"private static final $Serializer<$Type> $SerializerName = ${serializer(decl.fieldtype)};"
      
      def parseStatement(reader: String, instance: String) = s"""
          if ($reader.enter($BagKeyName)) {
              $instance.$Name = $ParserName.parse($reader);
              $reader.exit();
          }"""
      
      def serializeStatement(writer: String, instance: String) = s"""
          if ($instance.$Name != null) {
              $writer.enter($BagKeyName);
              $SerializerName.serialize($writer, $instance.$Name);
              $writer.exit();
          }"""
      
    }
    
    class BagToCompile(decl: BagDeclaration) {
      
      val Name: String = javaName(decl.name)
      val PackageName: String = decl.packageName
      
      val fields = decl.fields.sortWith(_.index < _.index).map(new FieldToCompile(_))
      
      val ParserClass = s"${Name}Parser"
      val SerializerClass = s"${Name}Serializer"
      
      val declaration = s"""/** Generated by BaggageBuffersCompiler */
        package ${decl.packageName};
    
        public class $Name {
    
            ${fields.map(_.fieldDeclaration).mkString("\n")}
        
            public boolean _overflow = false;
        
            ${fields.map(_.bagKeyFieldDeclaration).mkString("\n")}
            
            public static final $Parser<$Name> _parser = new $ParserClass();
            private static class $ParserClass implements $Parser<$Name> {

                ${fields.map(_.parserFieldDeclaration).mkString("\n")}
    
                @Override
                public $Name parse($BaggageReader reader) {
                    $Name instance = new $Name();
                    ${fields.map(_.parseStatement("reader", "instance")).mkString("\n")}
                    instance._overflow = reader.didOverflow();
    
                    return instance;
                }
            }
    
            public static final $Serializer<$Name> _serializer = new $SerializerClass();
            private static class $SerializerClass implements $Serializer<$Name> {

                ${fields.map(_.serializerFieldDeclaration).mkString("\n")}
    
                @Override
                public void serialize($BaggageWriter writer, $Name instance) {
                    if (instance == null) {
                        return;
                    }
    
                    writer.didOverflowHere(instance._overflow);
                    ${fields.map(_.serializeStatement("writer", "instance")).mkString("\n")}
                }
        
            }
        }"""
      
    }
    
  }
  
}