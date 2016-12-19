package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import edu.brown.cs.systems.tracingplane.baggage_buffers.compiler.Ast.BaggageBuffersDeclaration;

public class BBC {

    public static final String version = "0.1.alpha";

    private static final Logger log = LoggerFactory.getLogger(BBC.class);

    /** Command line parameters */
    @Parameters(separators = "=")
    public static class Settings {

        @Parameter(names = { "--bag_path" },
                   description = "Specify the directory in which to search for imports.  May be specified multiple times; directories will be searched in order.  If not given, the current working directory is used.")
        String bagPath = ".";

        @Parameter(names = { "--version" }, description = "Show version info and exit.")
        boolean version = false;

        @Parameter(names = { "-h", "--help" }, help = true)
        boolean help = false;

        @Parameter(names = { "--java_out" }, description = "Output directory to generate Java source files")
        String javaOut = null;

        @Parameter(description = "file1 file2 ...")
        List<String> files = new ArrayList<>();

    }
    
    public static void compile(Settings settings) throws CompileException {
        Set<BaggageBuffersDeclaration> linked = Linker.link(settings);
        if (settings.javaOut != null) {
            new JavaCompiler().compile(settings.javaOut, linked);
        }
    }

    public static void main(String[] args) throws IOException {
        // Parse the args
        Settings settings = new Settings();
        JCommander jc = new JCommander(settings, args);
        jc.setProgramName("bbc");

        if (settings.help) {
            jc.usage();
            return;
        }

        if (settings.version) {
            System.out.println("bbc " + version);
            return;
        }

        if (settings.files.size() <= 0) {
            System.out.println("Missing input file.");
            return;
        }

        try {
            compile(settings);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}