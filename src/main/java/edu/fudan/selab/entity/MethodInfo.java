package edu.fudan.selab.entity;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.Type;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class MethodInfo {
    private String modifier;
    private String methodName;
    private HashMap<String, Type> parameters;
    private Type returnType;
    private BlockStmt code;
    private Boolean isStatic;
    private Boolean isAbstract;
    private Boolean isConstructor;
    private Boolean isBuilder;
}