//package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.util.Set;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import com.google.common.collect.HashBasedTable;
//import com.google.common.collect.Table;
//import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.BagDeclaration;
//import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.BaggageBuffersDeclaration;
//import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.PackageDeclaration;
//
//public class Compiler {
//
//    static final Logger log = LoggerFactory.getLogger(Compiler.class);
//
//    /** Log a warning; though override to throw a compile exception */
//    static void warn(String message, Object... messageArgs) throws CompileException {
//        log.warn(String.format(message, messageArgs));
//    }
//
//    final String fileName;
//    final Set<String> otherInputFiles;
//    final Set<String> bagPath;
//
//    public Compiler(String fileName, Set<String> otherInputFiles, Set<String> bagPath) {
//        this.fileName = fileName;
//        this.otherInputFiles = otherInputFiles;
//        this.bagPath = bagPath;
//    }
//
//    /**
//     * Find the specified file on the bagPath
//     * 
//     * @param fileName the name of the file to search for
//     * @return a {@link File} object for this fileName, or null if the import cannot be found on the bagPath
//     */
//    File findImport(String fileName) {
//        for (String importDir : bagPath) {
//            File f = new File(importDir, fileName);
//            if (f.exists() && !f.isDirectory() && f.canRead()) {
//                return f;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Read the entire contents of the file as a String
//     * 
//     * @param file the file to read
//     * @return the contents of the file
//     * @throws IOException if the file cannot be read
//     */
//    String readFully(File file) throws IOException {
//        FileInputStream fis = new FileInputStream(file);
//        byte[] data = new byte[(int) file.length()];
//        fis.read(data);
//        fis.close();
//        return new String(data, Charset.defaultCharset());
//    }
//
//    /**
//     * Parse the provided text as a BaggageBuffers file
//     * 
//     * @param text the text to parse
//     * @return a BaggageBuffersDeclaration AST if valid
//     * @throws Exception if parsing failed
//     */
//    BaggageBuffersDeclaration parse(String text) throws Exception {
//        return Declarations.parseBaggageBuffersFile(text);
//    }
//
//    class SingleFileCompiler {
//
//        final String fileName;
//        final Table<String, String, BagReference> allKnownBags = HashBasedTable.create();
//
//        SingleFileCompiler(String fileName) {
//            this.fileName = fileName;
//        }
//
//        /**
//         * Find the specified fileName on the bagPath, parse the file (though do not validate it), and add its bag
//         * imports to {@link allBags}
//         * 
//         * @param fileName the name of the file to parse and import from
//         * @throws CompileException if the specified import could not be loaded or parsed
//         */
//        void importBags(File currentFile, String importName) throws CompileException {
//            File importFile = findImport(importName);
//            if (importFile == null) {
//                throw CompileException.importNotFound(importName, currentFile, StringUtils.join(bagPath, ";"));
//            }
//
//            final String fileContents;
//            try {
//                fileContents = readFully(importFile);
//            } catch (IOException e) {
//                throw CompileException.importNotReadable(importFile, currentFile, importName, e);
//            }
//
//            final BaggageBuffersDeclaration bbDecl;
//            try {
//                bbDecl = parse(fileContents);
//            } catch (Exception e) {
//                throw CompileException.syntaxError(importFile, currentFile, importName, e);
//            }
//
//            PackageDeclaration packageDecl = bbDecl.getPackageDeclaration();
//            for (BagDeclaration bagDecl : bbDecl.getBagDeclarations()) {
//                String packageName = packageDecl.getPackageNameString();
//                String bagName = bagDecl.name();
//                String fullyQualifiedName = bagDecl.fullyQualifiedName(packageDecl);
//
//                if (allKnownBags.contains(packageName, bagName)) {
//                    BagReference existing = allKnownBags.get(packageName, bagName);
//                    warn("Duplicate declaration of %s in %s and %s", fullyQualifiedName, importFile,
//                         existing.declaredIn);
//                } else {
//                    allKnownBags.put(packageName, bagName,
//                                     new BagReference(packageName, bagName, fullyQualifiedName, importFile));
//                }
//            }
//        }
//
//    }
//    
//    /** Any type permitted as a field */
//    static interface FieldType {
//        
//    }
//
//    /** A reference to a bag */
//    static class BagReference implements FieldType {
//        final String packageName;
//        final String bagName;
//        final String fullyQualifiedBagName;
//        final File declaredIn;
//
//        public BagReference(String packageName, String bagName, String fullyQualifiedName, File declaredIn) {
//            this.packageName = packageName;
//            this.bagName = bagName;
//            this.fullyQualifiedBagName = fullyQualifiedName;
//            this.declaredIn = declaredIn;
//        }
//
//        @Override
//        public boolean equals(Object other) {
//            if (other == null || !(other instanceof BagReference)) {
//                return false;
//            } else {
//                return fullyQualifiedBagName.equals(((BagReference) other).fullyQualifiedBagName);
//            }
//        }
//
//        @Override
//        public int hashCode() {
//            return fullyQualifiedBagName.hashCode();
//        }
//
//    }
//
//}
