package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Declarations._
import fastparse.all._
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.FieldType.BuiltInType
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.FieldType.UserDefined.UserDefinedType
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast._

@RunWith(classOf[JUnitRunner])
class TestDeclarations extends FunSuite {

  val builtins = Map("bool" -> BuiltInType.bool,
    "int32" -> BuiltInType.int32,
    "int64" -> BuiltInType.int64,
    "sint32" -> BuiltInType.sint32,
    "sint64" -> BuiltInType.sint64,
    "fixed32" -> BuiltInType.fixed32,
    "fixed64" -> BuiltInType.fixed64,
    "sfixed32" -> BuiltInType.sfixed32,
    "sfixed64" -> BuiltInType.sfixed64,
    "float" -> BuiltInType.float,
    "double" -> BuiltInType.double,
    "string" -> BuiltInType.string,
    "bytes" -> BuiltInType.bytes)

  val userdefineds = List("mytype", "int33", "jontype")

  test("Test builtin primitives") {
    for ((name, builtin) <- builtins) {
      val Parsed.Success(res, _) = fieldtype.parse(name)
      assert(res == builtin)
    }
  }

  test("Test set") {
    for ((name, builtin) <- builtins) {
      val parseString = s"set<$name>"
      val Parsed.Success(res, _) = fieldtype.parse(parseString)
      res match {
        case BuiltInType.Set(of) => assert(of == builtin)
        case _ => fail(s"Expected to parse $parseString to a set of $name")
      }
    }
  }

  test("Test user defined type") {
    for (name <- userdefineds) {
      val Parsed.Success(res, _) = fieldtype.parse(name)
      assert(res.isInstanceOf[UserDefinedType])
    }
  }

  test("Test builtin declarations") {
    var Parsed.Success(res, _) = fieldDeclaration.parse("bool test = 1;")
    assert(res.instanceof == BuiltInType.bool)
    assert(res.name == "test")
    assert(res.index == 1)

    var Parsed.Success(res2, _) = fieldDeclaration.parse("int32 test3 = 2;")
    assert(res2.instanceof == BuiltInType.int32)
    assert(res2.name == "test3")
    assert(res2.index == 2)
  }

  test("Test bag declaration with one field") {
    val declaration = """bag MyBag {
        bool test = 1;
    }"""

    var Parsed.Success(res, _) = bagDeclaration.parse(declaration)

    assert(res.name == "MyBag")
    assert(res.fields.length == 1)
    assert(res.fields(0).isInstanceOf[FieldDeclaration])
  }

  test("Test bag declaration with multiple fields") {
    val declaration = """bag MyBag {
        bool test = 1;
        blahblah jon = 2;
        int32 third = 3;
    }"""

    var Parsed.Success(res, _) = bagDeclaration.parse(declaration)

    assert(res.name == "MyBag")
    assert(res.fields.length == 3)
    assert(res.fields(0).isInstanceOf[FieldDeclaration])
    assert(res.fields(0).instanceof == BuiltInType.bool)
    assert(res.fields(0).name == "test")
    assert(res.fields(0).index == 1)
    assert(res.fields(1).isInstanceOf[FieldDeclaration])
    assert(res.fields(1).instanceof.isInstanceOf[UserDefinedType])
    assert(res.fields(1).name == "jon")
    assert(res.fields(1).index == 2)
    assert(res.fields(2).isInstanceOf[FieldDeclaration])
    assert(res.fields(2).instanceof == BuiltInType.int32)
    assert(res.fields(2).name == "third")
    assert(res.fields(2).index == 3)
  }

  test("Must have a type") {
    var Parsed.Failure(expected, failIndex, extra) = fieldDeclaration.parse("test = 1;")
  }

  test("Must have a semicolon") {
    var Parsed.Failure(expected, failIndex, extra) = fieldDeclaration.parse("bool test = 1")
  }

  test("Can have lots of whitespace") {
    var Parsed.Success(res2, _) = fieldDeclaration.parse("int32\t\t  \t  test3   \t   = \t\t   2 \t\t  ; \t\t  ")
    assert(res2.instanceof == BuiltInType.int32)
    assert(res2.name == "test3")
    assert(res2.index == 2)
  }

  test("Can have little whitespace") {
    var Parsed.Success(res2, _) = fieldDeclaration.parse("int32 test3=2;")
    assert(res2.instanceof == BuiltInType.int32)
    assert(res2.name == "test3")
    assert(res2.index == 2)
  }

  test("Valid package name 1") {
    var Parsed.Success(res, _) = packagename.parse("edu.brown.systems")
    assert(res.length == 3)
    assert(res(0) == "edu")
    assert(res(1) == "brown")
    assert(res(2) == "systems")
  }

  test("Valid package name 2") {
    var Parsed.Success(res, _) = packagename.parse("edu")
    assert(res.length == 1)
    assert(res(0) == "edu")
  }

