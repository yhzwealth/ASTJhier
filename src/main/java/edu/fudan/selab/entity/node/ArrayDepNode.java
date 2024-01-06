package edu.fudan.selab.entity.node;

public class ArrayDepNode extends VarDepNode{

    public HierarchyDepNode innerComponentType;
    public int dimension;
    public ArrayDepNode(String type, String name, HierarchyDepNode innerComponentType, int dimension) {
        super(type, name);
        this.innerComponentType = innerComponentType;
        this.dimension = dimension;
    }
}
