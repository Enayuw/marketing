package com.br.marketing.mapper;

import com.br.marketing.dto.autocheck.CheckTransferSyncDataDto;
import com.br.marketing.entity.TransferSyncReport;
import com.br.marketing.entity.TransferSyncReportExample;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.TransferSyncReportNumVO;
import com.br.marketing.vo.TransferSyncReportVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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
     *
     * @param apiCode  apiCode
     * @param dateStr  日期字符串
     * @param userType userType
     * @return TransferSyncReport
     */
    TransferSyncReport dateTimeMinMaxCountSMYtiflash_(@Param("apiCode") String apiCode, @Param("dateStr") String dateStr
            , @Param("userType") String userType);

    /**
     * 获取开始时间、结束时间、数据量
     * 2022/6/29 19:26
     *
     * @param tCid     tCid
     * @param apiCode  apiCode
     * @param dateStr  日期字符串
     * @param userType userType
     * @return TransferSyncReport
     */
    TransferSyncReport dateTimeMinMaxCounttiflash_(@Param("tCid") String tCid, @Param("apiCode") String apiCode
            , @Param("dateStr") String dateStr, @Param("userType") String userType);


    List<String> requestDatetikv_(@Param("tCid") String tCid, @Param("apiCode") String apiCode
            , @Param("startDate") String startDate,@Param("endDate") String endDate, @Param("userType") String userType);

    /**
     * 转化数据统计报表列表
     * 2022/6/29 19:26
     *
     * @param params params
     * @return List
     */
    @AddDataAuth
    List<TransferSyncReportVO> selectList(Map<String, Object> params);

    /**
     * 转化数据统计报表总计
     * 2022/6/29 19:26
     *
     * @param params params
     * @return List
     */
    @AddDataAuth
    List<TransferSyncReportNumVO> getReportListTotaltiflash_(Map<String, Object> params);


    /**
     * 2024-03-08 9:29
     * 获取数据量级
     *
     * @param example 条件
     * @return list
     */
    List<TransferSyncReport> selectNumberByExample(TransferSyncReportExample example);
}
