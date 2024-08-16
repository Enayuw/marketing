package com.br.marketing.mapper;


import com.br.marketing.entity.ReportTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportTaskMapper extends ReportTaskMapperBase{

    List<ReportTask> findList(@Param("name") String name);
}