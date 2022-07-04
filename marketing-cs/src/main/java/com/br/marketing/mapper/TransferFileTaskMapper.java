package com.br.marketing.mapper;

import com.br.marketing.vo.TransferFileTaskVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TransferFileTaskMapper extends TransferFileTaskMapperBase{
    List<TransferFileTaskVO> getTransferFileList(@Param("serach") String serach, @Param("startDateStart") String startDateStart, @Param("startDateEnd") String startDateEnd);
}