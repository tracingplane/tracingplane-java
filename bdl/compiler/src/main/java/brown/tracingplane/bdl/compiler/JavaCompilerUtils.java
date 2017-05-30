package brown.tracingplane.bdl.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import brown.tracingplane.bdl.compiler.JavaCompiler;
import brown.tracingplane.bdl.compiler.Parser;
import brown.tracingplane.bdl.compiler.Ast.BagDeclaration;
import brown.tracingplane.bdl.compiler.Ast.BaggageBuffersDeclaration;

public class JavaCompilerUtils {

    /**
     * Replaces characters preceded by underscores with uppercase version, eg:
     * 
     * 'hello_world' => 'helloWorld' 'hello_World' => 'helloWorld' 'hello__world' => 'hello_World'
     */
    public static String formatCamelCase(String name) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ci = name.charAt(i);
            if (i < name.length() - 1 && ci == '_' && CharUtils.isAscii(ci)) {
                char cii = name.charAt(i + 1);
                if (CharUtils.isAsciiAlphaLower(cii)) {
                    formatted.append(StringUtils.upperCase(String.valueOf(cii)));
                    i++;
                }
            } else {
                formatted.append(name.charAt(i));
            }
        }
        return formatted.toString();
    }

    public static String indent(String str) {
        return indent(str, "    ");
    }

    public static String indent(String str, String indentationString) {
        String[] splits = StringUtils.split(str, "\n");
        for (int i = 0; i < splits.length; i++) {
            splits[i] = indentationString + splits[i];
        }
        return StringUtils.join(splits, "\n");
    }

    // Simple indentation of str based on occurrences of { and }. Does not account for comments
    public static String formatIndentation(String str, String indentWith) {
        str = StringUtils.strip(str);
        String[] splits = StringUtils.splitPreserveAllTokens(str, "\n");
        int depth = 0;
        for (int i = 0; i < splits.length; i++) {
            // Unindent leading }'s
            String line = StringUtils.strip(splits[i]);
            while (line.startsWith("}")) {
                line = StringUtils.strip(line.substring(1));
                depth--;
            }

            // Indent line to correct depth
            splits[i] = StringUtils.repeat(indentWith, depth) + StringUtils.strip(splits[i]);
            splits[i] = StringUtils.stripEnd(splits[i], null); // in case empty line
            depth += StringUtils.countMatches(line, "{");
            depth -= StringUtils.countMatches(line, "}");
        }
        return StringUtils.join(splits, "\n");
    }
    
    public static void writeOutputFile(String baseDir, String packageName, String className, String fileContents) throws FileNotFoundException {
        File outDir = new File(baseDir);
        if (packageName != null && packageName.length() > 0) {
            String[] splits = StringUtils.split(packageName, ".");
            for (String split : splits) {
                outDir = new File(outDir, split);
            }
        }
        
        outDir.mkdirs();
        
        File outFile = new File(outDir, className + ".java");
        PrintWriter out = new PrintWriter(outFile);
        out.write(fileContents);
        out.close();
    }

    public static void main(String[] args) throws Exception {
        String text = "package edu.brown.test; \nbag MyBag {\n" +
                      "bool firstField = 1; int32 secondField = 3; string whoops = 2;}";

        JavaCompiler compiler = new JavaCompiler();

        BaggageBuffersDeclaration decl = Parser.parseBaggageBuffersFile(text);
        Linker.fillPackageNames(decl);

        BagDeclaration bagDecl = decl.getBagDeclarations().get(0);
        compiler.compile(".", bagDecl);
        
//        String compiled = compiler.compile(bagDecl);
//        System.out.println(formatIndentation(compiled, "  "));

    }

}
