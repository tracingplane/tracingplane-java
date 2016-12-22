package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler

import scala.collection.mutable.Set
import scala.collection.mutable.LinkedHashSet
import scala.collection.mutable.LinkedHashMap
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast._
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.BuiltInType._

/** Compiles BaggageBuffers declarations to Java */
class JavaCompiler extends Compiler {
  
  override def compile(outputDir: String, bagDecl: BagDeclaration): Unit = {
    new CompilerInstance(bagDecl).compile(outputDir)
  }
  
  class CompilerInstance(bagDecl: BagDeclaration) {
    
    def compile(): String = {
      return JavaCompilerUtils.formatIndentation(new BagToCompile(bagDecl).declaration, "    ");
    }
    
    def compile(outputDir: String): Unit = {
      val toCompile = new BagToCompile(bagDecl)
      val text = JavaCompilerUtils.formatIndentation(toCompile.declaration, "    ");
      JavaCompilerUtils.writeOutputFile(outputDir, toCompile.PackageName, toCompile.Name, text)
    }
    
    var importedAndReserved = List[String](bagDecl.name, "Handler")
    var toImport = List[String]()
    
    def importIfPossible(fqn: String): String = {
      val className = fqn.drop(fqn.lastIndexOf(".")+1)
      if (importedAndReserved contains className) {
        return fqn
      } else {
        toImport = toImport :+ fqn
        importedAndReserved = importedAndReserved :+ className
        return className
      }
    }
    
    // Built-in types that are used
    val Set = importIfPossible("java.util.Set")
    val Map = importIfPossible("java.util.Map")
    
    // Logging
    val Logger = importIfPossible("org.slf4j.Logger")
    val LoggerFactory = importIfPossible("org.slf4j.LoggerFactory")
    
    // Transit layer api
    val Baggage = importIfPossible("edu.brown.cs.systems.tracingplane.transit_layer.Baggage")
    
    // Baggage layer api
    val BagKey = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_layer.BagKey")
    val BaggageReader = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader")
    val BaggageWriter = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter")
    
    // Baggage buffers api
    val BaggageBuffers = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.BaggageBuffers")
    val Registrations = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.Registrations")
    val Bag = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.api.Bag")
    val Parser = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.api.Parser")
    val Serializer = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.api.Serializer")
    val Brancher = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.api.Brancher")
    val Joiner = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.api.Joiner")
    val BaggageHandler = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.api.BaggageHandler")
    
    // Baggage buffers helpers
    val ReaderHelpers = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.impl.ReaderHelpers")
    val WriterHelpers = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.impl.WriterHelpers")
    val Parsers = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.impl.Parsers")
    val Serializers = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.impl.Serializers")
    val Branchers = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.impl.Branchers")
    val Joiners = importIfPossible("edu.brown.cs.systems.tracingplane.baggage_buffers.impl.Joiners")
    
    
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
        
        case BuiltInType.Set(of) => return s"$Set<${javaType(of)}>"
        case BuiltInType.Map(k, v) => return s"$Map<${javaType(k)}, ${javaType(v)}>"
        
