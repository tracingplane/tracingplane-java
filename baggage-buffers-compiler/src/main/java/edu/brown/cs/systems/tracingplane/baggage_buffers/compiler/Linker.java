package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.BaggageBuffersDeclaration;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.ImportDeclaration;

public class Linker {

    static final Logger log = LoggerFactory.getLogger(Linker.class);

    List<String> bagPath;
    Map<File, SourceFile> sourceFiles = new HashMap<>();
    Map<File, BaggageBuffersFile> allFiles = new HashMap<>();

    public Linker(List<String> sourceFileNames, List<String> bagPath) throws CompileException {
        this.bagPath = bagPath;

        // Check and load the source files. Throws exceptions if they can't be loaded.
        for (String sourceFileName : sourceFileNames) {
            SourceFile sourceFile = loadSourceFile(sourceFileName);
            sourceFiles.put(sourceFile.file, sourceFile);
            allFiles.put(sourceFile.file, sourceFile);
        }

        // Resolve the immediate imports of the source files, but don't go any further
        for (SourceFile sourceFile : sourceFiles.values()) {
            sourceFile.resolveImports();
        }
        
        // Validate the imports
        for (SourceFile sourceFile : sourceFiles.values()) {
            sourceFile.validateImports();
        }
    }

    private SourceFile loadSourceFile(String fileName) throws CompileException {
        File file = FileUtils.getFile(fileName);
        if (file == null) {
            throw CompileException.sourceFileNotFound(fileName);
        }
        String fileContents;
        try {
            fileContents = FileUtils.readFully(file);
        } catch (IOException e) {
            throw CompileException.sourceFileNotReadable(fileName, file, e);
        }
        BaggageBuffersDeclaration decl;
        try {
            decl = Parser.parseBaggageBuffersFile(fileContents);
        } catch (Exception e) {
            throw CompileException.sourceFileSyntaxError(fileName, file, e);
        }

        return new SourceFile(fileName, file, fileContents, decl);
    }

    public class BaggageBuffersFile {

        public final File file;
        public final String fileContents;
        public final BaggageBuffersDeclaration decl;
        public final Multimap<BaggageBuffersFile, String> imports;
        public final Set<BaggageBuffersFile> importedBy;

        protected BaggageBuffersFile(File file, String fileContents, BaggageBuffersDeclaration decl) {
            this.file = file;
            this.fileContents = fileContents;
            this.decl = decl;
            this.imports = HashMultimap.create();
            this.importedBy = new HashSet<>();
        }

        public void resolveImports() throws CompileException {
            imports.clear();

            for (ImportDeclaration importDecl : decl.getImportDeclarations()) {
                String importFileName = importDecl.filename();
                File importFile = FileUtils.findFile(importFileName, bagPath);
                if (importFile == null) {
                    throw CompileException.importNotFound(importFileName, this.file, bagPath);
                }

                final BaggageBuffersFile imported;
                if (allFiles.containsKey(importFile)) {
                    imported = allFiles.get(importFile);
                } else {
                    imported = loadImport(importFile, importFileName);
                    allFiles.put(importFile, imported);
                }

                if (imports.containsKey(importFile)) {
                    log.warn("{}: duplicate import of {} (imported as {}, {})", file, importFile, importFileName,
                             StringUtils.join(imports.get(imported), ", "));
                }
                imports.put(imported, importFileName);
                imported.importedBy.add(this);
            }
        }

        public void validateImports() throws CompileException {
            this.validateImports(new Stack<>());
        }

        private void validateImports(Stack<BaggageBuffersFile> pathToHere) throws CompileException {
            pathToHere.push(this);
            for (BaggageBuffersFile importedFile : imports.keySet()) {
                if (pathToHere.contains(importedFile)) {
                    throw CompileException.recursiveImport(this.file, importedFile.file,
                                                           StringUtils.join(imports.get(importedFile), ", "));
                } else {
                    importedFile.validateImports(pathToHere);
                }
            }
            pathToHere.pop();
        }

        public ImportFile loadImport(File importFile, String importedAs) throws CompileException {
            String importFileContents;
            try {
                importFileContents = FileUtils.readFully(importFile);
            } catch (IOException e) {
                throw CompileException.importNotReadable(importFile, this.file, importedAs, e);
            }
            BaggageBuffersDeclaration importDecl;
            try {
                importDecl = Parser.parseBaggageBuffersFile(importFileContents);
            } catch (Exception e) {
                throw CompileException.importFileSyntaxError(importFile, this.file, importedAs, e);
            }
            return new ImportFile(importFile, importFileContents, importDecl);
        }

        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof BaggageBuffersFile)) {
                return false;
            } else {
                return file.equals(((BaggageBuffersFile) other).file);
            }
        }

    }

    public class SourceFile extends BaggageBuffersFile {

        public final String fileName;

        protected SourceFile(String fileName, File file, String fileContents, BaggageBuffersDeclaration decl) {
            super(file, fileContents, decl);
            this.fileName = fileName;
        }

    }

    public class ImportFile extends BaggageBuffersFile {

        protected ImportFile(File file, String fileContents, BaggageBuffersDeclaration decl) {
            super(file, fileContents, decl);
        }

    }

}
