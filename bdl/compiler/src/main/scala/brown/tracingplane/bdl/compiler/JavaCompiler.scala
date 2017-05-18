package brown.tracingplane.bdl.compiler

import scala.collection.mutable.Set
import scala.collection.mutable.LinkedHashSet
import scala.collection.mutable.LinkedHashMap
import brown.tracingplane.bdl.compiler.Ast._
import brown.tracingplane.bdl.compiler.Ast.BuiltInType._

/** Compiles BaggageBuffers declarations to Java */
class JavaCompiler extends Compiler {

  override def compile(outputDir: String, objectDecl: ObjectDeclaration): Unit = {
    compiler(objectDecl).compile(outputDir)
  }

  def compiler(objectDecl: ObjectDeclaration): CompilerInstance = {
    objectDecl match {
      case bagDecl: BagDeclaration => return new BagCompilerInstance(bagDecl)
      case structDecl: StructDeclaration => return new StructCompilerInstance(structDecl)
    }
  }

  abstract class CompilerInstance(objectDecl: ObjectDeclaration) {

    def compile(): String

    def compile(outputDir: String): Unit

    var importedAndReserved = List[String](objectDecl.name, "Handler")
    var toImport = List[String]()

    def importIfPossible(fqn: String): String = {
      val className = fqn.drop(fqn.lastIndexOf(".") + 1)
      if (toImport contains fqn) {
        return className
      } else if (importedAndReserved contains className) {
        return fqn
      } else {
        toImport = toImport :+ fqn
        importedAndReserved = importedAndReserved :+ className
        return className
      }
    }

    // Built-in types that are used
    def Set = importIfPossible("java.util.Set")
    def Map = importIfPossible("java.util.Map")
    def ByteBuffer = importIfPossible("java.nio.ByteBuffer")
    def Objects = importIfPossible("java.util.Objects")

    // Logging
    def Logger = importIfPossible("org.slf4j.Logger")
    def LoggerFactory = importIfPossible("org.slf4j.LoggerFactory")

    // Transit layer api
    def Baggage = importIfPossible("brown.tracingplane.BaggageContext")
    def ActiveBaggage = importIfPossible("brown.tracingplane.ActiveBaggage")

    // Baggage layer api
    def BagKey = importIfPossible("brown.tracingplane.baggageprotocol.BagKey")
    def BaggageReader = importIfPossible("brown.tracingplane.baggageprotocol.BaggageReader")
    def BaggageWriter = importIfPossible("brown.tracingplane.baggageprotocol.BaggageWriter")

    // Baggage buffers api
    def BDLContextProvider = importIfPossible("brown.tracingplane.impl.BDLContextProvider")
    def BaggageHandlerRegistry = importIfPossible("brown.tracingplane.impl.BaggageHandlerRegistry")
    def Bag = importIfPossible("brown.tracingplane.bdl.Bag")
    def Struct = importIfPossible("brown.tracingplane.bdl.Struct")
    def Parser = importIfPossible("brown.tracingplane.bdl.Parser")
    def Serializer = importIfPossible("brown.tracingplane.bdl.Serializer")
    def Brancher = importIfPossible("brown.tracingplane.bdl.Brancher")
    def Joiner = importIfPossible("brown.tracingplane.bdl.Joiner")
    def BaggageHandler = importIfPossible("brown.tracingplane.bdl.BaggageHandler")
    def BBUtils = importIfPossible("brown.tracingplane.bdl.BDLUtils")
    def Counter = importIfPossible("brown.tracingplane.bdl.SpecialTypes.Counter")
    def CounterImpl = importIfPossible("brown.tracingplane.bdl.CounterImpl")
    def StructReader = s"${importIfPossible("brown.tracingplane.bdl.Struct")}.StructReader"
    def StructSizer = s"${importIfPossible("brown.tracingplane.bdl.Struct")}.StructSizer"
    def StructWriter = s"${importIfPossible("brown.tracingplane.bdl.Struct")}.StructWriter"
    def StructHandler = s"${importIfPossible("brown.tracingplane.bdl.Struct")}.StructHandler"

