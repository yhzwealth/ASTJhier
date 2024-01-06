package edu.fudan.selab.entity;

import edu.fudan.selab.entity.node.HierarchyDepNode;
import edu.fudan.selab.entity.node.VarDepNode;
import edu.fudan.selab.entity.pojo.Type;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MethodGraph {
    private HierarchyDepNode returnNode;
    private String methodName;
    private List<VarDepNode> parametersNode;
    private String code;
    private boolean isStatic;
    private Type clazz;
}
