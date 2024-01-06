package edu.fudan.selab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.fudan.selab.entity.pojo.Field;

import java.util.List;

public interface FieldMapper extends BaseMapper<Field> {
    void insertAll(List<Field> list);
}
