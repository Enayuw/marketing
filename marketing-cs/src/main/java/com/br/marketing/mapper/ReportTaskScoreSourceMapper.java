package com.br.marketing.mapper;


import com.br.marketing.entity.ReportTaskScoreSource;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ReportTaskScoreSourceMapper extends ReportTaskScoreSourceMapperBase{

    void insertBatch(@Param("list") List<ReportTaskScoreSource> list);

    List<Map<String, Object>> selectReportIdByBatchNumberstikv_(@Param("batchNumbers")List<String> batchNumbers, @Param("size")int size);
}