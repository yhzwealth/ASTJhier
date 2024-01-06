package edu.fudan.selab.utils;

import com.github.kwart.jd.JavaDecompiler;
import com.github.kwart.jd.input.JDInput;
import com.github.kwart.jd.input.ZipFileInput;
import com.github.kwart.jd.options.DecompilerOptions;
import com.github.kwart.jd.output.DirOutput;
import com.github.kwart.jd.output.JDOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.jar.JarInputStream;

public class JarUtils {

    private static final Logger logger = LoggerFactory.getLogger(JarUtils.class);

    private static final JavaDecompiler DECOMPILER = new JavaDecompiler(new DecompilerOptions() {
        @Override public boolean isSkipResources() { return true; } // skip resource, only process source
        @Override public boolean isDisplayLineNumbers() { return false; }
        @Override public boolean isEscapeUnicodeCharacters() { return false; }
        @Override public boolean isParallelProcessingAllowed() { return true; }
    });

    /**
     * Used to decompile a jar
     * @param jarPath the path to jar
     * @param outputPath the path to a folder used to store the sources
     */
    public static void decompileJar(String jarPath, String outputPath) {
        JDInput input = new ZipFileInput(jarPath);
        JDOutput output = new DirOutput(new File(outputPath));
        try {
            input.decompile(DECOMPILER, output);
        } catch (NullPointerException e) {
            logger.warn("[NullPointerException] jarPath: " + jarPath + " cannot be decompiled all the *.class files successfully");
        } catch (AssertionError e) {
            logger.warn("[AssertionError] jarPath: " + jarPath + " cannot be decompiled all the *.class files successfully");
        }
    }

    /**
     * Used to unzip a Jar
     * @param jarPath the path to jar
     * @param extractDir the path to extracted dir
     * @param encoding default utf-8
     * @return if empty, zip failed; if present, zip succeeded
     * @throws IOException
     */
    public static Optional<String> unzipJar(String jarPath, String extractDir, String encoding) throws IOException {
        Optional<String> jarFileNameOpt = FileUtils.getFileName(jarPath);
        if (jarFileNameOpt.isEmpty()) {
            return Optional.empty();
        }

        String jarContentPath = extractDir + File.separator + jarFileNameOpt.get() + "_unzip";
        File jarContentFolder = new File(jarContentPath);
        if (!jarContentFolder.exists()) {
            jarContentFolder.mkdirs();
        }

        ZipUtils.unzip(jarPath, jarContentPath, encoding);
        return Optional.of(jarContentPath);
    }

    /**
     * Used to distinguish if a file is a jar
     * @param filePath path to file
     * @return true => is a jar
     */
    public static boolean isJarFile(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             JarInputStream jis = new JarInputStream(fis)) {
            return jis.getNextJarEntry() == null;
        } catch (IOException e) {
            return false;
        }
    }

}
