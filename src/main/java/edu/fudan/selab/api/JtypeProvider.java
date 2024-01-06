package edu.fudan.selab.api;

import edu.fudan.selab.config.Generator;
import edu.fudan.selab.config.Global;
import edu.fudan.selab.entity.MethodGraph;
import edu.fudan.selab.service.MethodService;
import edu.fudan.selab.utils.JsonUtils;

@SuppressWarnings("unused")
public class JtypeProvider {

    public static class v1 {

        /**
         * Exposed API
         * Used to initialize the llm-jtype-provider project,
         * setup the environment for llm-seed-generator.
         * @param jarGAV GAV of a jar, see {@link Global#initBySingleJarGAV(String)}
         */
        @SuppressWarnings("unused")
        public static void initialize(String jarGAV) {
            Global.initBySingleJarGAV(jarGAV);
        }

        /**
         * Exposed API
         * Used to get Method Graph via method signature.
         * see {@link MethodService#handleMethodBySignature(String, String)}
         * @param signature a method signature, its format is like that in jazzer-autofuzz.
         *                  e.g. com.google.json.JsonSanitizer::sanitize(Integer, String)
         * @return Method Graph
         * @throws IllegalArgumentException when format is not valid
         */
        @SuppressWarnings("unused")
        public static MethodGraph getMethodGraphBySignature(String signature)
        throws IllegalArgumentException {
            return MethodService.handleMethodBySignature(
                    Global.DECOMPILED_SOURCE_ROOT, signature);
        }

        @SuppressWarnings("unused")
        public static MethodGraph getMethodGraphBySignature(String signature, Integer level)
                throws IllegalArgumentException {
            Generator.GENERATOR_LEVEL = level;
            MethodGraph mg = v1.getMethodGraphBySignature(signature);
            Generator.GENERATOR_LEVEL = Generator.GENERATOR_LEVEL_DEFAULT;
            return mg;
        }

        @SuppressWarnings("unused")
        public static String toJSONString(MethodGraph mg) {
            return JsonUtils.toJSONString(mg);
        }

        public static String toJSONString(MethodGraph mg, Integer level) {
            Generator.GENERATOR_LEVEL = level;
            String json = JsonUtils.toJSONString(mg);
            Generator.GENERATOR_LEVEL = Generator.GENERATOR_LEVEL_DEFAULT;
            return json;
        }
    }
}
