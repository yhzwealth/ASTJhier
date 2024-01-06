package edu.fudan.selab.service.generator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.fudan.selab.config.Generator;
import edu.fudan.selab.config.MBP;
import edu.fudan.selab.entity.node.*;
import edu.fudan.selab.entity.pojo.*;
import edu.fudan.selab.mapper.*;
import edu.fudan.selab.utils.SolverUtils;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractNodeGenerator implements GenerateNode {

    private static final TypeMapper typeMapper = MBP.sqlSession.getMapper(TypeMapper.class);
    private static final ExtendedMapper extendedMapper = MBP.sqlSession.getMapper(ExtendedMapper.class);
    private static final ImplementedMapper implementedMapper = MBP.sqlSession.getMapper(ImplementedMapper.class);
    private static final MethodMapper methodMapper = MBP.sqlSession.getMapper(MethodMapper.class);
    private static final ParameterMapper parameterMapper = MBP.sqlSession.getMapper(ParameterMapper.class);
    private static final List<String> primitiveType = List.of("byte", "short", "int", "long", "float", "double", "char", "boolean");

    @Override
    public HierarchyDepNode parseHierarchyNode(String fullyQualifiedClassName) {
        if (Generator.GeneratorCache.get(fullyQualifiedClassName) != null) {
            return Generator.GeneratorCache.get(fullyQualifiedClassName);
        }
        Type type = typeMapper.selectById(fullyQualifiedClassName);
        if (type == null) {
            NullHierarchyDepNode node = new NullHierarchyDepNode(fullyQualifiedClassName);
            Generator.GeneratorCache.put(fullyQualifiedClassName, node);
            return node;
        } else if (type.getIsInterface()) {
            return parseInterfaceHierarchyDepNode(type);
        }
        return parseClassHierarchyDepNode(type);
    }

    public ClassHierarchyDepNode parseClassHierarchyDepNode(Type type){
        Extended extended = extendedMapper.selectOne(new LambdaQueryWrapper<Extended>().eq(Extended::getTypeName, type.getFullyQualifiedClassName()));
        String superClassName = extended != null ? extended.getSuperClassName() : null;
        ClassHierarchyDepNode node = new ClassHierarchyDepNode(type.getFullyQualifiedClassName(), parseHierarchyNode(superClassName));
        Generator.GeneratorCache.put(type.getFullyQualifiedClassName(), node);

        List<Extended> subClass = extendedMapper.selectList(new LambdaQueryWrapper<Extended>().eq(Extended::getSuperClassName, type.getFullyQualifiedClassName()));
        subClass.forEach(sc -> node.subClass.add(parseHierarchyNode(sc.getTypeName())));

        Generator.GeneratorCache.put(type.getFullyQualifiedClassName(), node);
        node.classType = type.getIsAbstract() ? "abstract class" : type.getIsInterface() ? "interface" : "class";

        List<Implemented> implementedList = implementedMapper.selectList(new LambdaQueryWrapper<Implemented>().eq(Implemented::getTypeName, type.getFullyQualifiedClassName()));
        for (Implemented implemented : implementedList) {
            node.interfaces.add(parseHierarchyNode(implemented.getInterfaceName()));
        }

        List<Method> methodList = methodMapper.selectList(new LambdaQueryWrapper<Method>().eq(Method::getClassName, type.getFullyQualifiedClassName()));
        for (Method method : methodList) {
            ArrayList<VarDepNode> parameters = new ArrayList<>();
            List<Parameter> parameterList = parameterMapper.selectList(new LambdaQueryWrapper<Parameter>().eq(Parameter::getClassName, type.getFullyQualifiedClassName())
                    .eq(Parameter::getMethodName, method.getName()));
            for (Parameter parameter : parameterList) {
                parameters.add(parseVarNode(parameter));
            }
            if (method.getModifier().equals("public") && method.getIsConstructor()){
                node.constructors.put(method.getName(), parameters);
            }else if (method.getIsBuilder()){
                node.builders.put(method.getName(), parameters);
            }
        }

        if (node.constructors.isEmpty())
            if (methodList.stream().noneMatch(Method::getIsConstructor))
                node.constructors.put(type.getName() + "()", new ArrayList<>());

        return node;
    }

    @Override
    public VarDepNode parseVarNode(Type type, String name) {
        return getVarDepNode(type.getFullyQualifiedClassName(), 0, name);
    }

    @Override
    public VarDepNode parseVarNode(Parameter parameter) {
        return getVarDepNode(parameter.getTypeName(), parameter.getLevel(), parameter.getName());
    }

    @Override
    public VarDepNode parseVarNode(Field field) {
        return getVarDepNode(field.getTypeName(), field.getLevel(), field.getName());
    }

    private VarDepNode getVarDepNode(String typeName, Integer level, String name) {
        Type type = typeMapper.selectById(typeName);
        if (level != 0) {
            return new ArrayDepNode(typeName + "[]".repeat(level), name, parseHierarchyNode(typeName), level);
        } else if (primitiveType.contains(typeName)) {
            return new PrimitiveDepNode(typeName, name);
        } else if (type == null) {
            return new NullDepNode(typeName, name);
        } else {
            if (SolverUtils.isPresentSymbolSolver()) {
                return parseClassOrInterfaceVarNode(typeName, name);
            } else {
                return new NullDepNode(typeName, name);
            }
        }
    }

    private InterfaceHierarchyDepNode parseInterfaceHierarchyDepNode(Type type){
        InterfaceHierarchyDepNode node = new InterfaceHierarchyDepNode(type.getFullyQualifiedClassName());
        Generator.GeneratorCache.put(type.getFullyQualifiedClassName(), node);
        List<Extended> extendedList = extendedMapper.selectList(new LambdaQueryWrapper<Extended>().eq(Extended::getTypeName, type.getFullyQualifiedClassName()));
        List<String> superClassNames = extendedList.stream().map(Extended::getSuperClassName).collect(Collectors.toList());
        if (!superClassNames.isEmpty()) {
            List<Type> types = typeMapper.selectList(new LambdaQueryWrapper<Type>().in(Type::getFullyQualifiedClassName, superClassNames));
            types.forEach(t -> node.superInterface.add(parseHierarchyNode(t.getFullyQualifiedClassName())));
        }

        extendedList = extendedMapper.selectList(new LambdaQueryWrapper<Extended>().eq(Extended::getSuperClassName, type.getFullyQualifiedClassName()));
        List<String> subClassNames = extendedList.stream().map(Extended::getTypeName).collect(Collectors.toList());
        if (!subClassNames.isEmpty()) {
            List<Type> types = typeMapper.selectList(new LambdaQueryWrapper<Type>().in(Type::getFullyQualifiedClassName, subClassNames));
            types.forEach(t -> node.subInterface.add(parseHierarchyNode(t.getFullyQualifiedClassName())));
        }

        List<Implemented> implementedList = implementedMapper.selectList(new LambdaQueryWrapper<Implemented>().eq(Implemented::getInterfaceName, type.getFullyQualifiedClassName()));
        List<String> implementedClassName = implementedList.stream().map(Implemented::getTypeName).collect(Collectors.toList());
        if (!implementedClassName.isEmpty()) {
            List<Type> types = typeMapper.selectList(new LambdaQueryWrapper<Type>().in(Type::getFullyQualifiedClassName, implementedClassName));
            types.forEach(t -> node.implementedClass.add(parseHierarchyNode(t.getFullyQualifiedClassName())));
        }

        return node;
    }

    private VarDepNode parseClassOrInterfaceVarNode(String fullyQualifiedName, String name){
        HierarchyDepNode hierarchyNode = parseHierarchyNode(fullyQualifiedName);
        if (hierarchyNode instanceof InterfaceHierarchyDepNode) {
            InterfaceHierarchyDepNode interfaceHierarchyDepNode = (InterfaceHierarchyDepNode) hierarchyNode;
            InterfaceDepNode interfaceDepNode = new InterfaceDepNode(fullyQualifiedName, name);
            interfaceDepNode.superInterface = interfaceHierarchyDepNode.superInterface;
            interfaceDepNode.subInterface = interfaceHierarchyDepNode.subInterface;
            interfaceDepNode.implementedClass = interfaceHierarchyDepNode.implementedClass;
            return interfaceDepNode;
        } else if (hierarchyNode instanceof ClassHierarchyDepNode) {
            ClassHierarchyDepNode classHierarchyDepNode = (ClassHierarchyDepNode) hierarchyNode;
            ClassDepNode classDepNode = new ClassDepNode(fullyQualifiedName, name, classHierarchyDepNode.superClass);
            classDepNode.subClass = classHierarchyDepNode.subClass;
            classDepNode.classType = classHierarchyDepNode.classType;
            classDepNode.interfaces = classHierarchyDepNode.interfaces;
            classDepNode.fields = classHierarchyDepNode.fields;
            classDepNode.constructors = classHierarchyDepNode.constructors;
            classDepNode.builders = classHierarchyDepNode.builders;
            return classDepNode;
        }
        return new NullDepNode(fullyQualifiedName, name);
    }

}
