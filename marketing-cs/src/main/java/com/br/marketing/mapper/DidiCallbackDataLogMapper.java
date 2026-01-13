package com.br.marketing.mapper;

import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Param;

public interface DidiCallbackDataLogMapper extends DidiCallbackDataLogMapperBase{

    List<String> selectPushedCells(@Param("cellSet") Set<String> cellSet);

    List<String> selectSuccessPushedCells(@Param("cellSet") Set<String> cellSet);

    List<String> checkCellBatch(@Param("cellList") List<String> cellList);
}