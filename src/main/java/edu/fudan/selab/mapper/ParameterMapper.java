package edu.fudan.selab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.fudan.selab.entity.pojo.Parameter;

import java.util.List;

public interface ParameterMapper extends BaseMapper<Parameter> {
    void insertAll(List<Parameter> list);
}
