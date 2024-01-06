package edu.fudan.selab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.fudan.selab.entity.pojo.Method;

import java.util.List;

public interface MethodMapper extends BaseMapper<Method> {
    void insertAll(List<Method> list);
    List<Method> selectRandomMethod();
}
