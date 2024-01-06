package edu.fudan.selab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.fudan.selab.entity.pojo.Type;

import java.util.List;

public interface TypeMapper extends BaseMapper<Type> {
    Type getOneByIdType(Integer id);
    void insertAll(List<Type> list);
}
