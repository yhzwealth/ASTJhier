package edu.fudan.selab.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SolverUtils {
    private static final Logger logger = LoggerFactory.getLogger(SolverUtils.class);

    public static Pair<
            Map<JavaParserTypeSolver, Path>,
            List<JarTypeSolver>
            > genTypeSolversByPaths(List<String> pkgPaths, List<String> jarPaths) {
        Map<JavaParserTypeSolver, Path> jptss = genJavaParserTypeSolvers(pkgPaths);
        List<JarTypeSolver> jtss = genJarTypeSolvers(jarPaths);
        return new Pair<>(jptss, jtss);
    }

    /**
     * generate {@link JavaParserTypeSolver}, but haven't checked if it's a root of package
     * @param paths a list contains the root of packages
     * @return JavaParserTypeSolvers
     */
    public static Map<JavaParserTypeSolver, Path> genJavaParserTypeSolvers(List<String> paths) {
        return paths.stream()
                .map(Paths::get)
                .peek(p -> {
                    if (!Files.isDirectory(p)) {
                        logger.warn("genJavaParserTypeSolvers: " + p + " is not a dir, let alone the root of pkg");
                    }
                })
                .filter(p -> Files.isDirectory(p))
                .collect(Collectors.toMap(JavaParserTypeSolver::new, path -> path));
    }

    /**
     * generate {@link JarTypeSolver}
     * @param paths a list contains the path of jars
     * @return JarTypeSolvers
     */
    public static List<JarTypeSolver> genJarTypeSolvers(List<String> paths) {
        return paths.stream().peek(path -> {
                    if (!JarUtils.isJarFile(path)) {
                        logger.warn("genJarTypeSolvers: " + path + " is not a jar");
                    }
                })
                .filter(JarUtils::isJarFile)
                .map(SolverUtils::constructJarTypeSolverWrapper)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * just to wrap the constructor of JarTypeSolver, process exception
     * @param path still jar path
     * @return Optional
     */
    private static Optional<JarTypeSolver> constructJarTypeSolverWrapper(String path) {
        try {
            JarTypeSolver jts = new JarTypeSolver(path);
            return Optional.of(jts);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * just to check if Symbol Solver is presented
     * @return if Symbol Solver is presented
     */
    public static boolean isPresentSymbolSolver() {
        return StaticJavaParser
                .getParserConfiguration()
                .getSymbolResolver()
                .isPresent();
    }
}
