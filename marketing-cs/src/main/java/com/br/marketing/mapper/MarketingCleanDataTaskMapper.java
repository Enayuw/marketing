package com.br.marketing.mapper;

import com.br.marketing.vo.dataclean.DataCleanTaskVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MarketingCleanDataTaskMapper extends MarketingCleanDataTaskMapperBase{

    List<DataCleanTaskVO> getTaskList(String apiCode, String fileType, String status);


}
