package brown.tracingplane.bdl.compiler;

import java.util.Collection;
import brown.tracingplane.bdl.compiler.Ast.BaggageBuffersDeclaration;
import brown.tracingplane.bdl.compiler.Ast.ObjectDeclaration;

public interface Compiler {
    
    public default void compile(String outputDir, Collection<BaggageBuffersDeclaration> bbDecls) {
        bbDecls.forEach(decl -> compile(outputDir, decl));
    }
    
    public default void compile(String outputDir, BaggageBuffersDeclaration bbDecl) {
        bbDecl.getObjectDeclarations().forEach(objectDecl -> compile(outputDir, objectDecl));
    }
    
    public void compile(String outputDir, ObjectDeclaration objectDecl);

}
