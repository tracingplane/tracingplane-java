package edu.brown.cs.systems.tracingplane.baggage_buffers.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
        String bag_path = ".";

        @Parameter(names = { "--version" }, description = "Show version info and exit.")
        boolean version = false;

        @Parameter(names = { "-h", "--help" }, help = true)
        private boolean help = false;

        @Parameter(names = { "--java_out" }, description = "Generate Java source file.")
        private boolean java = false;

        @Parameter(description = "file1 file2 ...")
        List<String> files = new ArrayList<>();

        /**
         * Search for the specified file in the import directories
         * 
         * @param filename the name of the file to search for
         * @return a {@link File} object for this filename
         * @throws ByteBufferCompilerException if the file cannot be found
         */
        public File findImport(String filename) throws ByteBufferCompilerException {
            for (String importDir : bag_path.split(";")) {
                File f = new File(importDir, filename);
                if (f.exists() && !f.isDirectory() && f.canRead()) {
                    return f;
                }
            }
            throw new ByteBufferCompilerException(filename + " could not be found on import path, searched: " +
                                                  bag_path);
        }

        /**
         * Search import directories for the specified file, then load it fully and return the string.
         * 
         * @param filename the name of the file to search for
         * @return the full contents of the file
         * @throws ByteBufferCompilerException if the file cannot be found or if an {@link IOException} occurs
         *             attempting to read the file
         */
        public String loadImport(String filename) throws ByteBufferCompilerException {
            try {
                File f = findImport(filename);
                FileInputStream fis = new FileInputStream(f);
                byte[] data = new byte[(int) f.length()];
                fis.read(data);
                fis.close();
                return new String(data, "UTF-8");
            } catch (FileNotFoundException e) {
                throw new ByteBufferCompilerException(filename + " existed but now cannot be found, aborting", e);
            } catch (UnsupportedEncodingException e) {
                throw new ByteBufferCompilerException(filename +
                                                      " could not be read because UTF-8 encoding not supported by platform",
                                                      e);
            } catch (IOException e) {
                throw new ByteBufferCompilerException(filename + " exists but cannot be read: " + e.getMessage(), e);
            }
        }

    }

    public static void main(String[] args) throws IOException {
        // Parse the args
        Settings params = new Settings();
        JCommander jc = new JCommander(params, args);
        jc.setProgramName("bbc");

        if (params.help) {
            jc.usage();
            return;
        }

        if (params.version) {
            System.out.println("bbc " + version);
            return;
        }

        if (params.files.size() <= 0) {
            System.out.println("Missing input file.");
            return;
        }

        try {
            new Compiler(params).compile();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}