package com.br.marketing.mapper;

import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.DataExportLogVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataExportLogMapper extends DataExportLogMapperBase {

    @AddDataAuth
    List<DataExportLogVO> getDataExportLogList(@Param("taskId") Long taskId, 
                                               @Param("batchNo") String batchNo, 
                                               @Param("executeStatus") Integer executeStatus,
                                               @Param("startTimeStart") String startTimeStart,
                                               @Param("startTimeEnd") String startTimeEnd);

    List<DataExportLogVO> getLogsByTaskId(@Param("taskId") Long taskId);

    int updateSftpStatus(@Param("id") Long id, @Param("sftpStatus") Integer sftpStatus);
} 