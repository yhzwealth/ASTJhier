package edu.fudan.selab.entity.node;

public abstract class HierarchyDepNode extends AbstractDepNode {

    public HierarchyDepNode(String type) {
        super(type);
        this.isResolved = false;
    }

}