    // Baggage buffers helpers
    def ReaderHelpers = importIfPossible("brown.tracingplane.bdl.ReaderHelpers")
    def WriterHelpers = importIfPossible("brown.tracingplane.bdl.WriterHelpers")
    def Parsers = importIfPossible("brown.tracingplane.bdl.Parsers")
    def Serializers = importIfPossible("brown.tracingplane.bdl.Serializers")
    def Branchers = importIfPossible("brown.tracingplane.bdl.Branchers")
    def Joiners = importIfPossible("brown.tracingplane.bdl.Joiners")
    def StructHelpers = importIfPossible("brown.tracingplane.bdl.StructHelpers")

    def javaName(name: String): String = JavaCompilerUtils.formatCamelCase(name)

    def javaType(fieldType: FieldType): String = {
      fieldType match {
        case BuiltInType.taint => return "Boolean"
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
        case BuiltInType.Counter => return s"$Counter"

        case UserDefinedType(packageName, name, _) => return importIfPossible(s"$packageName.${javaName(name)}")
      }
    }

    def handler(udt: UserDefinedType): String = s"${javaType(udt)}.Handler.instance"
    def counterHandler: String = s"$CounterImpl.Handler.instance"

    def parser(fieldType: FieldType): String = {
      fieldType match {
        case prim: PrimitiveType => return s"$Parsers.${prim}Parser()"
        case udt: UserDefinedType => return handler(udt)
        case BuiltInType.Counter => return s"($Parser)$counterHandler"
        case BuiltInType.Set(of) => return s"$Parsers.<${javaType(of)}>setParser(${parser(of)})"
        case BuiltInType.Map(k, v) => return s"$Parsers.<${javaType(k)},${javaType(v)}>mapParser(${keyParser(k)}, ${parser(v)})"
      }
    }

    def serializer(fieldType: FieldType): String = {
      fieldType match {
        case prim: PrimitiveType => return s"$Serializers.${prim}Serializer()"
        case udt: UserDefinedType => return handler(udt)
        case BuiltInType.Counter => return s"($Serializer)$counterHandler"
        case BuiltInType.Set(of) => return s"$Serializers.<${javaType(of)}>setSerializer(${serializer(of)})"
        case BuiltInType.Map(k, v) => return s"$Serializers.<${javaType(k)},${javaType(v)}>mapSerializer(${keySerializer(k)}, ${serializer(v)})"
      }
    }

    def joiner(fieldType: FieldType): String = {
      fieldType match {
        case BuiltInType.taint => return s"$Joiners.or()"
        case prim: PrimitiveType => return s"$Joiners.<${javaType(fieldType)}>first()"
        case udt: UserDefinedType => {
          udt.structType match {
            case true => return s"$Joiners.<${javaType(fieldType)}>first()"
            case false => return handler(udt)
          }
        }
        case BuiltInType.Counter => return s"($Joiner)$counterHandler"
        case BuiltInType.Set(of) => return s"$Joiners.<${javaType(of)}>setUnion()"
        case BuiltInType.Map(k, v) => return s"$Joiners.<${javaType(k)}, ${javaType(v)}>mapMerge(${joiner(v)})"
      }
    }

    def brancher(fieldType: FieldType): String = {
      fieldType match {
        case prim: PrimitiveType => return s"$Branchers.<${javaType(fieldType)}>noop()"
        case udt: UserDefinedType => {
          udt.structType match {
            case true => return s"$Branchers.<${javaType(fieldType)}>noop()"
            case false => return handler(udt)
          }
        }
        case BuiltInType.Counter => return s"($Brancher)$counterHandler"
        case BuiltInType.Set(of) => return s"$Branchers.<${javaType(of)}>set()"
        case BuiltInType.Map(k, v) => return s"$Branchers.<${javaType(k)}, ${javaType(v)}>map(${brancher(v)})"
      }
    }

    def toStringStatement(fieldtype: FieldType, instance: String): String = toStringStatement(fieldtype, instance, 0)

    def toStringStatement(fieldtype: FieldType, instance: String, recurseCount: Integer): String = {
      val lambdaVarname = s"_v$recurseCount"
      fieldtype match {
        case set: BuiltInType.Set => s"$BBUtils.toString($instance)"
        case BuiltInType.Map(k, v) => s"$BBUtils.toString($instance, $lambdaVarname -> ${toStringStatement(v, lambdaVarname, recurseCount + 1)})"
        case _ => s"String.valueOf($instance)"
      }
    }

