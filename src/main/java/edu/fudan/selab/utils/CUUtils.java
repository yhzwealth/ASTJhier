package edu.fudan.selab.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CUUtils {

    private static final Logger logger = LoggerFactory.getLogger(CUUtils.class);

    /**
     * Used to generate CU directly
     * @param filePath the java file path
     * @return the corresponding CU
     * @throws IOException due to StaticJavaParser.parse(path)
     */
    public static CompilationUnit parseByFilePath(String filePath)
    throws IOException {
        Path path = Paths.get(filePath);
        CompilationUnit parse = null;
        try {
            parse = StaticJavaParser.parse(path);
        } catch (ParseProblemException e){
            logger.error("File: " + path + " cannot be parsed");
        }
        return parse;
    }

    /**
     * Used to parse CU with fully qualified class name.
     * @param fullyQualifiedClassName e.g. edu.fudan.selab.utils.CUUtils
     * @param srcPath the root of source path, e.g. src/main/java
     * @return the corresponding CU
     * @throws IOException due to StaticJavaParser.parse(path) actually
     */
    public static CompilationUnit parsedByFullyQualifiedClassName(String fullyQualifiedClassName, String srcPath)
    throws IOException {
        Path filePath = genFilePathByFullyQualifiedClassName(fullyQualifiedClassName, srcPath);
        return CUUtils.parseByFilePath(filePath.toString());
    }

    private static Path genFilePathByFullyQualifiedClassName(String fullyQualifiedClassName, String srcPath) {
        return Paths.get(srcPath,
                fullyQualifiedClassName.replace(".", "/") + ".java");
    }

}
