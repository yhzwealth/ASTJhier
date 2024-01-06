package edu.fudan.selab.mapper;

import java.util.List;

public interface InitMapper{
    void createExtendedTable();
    void createFieldTable();
    void createImplementedTable();
    void createMethodTable();
    void createParameterTable();
    void createTypeTable();
    void setLike();
}
