package com.br.marketing.service.eventtrack;


import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.eventtrack.EventTrackingCellReport;

/**
 * 页面埋点 增、删、查 接口
 * @Author yu.xia@brgroup.com
 * @Date 2024/4/15 15:35
 */
public interface EventTrackService {

    /**
     * 异步写入数据库
     * @Author yu.xia@brgroup.com
     * @Date 2024/4/15 17:48
     * @param eventTrackingCellReport
     */
    void insertSync(EventTrackingCellReport eventTrackingCellReport);

    /**
     * 查询
     * @Author yu.xia@brgroup.com
     * @Date 2024/4/15 15:49
     * @param current
     * @param size
     * @param startTime
     * @param endTime
     * @param userName
     * @param orderField
     * @param descField
     * @return PageResultReturn
     */
    PageResultReturn getCellReport(int current, int size, String startTime, String endTime
            , String userName, String orderField, String descField);

    /**
     *
     * @Author yu.xia@brgroup.com
     * @Date 2024/4/15 15:49
     * @param current
     * @param size
     * @param startTime
     * @param endTime
     * @param userName
     * @param apiCodes
     * @param orderField
     * @param descField
     * @return PageResultReturn
     */
    PageResultReturn getCellReportDetail(int current, int size, String startTime, String endTime
            , String userName, String apiCodes, String orderField, String descField);

}
