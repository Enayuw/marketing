package com.br.marketing.mapper;

import com.br.marketing.vo.dataclean.DataCleanTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MarketingCleanDataTaskMapper extends MarketingCleanDataTaskMapperBase{

    List<DataCleanTaskVO> getTaskList(@Param("apiCode")String apiCode, @Param("fileType")String fileType,@Param("status") String status);


    int updateMarketingCleanDataTaskById(@Param("id")Long id, @Param("cleanStatus") Integer cleanStatus);
}
