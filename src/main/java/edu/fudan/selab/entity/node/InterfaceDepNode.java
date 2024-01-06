package edu.fudan.selab.entity.node;

import java.util.List;

public class InterfaceDepNode extends VarDepNode {
    public List<HierarchyDepNode> superInterface;
    public List<HierarchyDepNode> subInterface;
    public List<HierarchyDepNode> implementedClass;

    public InterfaceDepNode(String type, String name) {
        super(type, name);
    }
}
