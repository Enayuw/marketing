package com.br.marketing.mapper;

import com.br.marketing.vo.dataclean.DataCleanConfigVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MarketingDataFileConfigMapper extends MarketingDataFileConfigMapperBase{

    List<DataCleanConfigVO> getList(@Param("apiCode")String apiCode, @Param("fileType")String fileType);

    List<Map<String,Object>> selectCleanData(@Param("sql") String sql);

    void updateCleanDataStatus(@Param("tableName") String tableName,
                               @Param("status") String status,
                               @Param("autoDuplicateColumn") String autoDuplicateColumn,
                               @Param("list") Set<Object> list);

}