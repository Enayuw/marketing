package com.br.marketing.mapper.eventtrack;


import com.br.marketing.entity.eventtrack.EventTrackingCellReportCount;
import com.br.marketing.entity.eventtrack.EventTrackingCellReportDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EventTrackingCellReportMapper extends EventTrackingCellReportMapperBase {

    /**
     * 查询【统计管理】【上传记录】手机号查询总量
     * @Author yu.xia@brgroup.com
     * @Date 2024/4/18 13:50
     * @param startTime
     * @param endTime
     * @param userNames
     * @param orderField
     * @param descField
     * @return List<EventTrackingCellReportCount>
     */
    List<EventTrackingCellReportCount> selectCellReportListtikv_(@Param("startTime") String startTime
            , @Param("endTime") String endTime
            , @Param("userNames") List<String> userNames, @Param("orderField") String orderField
            , @Param("descField") String descField);

    /**
     * 查询【统计管理】【上传记录】手机号查询详情
     * @Author yu.xia@brgroup.com
     * @Date 2024/4/18 13:50
     * @param startTime
     * @param endTime
     * @param userNames
     * @param apiCodes
     * @param orderField
     * @param descField
     * @return List<EventTrackingCellReportDetail>
     */
    List<EventTrackingCellReportDetail> selectCellReportDetailListtikv_(@Param("startTime") String startTime
            , @Param("endTime") String endTime
            , @Param("userNames") List<String> userNames, @Param("apiCodes") String apiCodes
            , @Param("orderField") String orderField, @Param("descField") String descField);
}