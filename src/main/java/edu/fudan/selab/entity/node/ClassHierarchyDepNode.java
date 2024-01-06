package edu.fudan.selab.entity.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: resolve
 */
public class ClassHierarchyDepNode extends HierarchyDepNode {

    public String classType;
    public HierarchyDepNode superClass;
    public List<HierarchyDepNode> subClass;
    public List<HierarchyDepNode> interfaces;
    public List<VarDepNode> fields;

    // key: the signature of constructor,
    // value: the DepNode of parameters.
    public Map<String, List<VarDepNode>> constructors;
    public Map<String, List<VarDepNode>> builders;

    public ClassHierarchyDepNode(String type, HierarchyDepNode superClass) {
        super(type);
        this.superClass = superClass;
        this.subClass = new ArrayList<>();
        this.interfaces = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.constructors = new HashMap<>();
        this.builders = new HashMap<>();
    }

    public void tryToResolve() {
        super.tryToResolve();
    }

}
