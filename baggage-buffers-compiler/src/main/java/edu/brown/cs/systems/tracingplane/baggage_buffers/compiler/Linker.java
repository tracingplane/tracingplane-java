package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.BagDeclaration;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.BaggageBuffersDeclaration;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.BuiltInType;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.FieldDeclaration;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.FieldType;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.ImportDeclaration;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.ObjectDeclaration;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.PackageDeclaration;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.ParameterizedType;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.PrimitiveType;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.StructDeclaration;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.StructFieldDeclaration;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.UserDefinedType;
import fastparse.core.ParseError;

public class Linker {

    public final List<String> bagPath;
    public final Map<File, BaggageBuffersDeclaration> loadedFiles = new HashMap<>();

    private Linker(List<String> bagPath) {
        this.bagPath = bagPath;
    }

    public static Set<BaggageBuffersDeclaration> link(BBC.Settings settings) throws CompileException {
        return link(settings.files, FileUtils.splitBagPath(settings.bagPath));
    }

    public static Set<BaggageBuffersDeclaration> link(List<String> inputFiles,
                                                      List<String> bagPath) throws CompileException {
        Linker linker = new Linker(bagPath);
        Map<File, BaggageBuffersDeclaration> processed = linker.process(inputFiles);
        return new HashSet<>(processed.values());
    }

    public static BaggageBuffersDeclaration link(String inputFileContents,
                                                 List<String> bagPath) throws CompileException {
        try {
            BaggageBuffersDeclaration decl = Parser.parseBaggageBuffersFile(inputFileContents);
            new Linker(new ArrayList<String>()).process(decl);
            return decl;
        } catch (ParseError<?, ?> e) {
            throw CompileException.syntaxError(e);
        }
    }

    private void process(BaggageBuffersDeclaration decl) throws CompileException {
        fillPackageNames(decl);
        checkForDuplicateObjectDeclarations(null, decl);
        checkForDuplicateFieldDeclarations(null, decl);
        resolveFieldPackageNames(null, decl);
    }

    private Map<File, BaggageBuffersDeclaration> process(List<String> inputFileNames) throws CompileException {
        Map<File, BaggageBuffersDeclaration> inputs = loadInputFiles(inputFileNames);
        loadedFiles.putAll(inputs);

        for (File inputFile : inputs.keySet()) {
            BaggageBuffersDeclaration decl = inputs.get(inputFile);

            checkForDuplicateObjectDeclarations(inputFile, decl);
            checkForDuplicateFieldDeclarations(inputFile, decl);
            resolveFieldPackageNames(inputFile, decl);
        }

        return inputs;
    }

    public static void checkForDuplicateObjectDeclarations(File inputFile,
                                                           BaggageBuffersDeclaration bbDecl) throws CompileException {
        Set<String> objectsSeen = new HashSet<>();
        for (ObjectDeclaration objectDecl : bbDecl.getObjectDeclarations()) {
            String objectName = objectDecl.name();
            if (objectsSeen.contains(objectName)) {
                throw CompileException.duplicateDeclaration(inputFile, objectName);
            }
            objectsSeen.add(objectName);
        }
    }

    public static void checkForDuplicateFieldDeclarations(File inputFile,
                                                             BaggageBuffersDeclaration bbDecl) throws CompileException {
        for (BagDeclaration bagDecl : bbDecl.getBagDeclarations()) {
            checkBagForDuplicateFieldDeclarations(inputFile, bagDecl);
        }
        for (StructDeclaration structDecl : bbDecl.getStructDeclarations()) {
            checkStructForDuplicateFieldDeclarations(inputFile, structDecl);
        }
    }
    
