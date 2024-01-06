package edu.fudan.selab.utils;

import edu.fudan.selab.config.Generator;
import edu.fudan.selab.entity.node.HierarchyDepNode;
import edu.fudan.selab.entity.node.VarDepNode;
import edu.fudan.selab.entity.pojo.Parameter;
import edu.fudan.selab.entity.pojo.Type;
import edu.fudan.selab.service.generator.AbstractNodeGenerator;
import edu.fudan.selab.service.generator.AllInfoNodeGenerator;
import edu.fudan.selab.service.generator.WithoutFieldNodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class NodeUtils {
    private static final Logger logger = LoggerFactory.getLogger(NodeUtils.class);

    private static final Map<Integer, AbstractNodeGenerator> generators = Map.of(
            1, new AllInfoNodeGenerator(),
            2, new WithoutFieldNodeGenerator()
    );

    /**
     * Use {@link Generator#GENERATOR_LEVEL}
     * if not existed => Use {@link Generator#GENERATOR_LEVEL_DEFAULT}
     * @return a generator instance
     */
    public static AbstractNodeGenerator generator() {
        if (NodeUtils.generators.containsKey(Generator.GENERATOR_LEVEL)) {
            return NodeUtils.generators.get(Generator.GENERATOR_LEVEL);
        }
        return NodeUtils.generators.get(Generator.GENERATOR_LEVEL_DEFAULT);
    }

    /**
     * Use level as key
     * if not existed => Use {@link NodeUtils#generator()}
     * @return a generator instance
     */
    public static AbstractNodeGenerator generator(Integer level) {
        if (NodeUtils.generators.containsKey(level)) {
            return generators.get(level);
        }
        return NodeUtils.generator();
    }

    public static HierarchyDepNode parseHierarchyNode(String fullyQualifiedClassName) {
        return generator().parseHierarchyNode(fullyQualifiedClassName);
    }

    public static VarDepNode parseVarNode(Parameter parameter) {
        return generator().parseVarNode(parameter);
    }

    public static VarDepNode parseVarNode(Type type) {
        return generator().parseVarNode(type, "");
    }

}
