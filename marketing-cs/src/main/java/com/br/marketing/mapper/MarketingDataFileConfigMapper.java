package com.br.marketing.mapper;

import com.br.marketing.vo.dataclean.DataCleanConfigVO;
import com.br.marketing.vo.dataclean.DataCleanTaskVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingDataFileConfigMapper extends MarketingDataFileConfigMapperBase {


    List<DataCleanConfigVO> getList(@Param("apiCode")String apiCode, @Param("fileType")String fileType);







}