    public static void checkBagForDuplicateFieldDeclarations(File inputFile, BagDeclaration bagDecl) throws CompileException {
        Map<String, FieldDeclaration> names = new HashMap<>();
        Map<Integer, FieldDeclaration> indices = new HashMap<>();
        for (FieldDeclaration fieldDecl : bagDecl.getFieldDeclarations()) {
            if (names.containsKey(fieldDecl.name())) {
                throw CompileException.duplicateFieldDeclaration(inputFile, bagDecl.name(), fieldDecl.name(),
                                                                 names.get(fieldDecl.name()), fieldDecl);
            }
            if (indices.containsKey(fieldDecl.index())) {
                throw CompileException.duplicateFieldDeclaration(inputFile, bagDecl.name(), fieldDecl.index(),
                                                                 indices.get(fieldDecl.index()), fieldDecl);
            }
            names.put(fieldDecl.name(), fieldDecl);
            indices.put(fieldDecl.index(), fieldDecl);
        }
    }
    
    public static void checkStructForDuplicateFieldDeclarations(File inputFile, StructDeclaration structDecl) throws CompileException {
        Map<String, StructFieldDeclaration> names = new HashMap<>();
        for (StructFieldDeclaration fieldDecl : structDecl.getFieldDeclarations()) {
            if (names.containsKey(fieldDecl.name())) {
                throw CompileException.duplicateStructFieldDeclaration(inputFile, structDecl.name(), fieldDecl.name(),
                                                                 names.get(fieldDecl.name()), fieldDecl);
            }
            names.put(fieldDecl.name(), fieldDecl);
        }
    }

    public static void fillPackageNames(BaggageBuffersDeclaration bbDecl) {
        PackageDeclaration packageDecl = bbDecl.getPackageDeclaration();
        if (packageDecl != null) {
            String packageName = packageDecl.getPackageNameString();

            for (ObjectDeclaration objectDecl : bbDecl.getObjectDeclarations()) {
                objectDecl.packageName_$eq(packageName);
            }
        }
    }

    public void resolveFieldPackageNames(File inputFile, BaggageBuffersDeclaration bbDecl) throws CompileException {
        List<BaggageBuffersDeclaration> searchPath = new ArrayList<>();
        searchPath.add(bbDecl);
        for (ImportDeclaration idecl : Lists.reverse(bbDecl.getImportDeclarations())) {
            searchPath.add(getImport(inputFile, idecl.filename()));
        }

        for (BagDeclaration bagDecl : bbDecl.getBagDeclarations()) {
            for (FieldDeclaration fieldDecl : bagDecl.getFieldDeclarations()) {
                resolveField(inputFile, bagDecl.name(), fieldDecl, fieldDecl.fieldtype(), searchPath);
            }
        }

        for (StructDeclaration structDecl : bbDecl.getStructDeclarations()) {
            for (StructFieldDeclaration fieldDecl : structDecl.getFieldDeclarations()) {
                resolveStructField(inputFile, structDecl.name(), fieldDecl, fieldDecl.fieldtype(), searchPath);
            }
        }
    }

    public void resolveField(File inputFile, String bagName, FieldDeclaration fieldDecl, FieldType fieldType,
                             List<BaggageBuffersDeclaration> searchPath) throws CompileException {
        if (fieldType instanceof UserDefinedType) {
            UserDefinedType userDefined = ((UserDefinedType) fieldType);
            ObjectDeclaration declaration = findObject(userDefined.name(), userDefined.packageName(), searchPath);
            if (declaration == null) {
                throw CompileException.unknownType(inputFile, bagName, fieldDecl, userDefined);
            }
            if (declaration instanceof StructDeclaration) {
                userDefined.structType_$eq(true);
            }
            userDefined.packageName_$eq(declaration.packageName());

        } else if (fieldType instanceof ParameterizedType) {
            for (FieldType parameterType : ((ParameterizedType) fieldType).getParameters()) {
                resolveField(inputFile, bagName, fieldDecl, parameterType, searchPath);
            }
        } else if (fieldType instanceof BuiltInType) {
        } else {
            // Unexpected occurrence
            throw new RuntimeException("Encountered unexpected class for fieldType " + fieldType + ", " +
                                       fieldType.getClass().getName());
        }
        
        if (!fieldDecl.fieldtype().isValid()) {
            throw CompileException.fieldTypeNotValid(inputFile, bagName, fieldType);
        }
    }

