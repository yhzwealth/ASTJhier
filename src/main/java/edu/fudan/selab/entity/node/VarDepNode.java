package edu.fudan.selab.entity.node;

public class VarDepNode extends AbstractDepNode {
    public String name;

    public VarDepNode(String type, String name) {
        super(type);
        this.name = name;
    }
}
