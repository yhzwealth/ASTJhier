package edu.fudan.selab.entity.json;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class MethodJson {
    @JSONField(ordinal = 1)
    private String returnTypeName;
    @JSONField(ordinal = 2)
    private String className;
    @JSONField(ordinal = 3)
    private String methodName;
    @JSONField(ordinal = 4)
    private Map<String, String> parameters;
    @JSONField(ordinal = 5)
    private String code;
    @JSONField(ordinal = 6)
    private Map<String, NodeJson> nodes;
    @JSONField(ordinal = 7)
    private boolean isStatic;
}