package edu.fudan.selab.entity.node;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: resolve
 */
public class InterfaceHierarchyDepNode extends HierarchyDepNode {
    public List<HierarchyDepNode> superInterface;
    public List<HierarchyDepNode> subInterface;
    public List<HierarchyDepNode> implementedClass;

    public InterfaceHierarchyDepNode(String type) {
        super(type);
        superInterface = new ArrayList<>();
        subInterface = new ArrayList<>();
        implementedClass = new ArrayList<>();
    }

    public void tryToResolve() {
        super.tryToResolve();
    }
}