    def keyParser(primitiveType: PrimitiveType): String = {
      return s"$ReaderHelpers.to_$primitiveType"
    }

    def keySerializer(primitiveType: PrimitiveType): String = {
      return s"$WriterHelpers.from_$primitiveType"
    }
  }

  class StructCompilerInstance(structDecl: StructDeclaration) extends CompilerInstance(structDecl) {

    override def compile(): String = {
      return JavaCompilerUtils.formatIndentation(new StructToCompile(structDecl).declaration, "    ");
    }

    override def compile(outputDir: String): Unit = {
      val toCompile = new StructToCompile(structDecl)
      val text = JavaCompilerUtils.formatIndentation(toCompile.declaration, "    ");
      JavaCompilerUtils.writeOutputFile(outputDir, toCompile.PackageName, toCompile.Name, text)
    }

    abstract class StructFieldToCompile(decl: StructFieldDeclaration) {

      val Name: String = javaName(decl.name)

      val Type: String = javaType(decl.fieldtype)

      def DefaultValue: String = {
        decl.fieldtype match {
          case BuiltInType.bool => return "false"
          case BuiltInType.int32 | BuiltInType.sint32 | BuiltInType.fixed32 => return "0"
          case BuiltInType.int64 | BuiltInType.sint64 | BuiltInType.fixed64 => return "0L"
          case BuiltInType.float => return "0.0f"
          case BuiltInType.double => return "0.0d"
          case BuiltInType.string => return "\"\""
          case BuiltInType.bytes => return s"$StructHelpers.EMPTY_BYTE_BUFFER"
          case _ => return s"new $Type()"
        }
      }
      val fieldDeclaration = s"public $Type $Name = $DefaultValue;"

      val defaultValueName = s"_${Name}_defaultValue"
      val defaultValueDeclaration = s"private static final $Type $defaultValueName = $DefaultValue;"

      def NullCheck(instance: String): String = s"$instance.$Name == null ? $defaultValueName : $instance.$Name"

      val ReaderName: String
      val SizerName: String
      val WriterName: String
      val privateFieldsDeclaration: String

      def equalsStatement(a: String, b: String) = s"if (!$BBUtils.equals($a.$Name, $b.$Name, $defaultValueName)) return false;"

      def readStatement(buf: String, instance: String) = s"$instance.$Name = $ReaderName.readFrom($buf);"

      def serializedSizeStatement(size: String, instance: String) = s"$size += $SizerName.serializedSize(${NullCheck(instance)});"

      def writeStatement(buf: String, instance: String) = s"$WriterName.writeTo($buf, ${NullCheck(instance)});"

      def toString(instance: String) = s"""$BBUtils.indent(String.format("$Name = %s\\n", ${toStringStatement(decl.fieldtype, NullCheck(instance))}))"""

    }

    class BuiltInStructFieldToCompile(decl: StructFieldDeclaration) extends StructFieldToCompile(decl) {
      val ReaderName = s"_${Name}Reader"
      val SizerName = s"_${Name}Sizer"
      val WriterName = s"_${Name}Writer"

      val privateFieldsDeclaration: String = s"""
          private static final $StructReader<$Type> $ReaderName = $StructHelpers.${decl.fieldtype}Reader;
          private static final $StructSizer<$Type> $SizerName = $StructHelpers.${decl.fieldtype}Sizer;
          private static final $StructWriter<$Type> $WriterName = $StructHelpers.${decl.fieldtype}Writer;"""
    }

    class UserDefinedStructFieldToCompile(decl: StructFieldDeclaration, userfield: UserDefinedType) extends StructFieldToCompile(decl) {
      val HandlerName = s"_${Name}Handler"
      val ReaderName = HandlerName
      val SizerName = HandlerName
      val WriterName = HandlerName

      val privateFieldsDeclaration: String = s"""
          private static final $StructHandler<$Type> $HandlerName = ${handler(userfield)};"""
    }

    class StructToCompile(decl: StructDeclaration) {

      val Name: String = javaName(decl.name)
      val PackageName: String = decl.packageName
      val varName: String = Name.head.toLower + Name.tail

