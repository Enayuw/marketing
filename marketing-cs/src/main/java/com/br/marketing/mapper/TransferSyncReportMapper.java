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

    /**
     * 自动化巡检：转化场景 - 前一天08:00快照（按 update_time &lt;= yesterday 08:00 取每个 api_code 最新一条）。
     *
     * <p>入参 {@code apiCodeList} 为空/为 null 时，XML 会走兜底条件（{@code 1=0}）返回空集，
     * 以避免生成 {@code IN ()} 语法错误或误查全表。</p>
     */
    List<CheckTransferSyncDataDto> getLastDay8DataByApiCodes(@Param("apiCodeList") List<String> apiCodeList);

    /**
     * 自动化巡检：转化场景 - 最新快照（取每个 api_code 最新一条）。
     *
     * <p>入参 {@code apiCodeList} 为空/为 null 时，XML 会走兜底条件（{@code 1=0}）返回空集，
     * 以避免生成 {@code IN ()} 语法错误或误查全表。</p>
     */
    List<CheckTransferSyncDataDto> getLatestDataByApiCodes(@Param("apiCodeList") List<String> apiCodeList);
}
