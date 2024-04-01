package com.br.marketing.service.Impl.xc;

import com.br.marketing.entity.XieChengStatisticsReport;

import java.util.List;

/**
 * 携程百万量级转化统计报表数据获取接口
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-03-29
 */
public interface XieChengStatisticsReporService  {

    /**
     * 查询数量并入库
     * @param apiCode apiCode
     * @param cid cid
     * @param requestData T-1
     * @return String
     */
    String getUploadCountAndInsert(String apiCode, Long cid, String requestData);

    /**
     *
     * @param apiCode apiCode
     * @param firstDayString firstDayString
     * @param requestData T-1
     * @return List<XieChengStatisticsReport>
     */
    List<XieChengStatisticsReport> queryStatisticsReporDate(String apiCode, String firstDayString, String requestData);

    /**
     *
     * @param xieChengStatisticsReports xieChengStatisticsReports
     * @param excelFilePath 路径
     * @param fileName 文件名
     */
    void createExcel(List<XieChengStatisticsReport> xieChengStatisticsReports, String excelFilePath, String fileName);
}
