package edu.fudan.selab.entity;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class ClassInfo {
    String className;

    String superClassName;
    ClassOrInterfaceType superClassType;

    Map<String, ClassOrInterfaceType> interfaces;
    Map<String, MethodInfo> methods;
    Map<String, FieldInfo> fields;
    String classType = "class";

    public ClassInfo() {
        this.interfaces = new HashMap<>();
        this.fields = new HashMap<>();
    }
}
