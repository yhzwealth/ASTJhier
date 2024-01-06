package edu.fudan.selab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.fudan.selab.entity.pojo.Implemented;

import java.util.List;

public interface ImplementedMapper extends BaseMapper<Implemented>{
    void insertAll(List<Implemented> list);
}
