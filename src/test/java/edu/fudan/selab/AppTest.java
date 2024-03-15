package edu.fudan.selab;

import edu.fudan.selab.api.JtypeProvider;
import edu.fudan.selab.entity.MethodGraph;
import edu.fudan.selab.utils.FileUtils;
import org.junit.Test;


public class AppTest {
    @Test
    public void wholeProcessTest() {
        JtypeProvider.v1.initialize("net.time4j:time4j-base:5.9.3");
        MethodGraph mg1 = JtypeProvider.v1.getMethodGraphBySignature("com.github.javaparser.TokenRange::withBegin(JavaToken)");
        String json1 = JtypeProvider.v1.toJSONString(mg1);

        FileUtils.writeFile("graph.json", json1, false);
        assert json1.length() > 0;

    }
}