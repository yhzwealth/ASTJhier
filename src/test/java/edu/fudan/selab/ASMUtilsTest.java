package edu.fudan.selab;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import edu.fudan.selab.api.JtypeProvider;
import edu.fudan.selab.config.Global;
import edu.fudan.selab.entity.MethodGraph;
import edu.fudan.selab.utils.FileUtils;
import org.junit.Test;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.*;

import static edu.fudan.selab.config.Global.DECOMPILED_SOURCE_ROOT;

public class ASMUtilsTest {
    @Test
    public void changeSignatureToASM() throws ClassNotFoundException {
        String[] split = FileUtils.readFile("testMethod").split("\n");
        StringBuilder sb = new StringBuilder();
        Global.initByPkgJarPaths(List.of(DECOMPILED_SOURCE_ROOT), List.of());
        for (String s : split) {

            MethodGraph mg = JtypeProvider.v1.getMethodGraphBySignature(s);
            String json = JtypeProvider.v1.toJSONString(mg);
            JSONObject methodGraph = JSON.parseObject(json);

            String className = s.substring(0, s.indexOf("::")).replaceAll("\\.", "/");
            String methodName = s.substring(s.indexOf("::") + 2, s.indexOf("("));
            String fullyQualifiedClassName = methodGraph.getString("className");
            JSONObject parameters = methodGraph.getJSONObject("parameters");
            String signature = methodGraph.getString("methodName");
            Set<String> paramNames = parameters.keySet();
            HashMap<Integer, String> map = new HashMap<>();
            paramNames.forEach(n -> map.put(signature.indexOf(" " + n, signature.indexOf("(")), n));
            ArrayList<Integer> indexList = new ArrayList<>(map.keySet());
            Collections.sort(indexList);

            Class<?> clazz = Class.forName(fullyQualifiedClassName);
            Method[] method = clazz.getMethods();
            for (Method m : method) {
                if (!m.getName().equals(methodName)) continue;
                if (m.getParameterCount() != indexList.size()) continue;
                Class<?>[] parameterTypes = m.getParameterTypes();
                boolean flag = true;
                for (int i = 0; i < parameterTypes.length; i++) {
                    String paramName = map.get(indexList.get(i));
                    String paramType = parameters.getString(paramName);
                    if (signature.startsWith("...", indexList.get(i) - 3)) {
                        paramType += "[]";
                    }

                    if (!parameterTypes[i].getTypeName().equals(paramType)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    sb.append(className).append("#").append(methodName).append(Type.getMethodDescriptor(m)).append("\n");
                    break;
                }
            }
        }
        FileUtils.writeFile("ASMMethodSignature", sb.toString(), true);
    }

    @Test
    public void test() throws ClassNotFoundException, NoSuchMethodException {
        Class<?> clazz = Class.forName("org.joda.time.chrono.IslamicChronology");
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            System.out.println(method.getName());
            for (Class<?> parameterType : method.getParameterTypes()) {
                System.out.println(parameterType.getTypeName());
            }
            System.out.println(method.getName() + Type.getMethodDescriptor(method));
        }
    }

    @Test
    public void test2() {
        String[] split = FileUtils.readFile("testMethod").split("\n");
        for (int i = 0; i < split.length; i++) {
            if (split[i].charAt(0) >= '0' && split[i].charAt(0) <= '9') {
                int index = split[i].indexOf(' ');
                split[i] = split[i].substring(index + 1);
            }
        }
        FileUtils.writeFile("ASMMethodSignature", String.join("\n", split), false);
    }
}
