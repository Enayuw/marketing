package com.br.marketing.mapper;


import com.br.marketing.entity.ReportTaskScoreSource;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportTaskScoreSourceMapper extends ReportTaskScoreSourceMapperBase{

    void insertBatch(@Param("list") List<ReportTaskScoreSource> list);

}