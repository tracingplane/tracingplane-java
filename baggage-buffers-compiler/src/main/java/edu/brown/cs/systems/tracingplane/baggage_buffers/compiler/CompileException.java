package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
    
    public static CompileException importFileSyntaxError(File importFile, File declaredIn, String importedAs, Exception e) {
        return new CompileException(e, "%s: import %s (%s) syntax error: %s", declaredIn, importedAs, importFile, e.getMessage());
    }
    
    public static CompileException recursiveImport(File first, File second, String secondImportedAs) {
        return new CompileException("%s: recursive import of %s (%s)", first, secondImportedAs, second);
    }

}
