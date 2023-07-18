package com.br.marketing.service;

import com.br.marketing.commonentity.PageResultReturn;

import java.util.Map;

/**
 * 客户上传数据统计报表
 *
 * @Author linquan.guo
 * @CreateDate 2021/11/18 14:47
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/11/18 14:47
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
public interface MarketingSyncReportService {

    void syncReportProcess(String uploadDate,String jobName);
    /**
     * 根据日期对上传数据进行报表统计
     *
     * @param uploadDate
     * @return
     */
    void syncReportProcess(String uploadDate);

    /**
     * 根据日期 和 apiCode 对上传数据进行报表统计
     *
     * @param uploadDate
     * @return
     */
    void syncReportProcessByApiCode(String uploadDate,String apiCode);


    /**
     * 客户上传数据统计报表列表
     * @param current
     * @param size
     * @param cidOrName
     * @param appletTimeStart
     * @param appletTimeEnd
     * @param apiCodes
     * @param userTypes
     * @return
     */
    PageResultReturn getReportList(int current, int size, String cidOrName, String appletTimeStart, String appletTimeEnd, String apiCodes, String userTypes);

    /**
     * 客户上传数据统计报表总计
     * @param cidOrName
     * @param appletTimeStart
     * @param appletTimeEnd
     * @param apiCodes
     * @param userTypes
     * @return
     */
    Map getReportListTotal(String cidOrName, String appletTimeStart, String appletTimeEnd, String apiCodes, String userTypes);


    /**
     * 根据上传日期和apicode进行删除统计
     * @param json
     */
    void deleteReportByAppletDate(String json);

}
