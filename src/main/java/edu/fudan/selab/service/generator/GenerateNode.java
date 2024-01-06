package edu.fudan.selab.service.generator;

import edu.fudan.selab.entity.node.HierarchyDepNode;
import edu.fudan.selab.entity.node.VarDepNode;
import edu.fudan.selab.entity.pojo.Field;
import edu.fudan.selab.entity.pojo.Parameter;
import edu.fudan.selab.entity.pojo.Type;

public interface GenerateNode {
    HierarchyDepNode parseHierarchyNode(String fullQualifiedClassName);
    VarDepNode parseVarNode(Type type, String name);
    VarDepNode parseVarNode(Parameter parameter);
    VarDepNode parseVarNode(Field field);

}