  test("Valid Short Package Declaration") {
    var Parsed.Success(res, _) = packageDeclaration.parse("package \"mypackage\";")
    assert(res.isInstanceOf[PackageDeclaration])
    assert(res.packageName.length == 1)
    assert(res.packageName(0) == "mypackage")
  }

  test("Valid Package Declaration Spacing") {
    packageDeclaration.parse("package \t     \"mypackage\"   \t\t ;") match {
      case failure: Parsed.Failure => fail(ParseError(failure).getMessage)
      case Parsed.Success(res, _) => {
        assert(res.isInstanceOf[PackageDeclaration])
        assert(res.packageName.length == 1)
        assert(res.packageName(0) == "mypackage")
      }
    }
  }

  test("Valid Package Declaration") {
    var Parsed.Success(res, _) = packageDeclaration.parse("""package "edu.brown.cs";""")
    assert(res.isInstanceOf[PackageDeclaration])
    assert(res.packageName.length == 3)
    assert(res.packageName(0) == "edu")
    assert(res.packageName(1) == "brown")
    assert(res.packageName(2) == "cs")
  }

  test("Invalid Package Characters") {
    var Parsed.Failure(expected, failIndex, extra) = packageDeclaration.parse("""package "e!@#$du.brown.cs";""")
    assert(failIndex == 10)
  }

  test("Invalid Empty Package") {
    var Parsed.Failure(expected, failIndex, extra) = packageDeclaration.parse("package \"\";")
    assert(failIndex == 9)
  }

  test("Import package") {
    importDeclaration.parse("import \"myfile.something\";") match {
      case failure: Parsed.Failure => fail(ParseError(failure).getMessage)
      case Parsed.Success(res, _) => {
        assert(res.isInstanceOf[ImportDeclaration])
        assert(res.filename == "myfile.something")
      }
    }
  }

  test("Simple End Of Line Comment") {
    val declaration = """bag MyBag {
        bool test = 1; // my comment
        int32 third = 3;
    }"""

    bagDeclaration.parse(declaration) match {
      case failure: Parsed.Failure => fail(ParseError(failure).getMessage)
      case Parsed.Success(res, _) => {
        assert(res.name == "MyBag")
        assert(res.fields.length == 2)
        assert(res.fields(0).isInstanceOf[FieldDeclaration])
        assert(res.fields(0).instanceof == BuiltInType.bool)
        assert(res.fields(0).name == "test")
        assert(res.fields(0).index == 1)
        assert(res.fields(1).isInstanceOf[FieldDeclaration])
        assert(res.fields(1).instanceof == BuiltInType.int32)
        assert(res.fields(1).name == "third")
        assert(res.fields(1).index == 3)
      }
    }
  }

  test("Test Multiline Comment") {
    val comment = """/*
      blah
      blah
      blaaaaah
      
      
      */"""
    
    comment2.parse(comment) match {
      case failure: Parsed.Failure => fail(ParseError(failure).getMessage)
      case Parsed.Success(res, _) => {
        println("success")
      }
    }
  }

  test("Simple Inline Comment") {
    val declaration = """bag MyBag {
        bool test = 1;
        /* This is my third field */ int32 third = 3;
    }"""

    bagDeclaration.parse(declaration) match {
      case failure: Parsed.Failure => fail(ParseError(failure).getMessage)
      case Parsed.Success(res, _) => {
        assert(res.name == "MyBag")
        assert(res.fields.length == 2)
        assert(res.fields(0).isInstanceOf[FieldDeclaration])
        assert(res.fields(0).instanceof == BuiltInType.bool)
        assert(res.fields(0).name == "test")
        assert(res.fields(0).index == 1)
        assert(res.fields(1).isInstanceOf[FieldDeclaration])
        assert(res.fields(1).instanceof == BuiltInType.int32)
        assert(res.fields(1).name == "third")
        assert(res.fields(1).index == 3)
      }
    }
  }

  test("End of line comment") {
    val declaration = """bag MyBag { // this is my bag
 /* cool bag huh?
bool notafield = 0
*/
        bool test = 1; // another comment
        // blahblah jon = 2;
        /* brief comment here... */ int32 third = 3;
    }"""

    bagDeclaration.parse(declaration) match {
      case failure: Parsed.Failure => fail(ParseError(failure).getMessage)
      case Parsed.Success(res, _) => {
        assert(res.name == "MyBag")
        assert(res.fields.length == 2)
        assert(res.fields(0).isInstanceOf[FieldDeclaration])
        assert(res.fields(0).instanceof == BuiltInType.bool)
        assert(res.fields(0).name == "test")
        assert(res.fields(0).index == 1)
        assert(res.fields(1).isInstanceOf[FieldDeclaration])
        assert(res.fields(1).instanceof == BuiltInType.int32)
        assert(res.fields(1).name == "third")
        assert(res.fields(1).index == 3)
      }
    }
  }

}
