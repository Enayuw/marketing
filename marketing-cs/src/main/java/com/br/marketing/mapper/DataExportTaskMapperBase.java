package com.br.marketing.mapper;

import com.br.marketing.entity.DataExportTask;
import com.br.marketing.entity.DataExportTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataExportTaskMapperBase {
    int countByExample(DataExportTaskExample example);

    int deleteByExample(DataExportTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DataExportTask record);

    int insertSelective(DataExportTask record);

    List<DataExportTask> selectByExample(DataExportTaskExample example);

    DataExportTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DataExportTask record, @Param("example") DataExportTaskExample example);

    int updateByExample(@Param("record") DataExportTask record, @Param("example") DataExportTaskExample example);

    int updateByPrimaryKeySelective(DataExportTask record);

    int updateByPrimaryKey(DataExportTask record);

    /**
     * 根据状态和文件名模板查询导出任务列表
     * @param status 状态
     * @param fileNameTemplate 文件名模板
     * @return 导出任务列表
     */
    List<DataExportTask> selectByStatusAndFileNameTemplate(@Param("status") Integer status, @Param("fileNameTemplate") String fileNameTemplate);
} 