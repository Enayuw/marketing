package com.br.marketing.mapper;


import com.br.marketing.entity.ReportIntervalModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportIntervalModelMapper extends ReportIntervalModelMapperBase {

    List<ReportIntervalModel> getByConfigId(@Param("configId")Long configId);

    int batchSaveIntervalModel(@Param("list")List<ReportIntervalModel> list);

}