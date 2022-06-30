package com.br.marketing.mapper;

import com.br.marketing.entity.TransferSyncReport;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper
 *
 * @author Guo Zeqiang
 * @dateTime 2022/6/28 19:12
 */
public interface TransferSyncReportMapper extends TransferSyncReportMapperBase {
    /**
     * 获取萨摩耶开始时间、结束时间、数据量
     * 2022/6/29 19:26
     */
    TransferSyncReport dateTimeMinMaxCountSMY(@Param("apiCode") String apiCode, @Param("dateStr") String dateStr
            , @Param("userType") String userType);

    /**
     * 获取开始时间、结束时间、数据量
     * 2022/6/29 19:26
     */
    TransferSyncReport dateTimeMinMaxCount(@Param("tCid") String tCid, @Param("apiCode") String apiCode
            , @Param("dateStr") String dateStr, @Param("userType") String userType);

}