      val fields = decl.fields.map {
        x =>
          x match {
            case StructFieldDeclaration(fieldtype: UserDefinedType, _) => new UserDefinedStructFieldToCompile(x, fieldtype)
            case _ => new BuiltInStructFieldToCompile(x)
          }
      }

      val body = s"""
        public class $Name implements $Struct {

            private static final $Logger _log = $LoggerFactory.getLogger($Name.class);
    
            ${fields.map(_.fieldDeclaration).mkString("\n")}
      
            private static final $Name _defaultValue = new $Name();
            ${fields.map(_.defaultValueDeclaration).mkString("\n")}

            @Override
            public $StructHandler<?> handler() {
                return Handler.instance;
            }

            @Override
            public String toString() {
                StringBuilder b = new StringBuilder();
                b.append("$Name{\\n");
                ${fields.map(x => s"b.append(${x.toString("this")});").mkString("\n")}
                b.append("}");
                return b.toString();
            }

            @Override
            public boolean equals(Object other) {
                if (other == null) {
                    return $Name.equals(this, _defaultValue);
                } else if (!(other instanceof $Name)) {
                    return false;
                } else {
                    return $Name.equals(this, ($Name) other);
                }
            }

            @Override
            public int hashCode() {
                int result = 1;
                ${fields.map(x => s"result = result * 37 + (${x.NullCheck("this")}).hashCode();").mkString("\n")}
                return result;
            }

            private static boolean equals($Name a, $Name b) {
                ${fields.map(_.equalsStatement("a", "b")).mkString("\n")}
                return true;
            }
            
            public static class Handler implements $StructHandler<$Name> {

                public static final Handler instance = new Handler();
                
                private Handler(){}
        
                ${fields.map(_.privateFieldsDeclaration).mkString("\n")}
    
                @Override
                public $Name readFrom($ByteBuffer buf) throws Exception {
                    $Name instance = new $Name();

                    try {
                        ${fields.map(_.readStatement("buf", "instance")).mkString("\n")}
                    } catch (Exception e) {
                        _log.warn("Exception parsing $Name ", e);
                    }
    
                    return instance;
                }
    
                @Override
                public void writeTo($ByteBuffer buf, $Name instance) {
                    try {
                        ${fields.map(_.writeStatement("buf", "instance")).mkString("\n")}
                    } catch (Exception e) {
                        _log.warn("Exception serializing $Name ", e);
                    }
                }

                @Override
                public int serializedSize($Name instance) {
                    int size = 0;
                    ${fields.map(_.serializedSizeStatement("size", "instance")).mkString("\n")}
                    return size;
                }
            }
        }"""

      val declaration = s"""/** Generated by BaggageBuffersCompiler */
        package ${decl.packageName};

        ${toImport.map(x => s"import $x;").mkString("\n")}
        
        ${body}"""

    }

  }

  class BagCompilerInstance(bagDecl: BagDeclaration) extends CompilerInstance(bagDecl) {

    override def compile(): String = {
      return JavaCompilerUtils.formatIndentation(new BagToCompile(bagDecl).declaration, "    ");
    }

    override def compile(outputDir: String): Unit = {
      val toCompile = new BagToCompile(bagDecl)
      val text = JavaCompilerUtils.formatIndentation(toCompile.declaration, "    ");
      JavaCompilerUtils.writeOutputFile(outputDir, toCompile.PackageName, toCompile.Name, text)
    }

    abstract class FieldToCompile(decl: FieldDeclaration) {

      val Name: String = javaName(decl.name)
      val Type: String = javaType(decl.fieldtype)
      def DefaultValue: String = {
        decl.fieldtype match {
          case BuiltInType.taint => return "false"
          case _ => return "null"
        }
      }
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

      def toString(instance: String) = s"""$instance.$Name == null ? "" : $BBUtils.indent(String.format("$Name = %s\\n", ${toStringStatement(decl.fieldtype, s"$instance.$Name")}))"""

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

    class CounterToCompile(decl: FieldDeclaration) extends FieldToCompile(decl) {
      val HandlerName = s"_${Name}Handler"
      val ParserName = HandlerName
      val SerializerName = HandlerName
      val BrancherName = HandlerName
      val JoinerName = HandlerName

