package com.br.marketing.mapper;

import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.TransferFileTaskVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TransferFileTaskMapper extends TransferFileTaskMapperBase{

    @AddDataAuth
    List<TransferFileTaskVO> getTransferFileList(@Param("serach") String serach, @Param("startDateStart") String startDateStart, @Param("startDateEnd") String startDateEnd);

    int deleteMrpExtraTaskAction(
            @Param("mrpExtraTaskId") String mrpExtraTaskId,
            @Param("actionDate") String actionDate
    );
}