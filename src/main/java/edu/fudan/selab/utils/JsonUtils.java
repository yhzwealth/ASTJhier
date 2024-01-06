package edu.fudan.selab.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import edu.fudan.selab.config.Generator;
import edu.fudan.selab.entity.MethodGraph;
import edu.fudan.selab.entity.json.MethodJson;
import edu.fudan.selab.entity.json.NodeJson;
import edu.fudan.selab.entity.node.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JsonUtils {

    public static String toJSONString(MethodGraph graph) {
        String returnType = graph.getReturnNode().type;
        List<VarDepNode> parametersNode = graph.getParametersNode();
        HashMap<String, String> parametersMap = new HashMap<>();
        parametersNode.forEach(n -> {
            StringBuilder type = new StringBuilder(n.type);
            if (n instanceof ArrayDepNode)
                if (!type.toString().endsWith("[]"))
                    type.append("[]".repeat(((ArrayDepNode) n).dimension));
            parametersMap.put(n.name, type.toString());
        });
        parametersNode.add(NodeUtils.parseVarNode(graph.getClazz()));
        Map<String, NodeJson> nodeJsons = handleVarDepNode(parametersNode);

        MethodJson methodJson = new MethodJson(
                returnType,
                graph.getClazz().getFullyQualifiedClassName(),
                graph.getMethodName(),
                parametersMap,
                graph.getMethodName() + graph.getCode(),
                nodeJsons,
                graph.isStatic());
        return JSON.toJSONString(methodJson, JSONWriter.Feature.PrettyFormat);
    }

    private static Map<String, NodeJson> handleVarDepNode(List<VarDepNode> nodes) {

        ConcurrentLinkedQueue<AbstractDepNode> queue = new ConcurrentLinkedQueue<>(nodes);
        HashSet<String> handledType = new HashSet<>();
        Map<String, NodeJson> nodeMap = new HashMap<>();

        while (!queue.isEmpty()) {
            AbstractDepNode polled = queue.poll();
            if (handledType.contains(polled.type)) continue;
            if (polled instanceof ClassDepNode || polled instanceof ClassHierarchyDepNode) {
                ClassHierarchyDepNode cNode = (ClassHierarchyDepNode) Generator.GeneratorCache.get(polled.type);
                String typeName = cNode.type;

                String superClassName = null;
                if (cNode.superClass != null) {
                    superClassName = cNode.superClass.type;
                    queue.add(Generator.GeneratorCache.get(superClassName));
                }

                ArrayList<String> subClassName = new ArrayList<>();
                for (HierarchyDepNode sc : cNode.subClass) {
                    subClassName.add(sc.type);
                    queue.add(sc);
                }

                ArrayList<String> interfaces = new ArrayList<>();
                for (HierarchyDepNode anInterface : cNode.interfaces) {
                    interfaces.add(anInterface.type);
                    queue.add(anInterface);
                }

                HashMap<String, String> fields = new HashMap<>();
                for (VarDepNode field : cNode.fields) {
                    fields.put(field.name, field.type);
                    queue.add(field);
                }

                HashMap<String, Map<String, String>> constructors = new HashMap<>();
                for (Map.Entry<String, List<VarDepNode>> entry : cNode.constructors.entrySet()) {
                    HashMap<String, String> map = new HashMap<>();
                    for (VarDepNode parameter : entry.getValue()) {
                        map.put(parameter.name, parameter.type);
                        queue.add(parameter);
                    }
                    constructors.put(entry.getKey(), map);
                }

                HashMap<String, Map<String, String>> builders = new HashMap<>();
                if (cNode.builders != null) {
                    for (Map.Entry<String, List<VarDepNode>> entry : cNode.builders.entrySet()) {
                        HashMap<String, String> map = new HashMap<>();
                        for (VarDepNode parameter : entry.getValue()) {
                            map.put(parameter.name, parameter.type);
                            queue.add(parameter);
                        }
                        builders.put(entry.getKey(), map);
                    }
                }

                nodeMap.put(typeName, new NodeJson(cNode.classType, superClassName, subClassName, null, null, interfaces, null, fields, constructors, builders, null, null));
                handledType.add(typeName);
            } else if (polled instanceof ArrayDepNode) {
                ArrayDepNode aNode = (ArrayDepNode) polled;
                String innerType = aNode.innerComponentType.type;
                int dimension = aNode.dimension;
                queue.add(Generator.GeneratorCache.get(innerType));
                StringBuilder typeName = new StringBuilder(aNode.type);
                if (!typeName.toString().endsWith("[]"))
                    typeName.append("[]".repeat(aNode.dimension));
                nodeMap.put(typeName.toString(), new NodeJson(null, null, null, null, null, null, null, null, null, null, innerType, dimension));
                handledType.add(typeName.toString());
            } else if (polled instanceof InterfaceDepNode || polled instanceof InterfaceHierarchyDepNode) {
                InterfaceHierarchyDepNode iNode = (InterfaceHierarchyDepNode) Generator.GeneratorCache.get(polled.type);

                ArrayList<String> superInterfaceName = new ArrayList<>();
                for (HierarchyDepNode sc : iNode.superInterface) {
                    superInterfaceName.add(sc.type);
                    queue.add(sc);
                }

                ArrayList<String> subInterfaceName = new ArrayList<>();
                for (HierarchyDepNode sc : iNode.subInterface) {
                    subInterfaceName.add(sc.type);
                    queue.add(sc);
                }

                ArrayList<String> implementedClassName = new ArrayList<>();
                for (HierarchyDepNode sc : iNode.implementedClass) {
                    implementedClassName.add(sc.type);
                    queue.add(sc);
                }

                nodeMap.put(polled.type, new NodeJson("interface", null, null, superInterfaceName, subInterfaceName, null, implementedClassName, null, null, null, null, null));
                handledType.add(polled.type);
            } else {
                nodeMap.put(polled.type, new NodeJson());
                handledType.add(polled.type);
            }
        }

        return nodeMap;
    }
}
