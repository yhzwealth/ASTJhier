package edu.fudan.selab.service;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import edu.fudan.selab.config.Global;
import edu.fudan.selab.config.MBP;
import edu.fudan.selab.entity.ClassInfo;
import edu.fudan.selab.entity.FieldInfo;
import edu.fudan.selab.entity.MethodInfo;
import edu.fudan.selab.entity.pojo.*;
import edu.fudan.selab.mapper.*;
import edu.fudan.selab.utils.CUUtils;
import edu.fudan.selab.utils.FileUtils;
import edu.fudan.selab.utils.SolverUtils;
import edu.fudan.selab.visitor.ClassInfoCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PersistenceService {
    private static final Logger logger = LoggerFactory.getLogger(PersistenceService.class);

    private static final TypeMapper typeMapper = MBP.sqlSession.getMapper(TypeMapper.class);
    private static final ExtendedMapper extendedMapper = MBP.sqlSession.getMapper(ExtendedMapper.class);
    private static final ImplementedMapper implementedMapper = MBP.sqlSession.getMapper(ImplementedMapper.class);
    private static final MethodMapper methodMapper = MBP.sqlSession.getMapper(MethodMapper.class);
    private static final FieldMapper fieldMapper = MBP.sqlSession.getMapper(FieldMapper.class);
    private static final ParameterMapper parameterMapper = MBP.sqlSession.getMapper(ParameterMapper.class);
    private static final InitMapper initMapper = MBP.sqlSession.getMapper(InitMapper.class);
    private static List<Type> types = new ArrayList<>();
    private static List<Extended> extended = new ArrayList<>();
    private static List<Implemented> implemented = new ArrayList<>();
    private static List<Method> methods = new ArrayList<>();
    private static List<Field> fields = new ArrayList<>();
    private static List<Parameter> parameters = new ArrayList<>();

    public static void saveJFileToDB(String fullyQualifiedPackageName) {
        List<ClassInfo> classInfos = handleFileToInfo(fullyQualifiedPackageName);

        for (int i = 0; i < classInfos.size(); i++) {
            ClassInfo classInfo = classInfos.get(i);
            //To avoid handle Annotation
            if (classInfo.getClassName() == null) continue;
            types.add(transformToType(classInfo));
            extended.add(transformToExtended(classInfo));
            implemented.addAll(transformToImplemented(classInfo));
            fields.addAll(transformToField(classInfo));
            methods.addAll(transformToMethod(classInfo));

            if (i % 100 == 0 || i == classInfos.size() - 1) {
                if (!types.isEmpty()) {
                    typeMapper.insertAll(types);
                    types = new ArrayList<>();
                }
                if (!extended.isEmpty()) {
                    extendedMapper.insertAll(extended);
                    extended = new ArrayList<>();
                }
                if (!implemented.isEmpty()) {
                    implementedMapper.insertAll(implemented);
                    implemented = new ArrayList<>();
                }
                if (!fields.isEmpty()) {
                    fieldMapper.insertAll(fields);
                    fields = new ArrayList<>();
                }
                if (!methods.isEmpty()) {
                    while (methods.size() > 1000){
                        methodMapper.insertAll(methods.subList(0, 1000));
                        methods = methods.subList(1000, methods.size());
                    }
                    methodMapper.insertAll(methods);
                    methods = new ArrayList<>();
                }
                if (!parameters.isEmpty()) {
                    while (parameters.size() > 1000) {
                        parameterMapper.insertAll(parameters.subList(0, 1000));
                        parameters = parameters.subList(1000, parameters.size());
                    }
                    parameterMapper.insertAll(parameters);
                    parameters = new ArrayList<>();
                }
            }
        }


    }

    public static List<Method> getRandomMethodName(){
        return methodMapper.selectRandomMethod();
    }

    private static List<ClassInfo> handleFileToInfo(String fullyQualifiedPackageName) {
        List<String> allJavaFile = FileUtils.getAllJavaFile(Global.DECOMPILED_SOURCE_ROOT, fullyQualifiedPackageName);
        List<ClassInfo> classInfos = new ArrayList<>();
        for (String jFile : allJavaFile) {
            CompilationUnit cu;
            try {
                cu = CUUtils.parseByFilePath(jFile);
            } catch (IOException e) {
                // Some source code has syntax errors and cannot be parsed.
                throw new RuntimeException(e);
            }
            if (cu == null) continue;
            ClassInfoCollector collector = new ClassInfoCollector();
            ClassInfo info = new ClassInfo();
            collector.visit(cu, info);
            classInfos.add(info);
        }
        return classInfos;
    }

    private static Type transformToType(ClassInfo classInfo) {
        String fullyQualifiedClassName = classInfo.getClassName();
        String className = fullyQualifiedClassName;
        String packageName = null;
        int index = className.lastIndexOf(".");
        if (index != -1) {
            packageName = className.substring(0, index);
            className = className.substring(index + 1);
        }
        return new Type(fullyQualifiedClassName, className, packageName,
                classInfo.getClassType().equals("interface"), classInfo.getClassType().equals("abstract"));
    }

    private static Extended transformToExtended(ClassInfo classInfo) {
        String fullyQualifiedClassName = classInfo.getClassName();
        return new Extended(fullyQualifiedClassName, classInfo.getSuperClassName());
    }

    private static List<Implemented> transformToImplemented(ClassInfo classInfo) {
        String fullyQualifiedClassName = classInfo.getClassName();
        ArrayList<Implemented> list = new ArrayList<>();
        for (String key : classInfo.getInterfaces().keySet()) {
            list.add(new Implemented(fullyQualifiedClassName, key));
        }
        return list;
    }

    private static List<Field> transformToField(ClassInfo classInfo) {
        String fullyQualifiedClassName = classInfo.getClassName();
        ArrayList<Field> list = new ArrayList<>();
        for (Map.Entry<String, FieldInfo> entry : classInfo.getFields().entrySet()) {
            FieldInfo fieldInfo = entry.getValue();
            FieldDeclaration fd = fieldInfo.getFd();
            String modifier = fd.isPublic() ? "public" : fd.isPrivate() ? "private" : fd.isProtected() ? "protected" : "";
            String fieldTypeName = getQualifiedName(fieldInfo.getType());
            int level = fieldInfo.getType().isArrayType() ? fieldInfo.getType().getArrayLevel() : 0;

            list.add(new Field(fullyQualifiedClassName, modifier, fd.isStatic(),
                    fieldTypeName, level, fieldInfo.getName()));
        }
        return list;
    }

    private static List<Method> transformToMethod(ClassInfo classInfo) {
        String fullyQualifiedClassName = classInfo.getClassName();
        ArrayList<Method> list = new ArrayList<>();
        for (Map.Entry<String, MethodInfo> entry : classInfo.getMethods().entrySet()) {
            MethodInfo methodInfo = entry.getValue();
            String code = methodInfo.getCode() != null ? methodInfo.getCode().toString() : "";
            if (methodInfo.getIsStatic() && methodInfo.getModifier().equals("public") && methodInfo.getReturnType() != null && getQualifiedName(methodInfo.getReturnType()).equals(fullyQualifiedClassName))
                methodInfo.setIsBuilder(true);
            if (methodInfo.getIsConstructor() && !fullyQualifiedClassName.endsWith(methodInfo.getMethodName())) {
                methodInfo.setIsConstructor(false);
            }
            list.add(new Method(methodInfo.getModifier(), fullyQualifiedClassName, getQualifiedName(methodInfo.getReturnType()), entry.getKey(), code != null ? code : "",
                    methodInfo.getIsStatic(), methodInfo.getIsAbstract(), methodInfo.getIsConstructor(), methodInfo.getIsBuilder()));
            parameters.addAll(transformToParameter(fullyQualifiedClassName, entry.getKey(), methodInfo.getParameters()));
        }
        return list;
    }

    private static List<Parameter> transformToParameter(String className, String methodName, Map<String, com.github.javaparser.ast.type.Type> map) {
        ArrayList<Parameter> list = new ArrayList<>();
        for (Map.Entry<String, com.github.javaparser.ast.type.Type> entry : map.entrySet()) {
            int level = entry.getValue().isArrayType() ? entry.getValue().getArrayLevel() : 0;
            list.add(new Parameter(className, methodName, getQualifiedName(entry.getValue()), level, entry.getKey()));
        }
        return list;
    }

    private static String getQualifiedName(com.github.javaparser.ast.type.Type type) {
        if (type == null) return "";
        String fieldTypeName;
        try {
            if (type.isArrayType()) {
                com.github.javaparser.ast.type.Type elementType = type.getElementType();
                if (elementType.isReferenceType()) {
                    if (SolverUtils.isPresentSymbolSolver())
                        fieldTypeName = elementType.resolve().asReferenceType().getQualifiedName();
                    else
                        fieldTypeName = elementType.toString();
                } else {
                    fieldTypeName = elementType.toString();
                }
            } else if (type.isReferenceType()) {
                if (SolverUtils.isPresentSymbolSolver())
                    fieldTypeName = type.resolve().asReferenceType().getQualifiedName();
                else
                    fieldTypeName = type.toString();
            } else {
                fieldTypeName = type.toString();
            }
        } catch (Exception exception) {
            logger.error("Type `" + type + "` can not be handled successfully");
            fieldTypeName = type.toString();
        }
        return fieldTypeName;
    }
}
