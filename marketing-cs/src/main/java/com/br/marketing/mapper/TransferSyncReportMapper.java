package com.br.marketing.mapper;

import com.br.marketing.entity.TransferSyncReport;
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
    TransferSyncReport dateTimeMinMaxCountSMY(@Param("apiCode") String apiCode, @Param("dateStr") String dateStr
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
    TransferSyncReport dateTimeMinMaxCount(@Param("tCid") String tCid, @Param("apiCode") String apiCode
            , @Param("dateStr") String dateStr, @Param("userType") String userType);

    /**
     * 转化数据统计报表列表
     * 2022/6/29 19:26
     *
     * @param params params
     * @return List
     */
    List<TransferSyncReportVO> selectList(Map<String, Object> params);

    /**
     * 转化数据统计报表总计
     * 2022/6/29 19:26
     *
     * @param params params
     * @return long
     */
    Long getReportListTotal(Map<String, Object> params);
}
