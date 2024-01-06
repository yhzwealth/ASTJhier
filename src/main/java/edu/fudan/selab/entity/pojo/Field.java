package edu.fudan.selab.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Field implements Serializable {
    private String className;
    private String modifier;
    private Boolean isStatic;
    private String typeName;
    private Integer level;
    private String name;
}
