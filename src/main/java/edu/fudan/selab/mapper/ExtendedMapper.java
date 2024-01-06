package edu.fudan.selab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.fudan.selab.entity.pojo.Extended;

import java.util.List;

public interface ExtendedMapper extends BaseMapper<Extended> {
    void insertAll(List<Extended> list);
}
