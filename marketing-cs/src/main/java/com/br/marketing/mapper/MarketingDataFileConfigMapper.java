package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataFileConfig;
import com.br.marketing.entity.MarketingDataFileConfigExample;
import com.br.marketing.vo.dataclean.DataCleanConfigVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MarketingDataFileConfigMapper extends MarketingDataFileConfigMapperBase{

    List<DataCleanConfigVO> getList(@Param("apiCode")String apiCode, @Param("fileType")String fileType);

    List<Map<String,Object>> selectCleanData(@Param("sql") String sql);

}