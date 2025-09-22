package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengReportHandlerConfig;
import com.br.marketing.entity.XieChengReportHandlerConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengReportHandlerConfigMapper extends XieChengReportHandlerConfigMapperBase{

    List<String> selectHandlerNameByBizForm(@Param("bizForm") String bizForm);

}