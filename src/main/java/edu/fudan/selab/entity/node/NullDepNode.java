package edu.fudan.selab.entity.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to represent the end of analysis.
 * 
 * If typesolver cannot find the type, create a NullDepNode.
 */
public class NullDepNode extends VarDepNode {
    private static final Logger logger = LoggerFactory.getLogger(NullDepNode.class);

    public NullDepNode(String type, String name) {
        super(type, name);
        logger.warn("Creating a NullDepNode. Type: " + type + "; Name: " + name);
    }
}
