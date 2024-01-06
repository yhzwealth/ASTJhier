package edu.fudan.selab.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Type implements Serializable{
    @TableId(type = IdType.INPUT)
    private String fullyQualifiedClassName;
    private String name;
    private String packageName;
    private Boolean isInterface;
    private Boolean isAbstract;
}
