package edu.fudan.selab.entity.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to represent the end of analysis.
 * 
 * If typesolver cannot find the type, create a NullHierarchyDepNode.
 */
public class NullHierarchyDepNode extends HierarchyDepNode {
    private static final Logger logger = LoggerFactory.getLogger(NullHierarchyDepNode.class);

    public NullHierarchyDepNode(String type) {
        super(type);
        logger.warn("Creating a NullDepNode. Type: " + type);
    }
}
