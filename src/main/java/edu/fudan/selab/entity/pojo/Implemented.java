package edu.fudan.selab.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Implemented implements Serializable {
    private String typeName;
    private String interfaceName;
}
