package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

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
        private boolean help = false;

        @Parameter(names = { "--java_out" }, description = "Generate Java source file.")
        private boolean java = false;

        @Parameter(description = "file1 file2 ...")
        public List<String> files = new ArrayList<>();

    }
    
    public static void run(Settings settings) throws CompileException {
        Linker.process(settings);
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
            run(settings);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}