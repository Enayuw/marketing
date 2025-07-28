package com.br.marketing.mapper;

import com.br.marketing.dto.tag.MaterializedViewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TagDataRuleCalculateMapper extends TagDataRuleCalculateMapperBase {


    MaterializedViewDTO getMViewInfobI_(@Param("viewName")String viewName, @Param("database")String database);

    Integer getCountbI_(@Param("querySql")String querySql);


}
