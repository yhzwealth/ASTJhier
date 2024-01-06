package edu.fudan.selab.entity.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassDepNode extends VarDepNode {

    public String classType;
    public HierarchyDepNode superClass;
    public List<HierarchyDepNode> subClass;
    public List<HierarchyDepNode> interfaces;
    public List<VarDepNode> fields;

    // key: the signature of constructor,
    // value: the DepNode of parameters.
    public Map<String, List<VarDepNode>> constructors;
    public Map<String, List<VarDepNode>> builders;

    public ClassDepNode(
            String type,
            String name,
            HierarchyDepNode superClass) {
        super(type, name);
        this.superClass = superClass;
        this.subClass = new ArrayList<>();
        this.interfaces = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.constructors = new HashMap<>();
        this.builders = new HashMap<>();
    }

}
