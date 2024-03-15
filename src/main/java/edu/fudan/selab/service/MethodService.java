package edu.fudan.selab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.fudan.selab.config.Global;
import edu.fudan.selab.config.MBP;
import edu.fudan.selab.entity.MethodGraph;
import edu.fudan.selab.entity.node.HierarchyDepNode;
import edu.fudan.selab.entity.node.VarDepNode;
import edu.fudan.selab.entity.pojo.Method;
import edu.fudan.selab.entity.pojo.Parameter;
import edu.fudan.selab.entity.pojo.Type;
import edu.fudan.selab.mapper.*;
import edu.fudan.selab.utils.NodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static edu.fudan.selab.config.Global.DECOMPILED_SOURCE_ROOT;

public class MethodService {

    private static final Logger logger = LoggerFactory.getLogger(MethodService.class);
    private static final TypeMapper typeMapper = MBP.sqlSession.getMapper(TypeMapper.class);
    private static final MethodMapper methodMapper = MBP.sqlSession.getMapper(MethodMapper.class);
    private static final ParameterMapper parameterMapper = MBP.sqlSession.getMapper(ParameterMapper.class);

    /**
     * Exposed API
     * Used to get Method Graph via method signature
     *
     * @param srcPath   DECOMPILED_SOURCE_ROOT by default
     * @param signature a method signature, its format is like that in jazzer-autofuzz.
     *                  e.g. com.google.json.JsonSanitizer::sanitize(Integer, String)
     * @return Method Graph
     * @throws IllegalArgumentException when format is not valid
     */
    public static MethodGraph handleMethodBySignature(
            String srcPath,
            String signature)
            throws IllegalArgumentException {
//        Global.initByPkgJarPaths(List.of(DECOMPILED_SOURCE_ROOT), List.of());
        String regex = "^([\\w.]+)::([\\w]+)\\(([^)]*)\\)$";
        // Compile the regex pattern
        Pattern pattern = Pattern.compile(regex);
        // Create a Matcher object to perform the match operation
        Matcher matcher = pattern.matcher(signature);

        // Check if the input string matches the pattern
        if (matcher.matches()) {
            // Extract the captured groups
            String className = matcher.group(1);
            String methodName = matcher.group(2);
            String[] parameterTypes = matcher.group(3).split(",");
            String[] parameters = Arrays.stream(parameterTypes)
                    .map(String::trim).toArray(String[]::new);

            logger.info("handleMethodBySignature: className(" + className + ")");
            logger.info("handleMethodBySignature: methodName(" + methodName + ")");
            logger.info("handleMethodBySignature: parameters(" + Arrays.toString(parameters) + ")");
            return handleMethodBySrcPath(srcPath, className, methodName, parameters);
        } else {
            throw new IllegalArgumentException(
                    "handleMethodBySignature: " + signature + " not valid. No match found.");
        }
    }

    /**
     * Used to get Method Graph via method signature
     *
     * @param srcPath                 DECOMPILED_SOURCE_ROOT by default
     * @param fullyQualifiedClassName as its name
     * @param methodName              as its name
     * @param parameterTypes          as its name
     * @return Method Graph
     */
    public static MethodGraph handleMethodBySrcPath(
            String srcPath,
            String fullyQualifiedClassName,
            String methodName,
            String... parameterTypes) {

        // use method visitor to retrieve MethodInfo
        List<String> pList = List.of(parameterTypes);
        List<String> pTypes = new ArrayList<>();
        for (int i = 0; i < pList.size(); i++) {
            StringBuilder newString = new StringBuilder(pList.get(i));
            int left = 0, right = 0;
            while (left + count(pList.get(i), "<") != right + count(pList.get(i), ">")) {
                newString.append(", ").append(pList.get(i + 1));
                left += count(pList.get(i), "<");
                right += count(pList.get(i), ">");
                i++;
            }
            pTypes.add(newString.toString());
        }

        StringBuilder search = new StringBuilder("%").append(methodName).append("(");
        pTypes.forEach(s -> search.append(s).append(" %, "));
        search.deleteCharAt(search.length() - 1).deleteCharAt(search.length() - 1).append(")");
        List<Type> types = typeMapper.selectList(new LambdaQueryWrapper<Type>()
                .eq(Type::getFullyQualifiedClassName, fullyQualifiedClassName));
        if (types.size() != 1) {
            String msg = "handleMethodBySrcPath: didn't find type " + srcPath
                    + " " + fullyQualifiedClassName
                    + " " + methodName
                    + " " + pTypes;
            logger.warn(msg);
            throw new RuntimeException(msg);
        }

        List<Method> methods = methodMapper.selectList(new LambdaQueryWrapper<Method>()
                .eq(Method::getClassName, fullyQualifiedClassName).like(Method::getName, search));
        methods = methods.stream().filter(m -> {
            int temp = 0;
            for (char c : m.getName().toCharArray()) {
                if (c == ',') temp++;
            }

            return temp == pTypes.size() - 1 && m.getName().contains(" " + methodName + "(");
        }).collect(Collectors.toList());
        if (methods.isEmpty()) {
            logger.warn("handleMethodBySrcPath: didn't find " + srcPath
                    + " " + fullyQualifiedClassName
                    + " " + methodName
                    + " " + pTypes);
            return null;
        } else if (methods.size() > 1) {
            logger.warn("handleMethodBySrcPath: found more than 1 method!! for " + srcPath
                    + " " + fullyQualifiedClassName
                    + " " + methodName
                    + " " + pTypes);
            return null;
        }
        Method methodInfo = methods.get(0);
        List<Parameter> parameters = parameterMapper.selectList(new LambdaQueryWrapper<Parameter>()
                .eq(Parameter::getClassName, fullyQualifiedClassName).eq(Parameter::getMethodName, methodInfo.getName()));

        HierarchyDepNode returnNode = NodeUtils.parseHierarchyNode(methodInfo.getReturnType());

        ArrayList<VarDepNode> list = new ArrayList<>();
        parameters.forEach(p -> list.add(NodeUtils.parseVarNode(p)));

        return new MethodGraph(
                returnNode,
                methodInfo.getName(),
                list,
                methodInfo.getCode(),
                methodInfo.getIsStatic(),
                types.get(0));
    }

    private static int count(String str, String temp) {
        int count = 0;
        while (str.contains(temp)) {
            str = str.substring(str.indexOf(temp) + 1);
            count++;
        }
        return count;
    }
}
