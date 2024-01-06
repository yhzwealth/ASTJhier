package edu.fudan.selab.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Method implements Serializable {
    private String modifier;
    private String className;
    private String returnType;
    private String name;
    private String code;
    private Boolean isStatic;
    private Boolean isAbstract;
    private Boolean isConstructor;
    private Boolean isBuilder;
}
