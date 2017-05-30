package brown.tracingplane.bdl.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;

/** Useful methods to do with reading files */
public class FileUtils {

    private FileUtils() {}
    
    
    public static List<String> splitBagPath(String bagPath) {
        return Lists.<String>newArrayList(StringUtils.split(bagPath, File.pathSeparator));
    }
    
    public static String joinBagPath(List<String> bagPathElements) {
        return StringUtils.join(bagPathElements, File.pathSeparator);
    }
    
    
    /**
     * Simply gets the File object for the file with the specified name, but also checks if it exists and is readable
     * @param fileName the name of the file to get
     * @return a {@link File} object for this fileName or null if it could not be found
     */
    public static File getFile(String fileName) {
        File f = new File(fileName).getAbsoluteFile();
        if (f.exists() && f.isFile() && f.canRead()) {
            return f;
        } else {
            return null;
        }
    }

    /**
     * Search several directories for a file with the specified filename. Ignores files if they are not readable.
     * 
     * @param fileName the name of the file to search for
     * @param directories directories to search
     * @return a {@link File} object for this fileName, or null if no file could be found in any of the directories
     */
    public static File findFile(String fileName, List<String> directories) {
        for (String directory : directories) {
            File f = new File(directory, fileName);
            if (f.exists() && f.isFile() && f.canRead()) {
                return f;
            }
        }
        return null;
    }

    /**
     * Read the entire contents of a file as a String
     * 
     * @param file the file to read
     * @return the contents of the file
     * @throws IOException if the file cannot be read for some reason
     */
    public static String readFully(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return new String(data, Charset.defaultCharset());
    }

}
