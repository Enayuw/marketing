package com.br.marketing.mapper;

import com.br.marketing.entity.DidiCallbackDataLog;
import com.br.marketing.entity.DidiCallbackDataLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface DidiCallbackDataLogMapper extends DidiCallbackDataLogMapperBase{

    List<String> selectPushedCells(@Param("cellSet") Set<String> cellSet);

    List<String> selectSuccessPushedCells(@Param("cellSet") Set<String> cellSet);

}