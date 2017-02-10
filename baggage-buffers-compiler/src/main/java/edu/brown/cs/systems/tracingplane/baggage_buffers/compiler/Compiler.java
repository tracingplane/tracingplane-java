package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler;

import java.util.Collection;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.ObjectDeclaration;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.BaggageBuffersDeclaration;

public interface Compiler {
    
    public default void compile(String outputDir, Collection<BaggageBuffersDeclaration> bbDecls) {
        bbDecls.forEach(decl -> compile(outputDir, decl));
    }
    
    public default void compile(String outputDir, BaggageBuffersDeclaration bbDecl) {
        bbDecl.getObjectDeclarations().forEach(objectDecl -> compile(outputDir, objectDecl));
    }
    
    public void compile(String outputDir, ObjectDeclaration objectDecl);

}
