package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.google.common.collect.Lists;

public class TestLinker {
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void testEmptyFile() throws IOException, CompileException {
        
        File f = File.createTempFile("empty", "bb");
        f.deleteOnExit();
        String fileName = f.getAbsolutePath();
        List<String> inputFiles = Lists.newArrayList(fileName);
        List<String> bagPath = Lists.newArrayList();
        

        exception.expect(CompileException.class);
        Linker linker = new Linker(inputFiles, bagPath);
    }
    
    @Test
    public void testSimpleSuccessfulExample() throws IOException, CompileException {
        
        File f = File.createTempFile("empty", "bb");
        f.deleteOnExit();
        PrintWriter out = new PrintWriter(f);
        out.write("bag MyBag {\nbool test = 0;\n}");
        out.close();
        
        String fileName = f.getAbsolutePath();
        List<String> inputFiles = Lists.newArrayList(fileName);
        List<String> bagPath = Lists.newArrayList();

        Linker linker = new Linker(inputFiles, bagPath);
    }

}
