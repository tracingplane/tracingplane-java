package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.*;

public class CompileException extends Exception {

    private static final long serialVersionUID = 1L;

    public CompileException(String message, Object... formatArgs) {
        super(String.format(message, formatArgs));
    }

    public CompileException(String message, Exception cause) {
        this(cause, message);
    }
    
    public CompileException(Exception cause, String message, Object... formatArgs) {
        super(String.format(message, formatArgs), cause);
    }
    
    public static CompileException sourceFileNotFound(String fileName) {
        return new CompileException("Source file %s could not be found", fileName);
    }
    
    public static CompileException sourceFileNotReadable(String fileName, File file, IOException e) {
        return new CompileException(e, "Source file %s (%s) unreadable: %s", fileName, file, e.getMessage());
    }
    
    public static CompileException importNotFound(String importName, File declaredIn, List<String> bagPath) {
        return new CompileException("%s: import %s could not be found on bagPath %s", declaredIn, importName, FileUtils.joinBagPath(bagPath));
    }
    
    public static CompileException importNotReadable(File importFile, File declaredIn, String importedAs, IOException e) {
        return new CompileException(e, "%s: import %s (%s) could not be read due to: %s", declaredIn, importedAs, importFile, e.getMessage());
    }
    
    public static CompileException sourceFileSyntaxError(String fileName, File file, Exception e) {
        return new CompileException(e, "Source file %s (%s) syntax error: %s", fileName, file, e.getMessage());
    }
    
    public static CompileException syntaxError(Exception e) {
        return new CompileException(e, "Syntax error: %s", e.getMessage());
    }
    
    public static CompileException importFileSyntaxError(File importFile, File declaredIn, String importedAs, Exception e) {
        return new CompileException(e, "%s: import %s (%s) syntax error: %s", declaredIn, importedAs, importFile, e.getMessage());
    }
    
    public static CompileException recursiveImport(File first, File second) {
        return new CompileException("%s: recursive import of %s", first, second);
    }
    
    public static CompileException noFieldsDeclared(File inputFile, String bagName) {
        return new CompileException("%s: bag %s declares no fields", inputFile, bagName);
    }
    
    public static CompileException duplicateDeclaration(File inputFile, String bagName) {
        return new CompileException("%s: duplicate declaration of bag %s", inputFile, bagName);
    }
    
    public static CompileException duplicateFieldDeclaration(File inputFile, String bagName, String fieldName, FieldDeclaration... fields) {
        return new CompileException("%s: %s declares field named %s multiple times (%s)", inputFile, bagName, fieldName, StringUtils.join(fields, ", "));
    }
    
    public static CompileException duplicateFieldDeclaration(File inputFile, String bagName, int fieldIndex, FieldDeclaration... fields) {
        return new CompileException("%s: %s declares field index %d multiple times (%s)", inputFile, bagName, fieldIndex, StringUtils.join(fields, ", "));
    }
    
    public static CompileException unknownType(File inputFile, String bagName, FieldDeclaration declaration, UserDefinedType userDefined) {
        return new CompileException("%s: %s declares field with unknown type %s (%s)", inputFile, bagName, userDefined, declaration);
    }

}
