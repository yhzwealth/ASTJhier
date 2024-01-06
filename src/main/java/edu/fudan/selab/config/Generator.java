package edu.fudan.selab.config;

import edu.fudan.selab.entity.node.HierarchyDepNode;

import java.util.HashMap;
import java.util.Map;

public class Generator {
    /**
     * Used to specify the level of information that DepNode-Generator uses.
     * 1 -> AllInformation;
     * 2 -> Without Fields;
     */
    public static Integer GENERATOR_LEVEL = 1;
    public static final Integer GENERATOR_LEVEL_DEFAULT = 1;

    public static class GeneratorCache {

        public static final Map<Integer, Map<String, HierarchyDepNode>> parsedHiNode = new HashMap<>();

        public static void put(Integer level, String name, HierarchyDepNode node) {
            if (!parsedHiNode.containsKey(level)) {
                parsedHiNode.put(level, new HashMap<>());
            }
            Map<String, HierarchyDepNode> m = parsedHiNode.get(level);
            m.put(name, node);
        }

        public static void put(String name, HierarchyDepNode node) {
            put(GENERATOR_LEVEL, name, node);
        }

        public static HierarchyDepNode get(Integer level, String name) {
            if (!parsedHiNode.containsKey(level)) {
                return null;
            }
            Map<String, HierarchyDepNode> m = parsedHiNode.get(level);
            if (!m.containsKey(name)) {
                return null;
            }
            return m.get(name);
        }

        public static HierarchyDepNode get(String name) {
            return get(GENERATOR_LEVEL, name);
        }
    }
}
