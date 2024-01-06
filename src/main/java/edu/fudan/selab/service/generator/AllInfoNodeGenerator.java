package edu.fudan.selab.service.generator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.fudan.selab.config.MBP;
import edu.fudan.selab.entity.node.ClassHierarchyDepNode;
import edu.fudan.selab.entity.pojo.Field;
import edu.fudan.selab.entity.pojo.Type;
import edu.fudan.selab.mapper.FieldMapper;

import java.util.List;

public class AllInfoNodeGenerator extends AbstractNodeGenerator {
    private static final FieldMapper fieldMapper = MBP.sqlSession.getMapper(FieldMapper.class);
    @Override
    public ClassHierarchyDepNode parseClassHierarchyDepNode(Type type) {
        ClassHierarchyDepNode node = super.parseClassHierarchyDepNode(type);
        List<Field> fieldList = fieldMapper.selectList(new LambdaQueryWrapper<Field>().eq(Field::getClassName, type.getFullyQualifiedClassName()));
        for (Field field : fieldList) {
            node.fields.add(parseVarNode(field));
        }
        return node;
    }

}
