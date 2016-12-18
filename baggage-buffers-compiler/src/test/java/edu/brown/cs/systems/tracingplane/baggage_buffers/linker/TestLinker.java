package edu.brown.cs.systems.tracingplane.baggage_buffers.linker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.CompileException;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Linker;

public class TestLinker {
    
    private int nextFileId = 0;
    
    final File createFile() throws IOException {
        File f = File.createTempFile("TestLinker" + nextFileId++, "bb");
        f.deleteOnExit();
        return f;
    }
    
    final String create(String contents, Object... formatArgs) throws IOException {
        File f = createFile();
        write(f, contents, formatArgs);
        return f.getAbsolutePath();
    }
    
    final void write(File f, String contents, Object... formatArgs) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(f);
        out.write(String.format(contents, formatArgs));
        out.close();
    }
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void testEmptyFile() throws IOException, CompileException {
        String fileName = create("");
        List<String> inputFiles = Lists.newArrayList(fileName);
        List<String> bagPath = Lists.newArrayList();

        exception.expect(CompileException.class);
        Linker.process(inputFiles, bagPath);
    }
    
    @Test
    public void testSimpleSuccessfulExample() throws IOException, CompileException {
        String fileName = create("bag MyBag {bool test = 0;}");
        List<String> inputFiles = Lists.newArrayList(fileName);
        List<String> bagPath = Lists.newArrayList();

        Linker.process(inputFiles, bagPath);
    }
    
    @Test
    public void testTwoFiles() throws CompileException, IOException {
        String first = create("bag MyBag1 {bool test = 0;}");
        String second = create("bag MyBag2 {bool test = 0;}");
        List<String> inputFiles = Lists.newArrayList(first, second);
        List<String> bagPath = Lists.newArrayList();

        Linker.process(inputFiles, bagPath);
    }
    
    @Test
    public void testTwoFilesAbsoluteImportFailure() throws CompileException, IOException {
        String first = create("bag MyBag1 {bool test = 0;}");
        String second = create("import \"%s\";\nbag MyBag2 {bool test = 0;}", first);
        List<String> inputFiles = Lists.newArrayList(first, second);
        List<String> bagPath = Lists.newArrayList();

        exception.expect(CompileException.class);
        Linker.process(inputFiles, bagPath);
    }
    
    @Test
    public void testTwoFilesRelativeImportSuccess() throws CompileException, IOException {
        File f1 = createFile();
        File f2 = createFile();
        
        write(f1, "bag MyBag1 {bool test = 0;}");
        write(f2, "import \"%s\";\nbag MyBag2 {bool test = 0;}", f1.getName());

        List<String> inputFiles = Lists.newArrayList(f1.getAbsolutePath(), f2.getAbsolutePath());
        List<String> bagPath = Lists.newArrayList();

        Linker.process(inputFiles, bagPath);
    }
    
    @Test
    public void testTwoFilesRecursiveImport() throws CompileException, IOException {
        File f1 = createFile();
        File f2 = createFile();
        
        write(f1, "import \"%s\";\nbag MyBag1 {bool test = 0;}", f2.getName());
        write(f2, "import \"%s\";\nbag MyBag2 {bool test = 0;}", f1.getName());

        List<String> inputFiles = Lists.newArrayList(f1.getAbsolutePath(), f2.getAbsolutePath());
        List<String> bagPath = Lists.newArrayList();

        //exception.expect(CompileException.class); // recursive import for now allowed
        Linker.process(inputFiles, bagPath);
    }
    
    @Test
    public void testTwoFilesBagPathImport() throws CompileException, IOException {
        File f1 = createFile();
        File f2 = createFile();
        
        write(f1, "bag MyBag1 {bool test = 0;}");
        write(f2, "import \"%s\";\nbag MyBag2 {bool test = 0;}", f1.getName());

        List<String> inputFiles = Lists.newArrayList(f2.getAbsolutePath());
        List<String> bagPath = Lists.newArrayList();

        Linker.process(inputFiles, bagPath);
    }
    
    @Test
    public void testTwoFilesBagPathImportFail() throws CompileException, IOException {
        
        File dir = Files.createTempDir();
        dir.deleteOnExit();
        
        File f1 = new File(dir, "bagpathimport");
        File f2 = createFile();
        
        write(f1, "bag MyBag1 {bool test = 0;}");
        write(f2, "import \"%s\";\nbag MyBag2 {bool test = 0;}", f1.getName());

        List<String> inputFiles = Lists.newArrayList(f2.getAbsolutePath());
        List<String> bagPath = Lists.newArrayList();

        exception.expect(CompileException.class); // f1 not found on bagpath
        Linker.process(inputFiles, bagPath);
    }
    
    @Test
    public void testTwoFilesBagPathImportSuccess() throws CompileException, IOException {
        
        File dir = Files.createTempDir();
        dir.deleteOnExit();
        
        File f1 = new File(dir, "bagpathimport");
        File f2 = createFile();
        
        write(f1, "bag MyBag1 {bool test = 0;}");
        write(f2, "import \"%s\";\nbag MyBag2 {bool test = 0;}", f1.getName());

        List<String> inputFiles = Lists.newArrayList(f2.getAbsolutePath());
        List<String> bagPath = Lists.newArrayList(dir.getAbsolutePath());

        Linker.process(inputFiles, bagPath);
    }
    
    @Test
    public void testUserDefinedSameFile() throws CompileException, IOException {
        File f = createFile();
        write(f, "bag MyBag1 {bool test = 0;}\nbag MyBag2 {MyBag1 test = 3;}");

        List<String> inputFiles = Lists.newArrayList(f.getAbsolutePath());
        List<String> bagPath = Lists.newArrayList();

        Linker.process(inputFiles, bagPath);
    }
    
    @Test
    public void testUserDefinedDifferentFileBothInputs() throws CompileException, IOException {
        File f1 = createFile();
        File f2 = createFile();
        
        write(f1, "bag MyBag1 {bool test = 0;}");
        write(f2, "import \"%s\";\nbag MyBag2 {MyBag1 test = 0;}", f1.getName());

        List<String> inputFiles = Lists.newArrayList(f1.getAbsolutePath(), f2.getAbsolutePath());
        List<String> bagPath = Lists.newArrayList();

        Linker.process(inputFiles, bagPath);
    }
    
    @Test
    public void testUserDefinedImport() throws CompileException, IOException {
        File dir = Files.createTempDir();
        dir.deleteOnExit();
        
        File f1 = new File(dir, "bagpathimport");
        File f2 = createFile();
        
        write(f1, "bag MyBag1 {bool test = 0;}");
        write(f2, "import \"%s\";\nbag MyBag2 {MyBag1 test = 0;}", f1.getName());

        List<String> inputFiles = Lists.newArrayList(f2.getAbsolutePath());
        List<String> bagPath = Lists.newArrayList(dir.getAbsolutePath());

        Linker.process(inputFiles, bagPath);
    }
    
    @Test
    public void testUserDefinedDoesNotExist() throws CompileException, IOException {
        File f1 = createFile();
        File f2 = createFile();
        
        write(f1, "bag MyBag1 {bool test = 0;}");
        write(f2, "import \"%s\";\nbag MyBag2 {MyBag3 test = 0;}", f1.getName());

        List<String> inputFiles = Lists.newArrayList(f1.getAbsolutePath(), f2.getAbsolutePath());
        List<String> bagPath = Lists.newArrayList();

        exception.expect(CompileException.class); // no such bag MyBag3
        Linker.process(inputFiles, bagPath);
    }

}