      val privateFieldsDeclaration: String = s"""
          private static final $BaggageHandler<? extends $Type> $HandlerName = $counterHandler;"""
    }

    class UserDefinedBagFieldToCompile(decl: FieldDeclaration, userfield: UserDefinedType) extends FieldToCompile(decl) {
      val HandlerName = s"_${Name}Handler"
      val ParserName = HandlerName
      val SerializerName = HandlerName
      val BrancherName = HandlerName
      val JoinerName = HandlerName

      val privateFieldsDeclaration: String = s"""
          private static final $BaggageHandler<$Type> $HandlerName = ${handler(userfield)};"""
    }

    class UserDefinedStructFieldToCompile(decl: FieldDeclaration, userfield: UserDefinedType) extends FieldToCompile(decl) {
      val HandlerName = s"_${Name}Handler"
      val ParserName = HandlerName
      val SerializerName = HandlerName
      val BrancherName = s"_${Name}Brancher"
      val JoinerName = s"_${Name}Joiner"

      val privateFieldsDeclaration: String = s"""
          private static final $StructHandler<$Type> $HandlerName = ${handler(userfield)};
          private static final $Brancher<$Type> $BrancherName = ${brancher(decl.fieldtype)};
          private static final $Joiner<$Type> $JoinerName = ${joiner(decl.fieldtype)};"""
    }

    class BagToCompile(decl: BagDeclaration) {

      val Name: String = javaName(decl.name)
      val PackageName: String = decl.packageName
      val varName: String = Name.head.toLower + Name.tail

      val fields = decl.fields.sortWith(_.index < _.index).map {
        x =>
          x match {
            case FieldDeclaration(BuiltInType.Counter, _, _) => new CounterToCompile(x)
            case FieldDeclaration(fieldtype: UserDefinedType, _, _) => {
              fieldtype.structType match {
                case true => new UserDefinedStructFieldToCompile(x, fieldtype)
                case false => new UserDefinedBagFieldToCompile(x, fieldtype)
              }
            }
            case _ => new BuiltInFieldToCompile(x)
          }
      }

      val body = s"""
        public class $Name implements $Bag {

            private static final $Logger _log = $LoggerFactory.getLogger($Name.class);
    
            ${fields.map(_.fieldDeclaration).mkString("\n")}
        
            public boolean _overflow = false;

            /**
             * <p>
             * Get the {@link $Name} set in the active {@link $Baggage} carried by the current thread. If no baggage is being
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
                $Bag bag = $BDLContextProvider.get($ActiveBaggage.peek(), Handler.registration());
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
                $Bag bag = $BDLContextProvider.get(baggage, Handler.registration());
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
                $ActiveBaggage.update($BDLContextProvider.set($ActiveBaggage.peek(), Handler.registration(), $varName));
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
                return $BDLContextProvider.set(baggage, Handler.registration(), $varName);
            }

            @Override
            public $BaggageHandler<?> handler() {
                return Handler.instance;
            }

            @Override
            public String toString() {
                StringBuilder b = new StringBuilder();
                b.append("$Name{\\n");
                ${fields.map(x => s"b.append(${x.toString("this")});").mkString("\n")}
                b.append("}");
                return b.toString();
            }
            
            public static class Handler implements $BaggageHandler<$Name> {

                public static final Handler instance = new Handler();
                private static $BagKey registration = null;

                static synchronized $BagKey checkRegistration() {
                    registration = $BaggageHandlerRegistry.get(instance);
                    if (registration == null) {
                        _log.error("$Name MUST be registered to a key before it can be propagated.  " +
                                   "There is currently no registration for $Name and it will not be propagated. " +
                                   "To register a bag set the bag.{index} property in your application.conf (eg, for " +
                                   "index 10, bag.10 = \\\"${decl.packageName}.$Name\\\") or with -Dbag.{index} flag " +
                                   "(eg, for index 10, -Dbag.10=${decl.packageName}.$Name)");
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

      val declaration = s"""/** Generated by BaggageBuffersCompiler */
        package ${decl.packageName};

        ${toImport.sortWith(_ < _).map(x => s"import $x;").mkString("\n")}
        
        ${body}"""

    }

  }

}