    public void resolveStructField(File inputFile, String structName, StructFieldDeclaration fieldDecl,
                                   FieldType fieldType,
                                   List<BaggageBuffersDeclaration> searchPath) throws CompileException {
        if (fieldType instanceof UserDefinedType) {
            UserDefinedType userDefined = ((UserDefinedType) fieldType);
            StructDeclaration declaration = findStruct(userDefined.name(), userDefined.packageName(), searchPath);
            if (declaration == null) {
                throw CompileException.unknownType(inputFile, structName, fieldDecl, userDefined);
            }
            userDefined.structType_$eq(true);
            userDefined.packageName_$eq(declaration.packageName());

        } else if (fieldType instanceof PrimitiveType) {
            return;
        } else {
            throw CompileException.invalidStructFieldType(inputFile, structName, fieldDecl);
        }
    }

    public ObjectDeclaration findObject(String objectNameToFind, String packageNameToFind,
                                  List<BaggageBuffersDeclaration> toSearch) {
        boolean searchAllPackages = packageNameToFind == null || "".equals(packageNameToFind);
        for (BaggageBuffersDeclaration decl : toSearch) {
            if (searchAllPackages || packageNameToFind.equals(decl.getPackageNameString())) {
                for (ObjectDeclaration objectDecl : decl.getObjectDeclarations()) {
                    if (objectDecl.name().equals(objectNameToFind)) {
                        return objectDecl;
                    }
                }
            }
        }
        return null;
    }
    
    public StructDeclaration findStruct(String structNameToFind, String packageNameToFind,
                                        List<BaggageBuffersDeclaration> toSearch) {
        boolean searchAllPackages = packageNameToFind == null || "".equals(packageNameToFind);
        for (BaggageBuffersDeclaration decl : toSearch) {
            if (searchAllPackages || packageNameToFind.equals(decl.getPackageNameString())) {
                for (StructDeclaration structDecl : decl.getStructDeclarations()) {
                    if (structDecl.name().equals(structNameToFind)) {
                        return structDecl;
                    }
                }
            }
        }
        return null;
    }

    public static Map<File, BaggageBuffersDeclaration> loadInputFiles(List<String> fileNames) throws CompileException {
        Map<File, BaggageBuffersDeclaration> loadedInputFiles = new HashMap<>();

        for (String fileName : fileNames) {
            // Input files do not search the bag path
            File file = FileUtils.getFile(fileName);
            if (file == null) {
                throw CompileException.sourceFileNotFound(fileName);
            }

            // Only load once
            if (loadedInputFiles.containsKey(file)) {
                continue;
            }

            // Read and parse
            try {
                String fileContents = FileUtils.readFully(file);
                BaggageBuffersDeclaration decl = Parser.parseBaggageBuffersFile(fileContents);
                fillPackageNames(decl);
                loadedInputFiles.put(file, decl);
            } catch (IOException e) {
                throw CompileException.sourceFileNotReadable(fileName, file, e);
            } catch (ParseError<?, ?> e) {
                throw CompileException.sourceFileSyntaxError(fileName, file, e);
            }
        }

        return loadedInputFiles;
    }

    public BaggageBuffersDeclaration getImport(File importedBy, String importedAs) throws CompileException {
        List<String> pathToSearch = new ArrayList<>();
        pathToSearch.add(importedBy.getParent());
        pathToSearch.addAll(bagPath);
        File importFile = FileUtils.findFile(importedAs, pathToSearch);
        if (importFile == null) {
            throw CompileException.importNotFound(importedAs, importedBy, pathToSearch);
        }

        if (!loadedFiles.containsKey(importFile)) {
            try {
                BaggageBuffersDeclaration decl = Parser.parseBaggageBuffersFile(FileUtils.readFully(importFile));
                fillPackageNames(decl);
                loadedFiles.put(importFile, decl);
            } catch (IOException e) {
                throw CompileException.importNotReadable(importFile, importedBy, importedAs, e);
            } catch (ParseError<?, ?> e) {
                throw CompileException.importFileSyntaxError(importFile, importedBy, importedAs, e);
            }
        }

        return loadedFiles.get(importFile);
    }

}
