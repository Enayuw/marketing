package com.br.marketing.mapper;

import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.DataExportTaskVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataExportTaskMapper extends DataExportTaskMapperBase {

    @AddDataAuth
    List<DataExportTaskVO> getDataExportTaskList(@Param("taskName") String taskName, 
                                                  @Param("dataSource") String dataSource, 
                                                  @Param("status") Integer status);

    List<DataExportTaskVO> getActiveTasksByDataSource(@Param("dataSource") String dataSource);
} 