        case UserDefinedType(packageName, name) => return s"$packageName.${javaName(name)}"
      }
    }
    
    def handler(udt: UserDefinedType): String = s"${udt.packageName}.${udt.name}.Handler.instance"
    
    def parser(fieldType: FieldType): String = {
      fieldType match {
        case prim: PrimitiveType => return s"$Parsers.${prim}Parser()"
        case udt: UserDefinedType => return handler(udt)
        case BuiltInType.Set(of) => return s"$Parsers.setParser(${parser(of)})"
        case BuiltInType.Map(k, v) => return s"$Parsers.mapParser(${keyParser(k)}, ${parser(v)})"
      }
    }
    
    def serializer(fieldType: FieldType): String = {
      fieldType match {
        case prim: PrimitiveType => return s"$Serializers.${prim}Serializer()"
        case udt: UserDefinedType => return handler(udt)
        case BuiltInType.Set(of) => return s"$Serializers.setSerializer(${serializer(of)})"
        case BuiltInType.Map(k, v) => return s"$Serializers.mapSerializer(${keySerializer(k)}, ${serializer(v)})"
      }
    }
    
    def joiner(fieldType: FieldType): String = {
      fieldType match {
        case prim: PrimitiveType => return s"$Joiners.<${javaType(fieldType)}>first()"
        case udt: UserDefinedType => return handler(udt)
        case BuiltInType.Set(of) => return s"$Joiners.<${javaType(of)}>setUnion()"
        case BuiltInType.Map(k, v) => return s"$Joiners.<${javaType(k)}, ${javaType(v)}>mapMerge(${joiner(v)})"
      }
    }
    
    def brancher(fieldType: FieldType): String = {
      fieldType match {
        case prim: PrimitiveType => return s"$Branchers.<${javaType(fieldType)}>noop()"
        case udt: UserDefinedType => return handler(udt)
        case BuiltInType.Set(of) => return s"$Branchers.<${javaType(of)}>set()"
        case BuiltInType.Map(k, v) => return s"$Branchers.<${javaType(k)}, ${javaType(v)}>map(${brancher(v)})"
      }
    }
    
    def keyParser(primitiveType: PrimitiveType): String = {
      return s"$ReaderHelpers.to_$primitiveType"
    }
    
    def keySerializer(primitiveType: PrimitiveType): String = {
      return s"$WriterHelpers.from_$primitiveType"    
    }
    
    abstract class FieldToCompile(decl: FieldDeclaration) {
      
      val Name: String = javaName(decl.name)
      val Type: String = javaType(decl.fieldtype)
      val DefaultValue: String = "null"
      val fieldDeclaration = s"public $Type $Name = $DefaultValue;"
      
      val BagKeyName = s"_${Name}Key"
      val bagKeyFieldDeclaration = s"private static final $BagKey $BagKeyName = $BagKey.indexed(${decl.index});"
      
      val ParserName: String
      val SerializerName: String
      val BrancherName: String
      val JoinerName: String
      val privateFieldsDeclaration: String
      
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
      
      def branchStatement(instance: String, newInstance: String) = s"$newInstance.$Name = $BrancherName.branch($instance.$Name);"
      
      def joinStatement(left: String, right: String, newInstance: String) = s"$newInstance.$Name = $JoinerName.join($left.$Name, $right.$Name);"
      
    }
    
    class BuiltInFieldToCompile(decl: FieldDeclaration) extends FieldToCompile(decl) {
      val ParserName = s"_${Name}Parser"
      val SerializerName = s"_${Name}Serializer"
      val BrancherName = s"_${Name}Brancher"
      val JoinerName = s"_${Name}Joiner"
      
      val privateFieldsDeclaration: String = s"""
          private static final $Parser<$Type> $ParserName = ${parser(decl.fieldtype)};
          private static final $Serializer<$Type> $SerializerName = ${serializer(decl.fieldtype)};
          private static final $Brancher<$Type> $BrancherName = ${brancher(decl.fieldtype)};
          private static final $Joiner<$Type> $JoinerName = ${joiner(decl.fieldtype)};"""
    }
    
    class UserDefinedFieldToCompile(decl: FieldDeclaration, userfield: UserDefinedType) extends FieldToCompile(decl) {
      val HandlerName = s"_${Name}Handler"
      val ParserName = HandlerName
      val SerializerName = HandlerName
      val BrancherName = HandlerName
      val JoinerName = HandlerName
      
      val privateFieldsDeclaration: String = s"""
          private static final $BaggageHandler<$Type> $HandlerName = ${handler(userfield)};"""
    }
    
    class BagToCompile(decl: BagDeclaration) {
      
      val Name: String = javaName(decl.name)
      val PackageName: String = decl.packageName
      val varName: String = Name.head.toLower + Name.tail
      
      val fields = decl.fields.sortWith(_.index < _.index).map {
        x => x match {
          case FieldDeclaration(fieldtype: UserDefinedType, _, _) => new UserDefinedFieldToCompile(x, fieldtype)
          case _ => new BuiltInFieldToCompile(x) 
        }
      }
      
      val declaration = s"""/** Generated by BaggageBuffersCompiler */
        package ${decl.packageName};

        ${toImport.map(x => s"import $x;").mkString("\n")}
    
        public class $Name implements $Bag {

            private static final $Logger _log = $LoggerFactory.getLogger($Name.class);
    
            ${fields.map(_.fieldDeclaration).mkString("\n")}
        
            public boolean _overflow = false;

            /**
             * <p>
             * Get the {@link $Name} set in the {@link $Baggage} carried by the current thread. If no baggage is being
             * carried by the current thread, or if there is no $Name in it, then this method returns {@code null}.
             * </p>
             * 
             * <p>
             * To get $Name from a specific Baggage instance, use {@link #getFrom($Baggage)}.
             * </p>
             * 
             * @return the $Name being carried in the {@link $Baggage} of the current thread, or {@code null}
             *         if none is being carried. The returned instance maybe be modified and modifications will be reflected in
             *         the baggage.
             */
            public static $Name get() {
                $Bag bag = $BaggageBuffers.get(Handler.registration());
                if (bag instanceof $Name) {
                    return ($Name) bag;
                } else {
                    return null;
                }
            }
        
            /**
             * <p>
             * Get the {@link $Name} set in {@code baggage}. If {@code baggage} has no $Name set then
             * this method returns null.
             * </p>
             * 
             * <p>
             * This method does <b>not</b> affect the Baggage being carried by the current thread.  To get $Name
             * from the current thread's Baggage, use {@link #get()}.
             * </p>
             * 
             * @param baggage A baggage instance to get the {@link $Name} from
             * @return the {@link $Name} instance being carried in {@code baggage}, or {@code null} if none is being carried.
             *         The returned instance can be modified, and modifications will be reflected in the baggage.
             */
            public static $Name getFrom($Baggage baggage) {
                $Bag bag = $BaggageBuffers.get(Handler.registration());
                if (bag instanceof $Name) {
                    return ($Name) bag;
                } else if (bag != null) {
                    Handler.checkRegistration();
                }
                return null;
            }
        
            /**
             * <p>
             * Update the {@link $Name} set in the current thread's baggage. This method will overwrite any existing
             * $Name set in the current thread's baggage.
             * </p>
             * 
             * <p>
             * To set the {@link $Name} in a specific {@link $Baggage} instance, use
             * {@link #setIn($Baggage, $Name)}
             * </p>
             * 
             * @param $varName the new {@link $Name} to set in the current thread's {@link $Baggage}. If {@code null}
             *            then any existing mappings will be removed.
             */
            public static void set($Name $varName) {
                $BaggageBuffers.set(Handler.registration(), $varName);
            }
        
            /**
             * <p>
             * Update the {@link $Name} set in {@code baggage}. This method will overwrite any existing
             * $Name set in {@code baggage}.
             * </p>
             * 
             * <p>
             * This method does <b>not</b> affect the {@link $Baggage} being carried by the current thread. To set the
             * {@link $Name} for the current thread, use {@link #set($Name)}
             * </p>
             * 
             * @param baggage A baggage instance to set the {@link $Name} in
             * @param $varName the new $Name to set in {@code baggage}. If {@code null}, it will remove any
             *            mapping present.
             * @return a possibly new {@link $Baggage} instance that contains all previous mappings plus the new mapping.
             */
            public static $Baggage setIn($Baggage baggage, $Name $varName) {
                return $BaggageBuffers.set(baggage, Handler.registration(), $varName);
            }

            @Override
            public $BaggageHandler<?> handler() {
                return Handler.instance;
            }
            
            public static class Handler implements $BaggageHandler<$Name> {

                public static final Handler instance = new Handler();
                private static $BagKey registration = null;

                static synchronized $BagKey checkRegistration() {
                    registration = $Registrations.lookup(instance);
                    if (registration == null) {
                        _log.error("$Name MUST be registered to a key before it can be propagated.  " +
                                   "There is currently no registration for $Name and it will not be propagated. " +
                                   "To register a bag set the baggage-buffers.bags property in your application.conf " +
                                   "or with -Dbaggage-buffers.bags flag (eg, for key 10, -Dbaggage-buffers.bags.10=" + $Name.class.getName());
                    }
                    return registration;
                }

                static BagKey registration() {
                    return registration == null ? checkRegistration() : registration;
                }
                
                private Handler(){}
        
                ${fields.map(_.bagKeyFieldDeclaration).mkString("\n")}
                ${fields.map(_.privateFieldsDeclaration).mkString("\n")}
                
                @Override
                public boolean isInstance($Bag bag) {
                    return bag == null || bag instanceof $Name;
                }
    
                @Override
                public $Name parse($BaggageReader reader) {
                    $Name instance = new $Name();
                    ${fields.map(_.parseStatement("reader", "instance")).mkString("\n")}
                    instance._overflow = reader.didOverflow();
    
                    return instance;
                }
    
                @Override
                public void serialize($BaggageWriter writer, $Name instance) {
                    if (instance == null) {
                        return;
                    }
    
                    writer.didOverflowHere(instance._overflow);
                    ${fields.map(_.serializeStatement("writer", "instance")).mkString("\n")}
                }
    
                @Override
                public $Name branch($Name instance) {
                    if (instance == null) {
                        return null;
                    }
                    
                    $Name newInstance = new $Name();
                    ${fields.map(_.branchStatement("instance", "newInstance")).mkString("\n")}
                    return newInstance;
                }
    
                @Override
                public $Name join($Name left, $Name right) {
                    if (left == null) {
                        return right;
                    } else if (right == null) {
                        return left;
                    } else {
                        ${fields.map(_.joinStatement("left", "right", "left")).mkString("\n")}
                        return left;
                    }
                }
            }
        }"""
      
    }
    
  }
  
}