package edu.fudan.selab.config;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Pair;
import edu.fudan.selab.service.PersistenceService;
import edu.fudan.selab.utils.*;
import edu.fudan.selab.utils.FileUtils;
import edu.fudan.selab.utils.JarUtils;
import edu.fudan.selab.utils.SolverUtils;
import edu.fudan.selab.utils.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple4;

import java.nio.file.Path;
import java.util.*;

public class Global {

    private static final Logger logger = LoggerFactory.getLogger(Global.class);

    public static final String DECOMPILED_SOURCE_ROOT = ".decompiled_sources";
    private static final String CLASS_PATH = "../jarPath";


    public static final ReflectionTypeSolver reflectionTS = new ReflectionTypeSolver();
    public static final Map<JavaParserTypeSolver, Path> jpTSs = new HashMap<>();
    public static final List<JarTypeSolver> jTSs = new ArrayList<>();

    public static Optional<CombinedTypeSolver> allTS = Optional.empty();
    public static Optional<JavaSymbolSolver> jSS = Optional.empty();

    /**
     * The GAV format here is "group_id:artifact_id:version"
     * e.g. org.scalaz:scalaz-core_2.11:7.2.3
     * @param jarGAV GAV of a jar, see {@link CoursierUtils#makeDependency(String)}
     */
    public static void initBySingleJarGAV(String jarGAV) {
        Tuple4<List<String>, List<String>, List<String>, List<String>> results =
                CoursierUtils.downloadDependencies(List.of(jarGAV));
        List<String> foundSrcs = results._1();

        StringBuilder sb = new StringBuilder();
        results._3().forEach(r -> sb.append(r).append(":"));
        FileUtils.writeFile(CLASS_PATH, sb.deleteCharAt(sb.length() - 1).toString(), false);

        List<String> rootPkgPaths = ZipUtils.unzipSourceJars(foundSrcs);
        List<String> pkgNames = FileUtils.getPkgName(rootPkgPaths);
        Set<String> nameSet = new HashSet<>(pkgNames);
        FileUtils.integrateFolders(DECOMPILED_SOURCE_ROOT, rootPkgPaths);
        // init static java parser
//        initStaticJavaParser(List.of(DECOMPILED_SOURCE_ROOT), List.of());
        // save package class info into database
        nameSet.forEach(PersistenceService::saveJFileToDB);
    }

    /**
     * Exposed API
     * @param jarWithAllDependenciesPath the path to *-with-all-dependencies.jar
     */
    public static void initByJarWithAllDependencies(String jarWithAllDependenciesPath) {
        initStaticJavaParser(jarWithAllDependenciesPath);
    }

    /**
     * Exposed API
     * @param pkgPaths a list contains the root of packages
     * @param jarPaths a list contains the path of jars
     */
    public static void initByPkgJarPaths(List<String> pkgPaths, List<String> jarPaths) {
        initStaticJavaParser(pkgPaths, jarPaths);
    }

    /**
     * Used to initialize the settings of {@link StaticJavaParser}.
     * @param jarWithAllDependenciesPath the path to *-with-all-dependencies.jar
     */
    private static void initStaticJavaParser(String jarWithAllDependenciesPath) {
        JarUtils.decompileJar(jarWithAllDependenciesPath, DECOMPILED_SOURCE_ROOT);
        initStaticJavaParser(List.of(DECOMPILED_SOURCE_ROOT), List.of());
    }

    /**
     * Used to initialize the settings of {@link StaticJavaParser}.
     * Currently, only add new TypeSolvers for {@link StaticJavaParser}.
     */
    private static void initStaticJavaParser(List<String> pkgPaths, List<String> jarPaths){
        Pair<Map<JavaParserTypeSolver, Path>, List<JarTypeSolver>> pair =
                SolverUtils.genTypeSolversByPaths(pkgPaths, jarPaths);

        // before initByJarWithAllDependencies, I need remove ALL TypeSolvers of both.
        jpTSs.clear();
        jpTSs.putAll(pair.a);
        jTSs.clear();
        jTSs.addAll(pair.b);

        // NOTICE: the sequence of TypeSolver adding is related to the sequence to
        // utilize the resources to resolve a type.
        CombinedTypeSolver cTS = new CombinedTypeSolver();
        cTS.add(reflectionTS);
        pair.a.forEach((key, value) -> cTS.add(key));
        pair.b.forEach(cTS::add);
        allTS = Optional.of(cTS);

        jSS = Optional.of(new JavaSymbolSolver(cTS));
        // StaticJavaParser now uses "utf-8" encoding, Java 11 by default
        StaticJavaParser.getParserConfiguration().setSymbolResolver(jSS.get());
    }

}
