package com.br.marketing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.vo.bi.ReportTaskVO;

public interface ReportTaskMapper extends ReportTaskMapperBase {

    List<ReportTaskVO> findList(@Param("apiCodes") List<String> apiCodes);
}