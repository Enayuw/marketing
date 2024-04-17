package com.br.marketing.service;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
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
     * 客户上传数据统计报表列表
     * @param cidOrName
     * @param appletTimeStart
     * @param appletTimeEnd
     * @param apiCodes
     * @param userTypes
     * @param cell
     * @param orderField
     * @param descField
     * @param request
     * @return
     */
    JSONArray getReportByCell(String cidOrName, String appletTimeStart, String appletTimeEnd
            , String apiCodes, String userTypes, String cell, String orderField, String descField
            , HttpServletRequest request);

    /**
     * 根据上传日期和apicode进行删除统计
     * @param json
     */
    void deleteReportByAppletDate(String json);

    /**
     * 修改有效期记录
     *
     * @param id
     * @param validStartDate
     * @param validEndDate
     * @return
     */
    boolean updateById(Long id, String validStartDate, String validEndDate);


    /**
     * 准实时数据计数碎片统计（上传）
     *
     * @param dataCountFragmentsMgs 碎片消息
     * @return 消费结果
     * @author Guo Zeqiang
     * @dateTime 2024-03-06 15:47
     */
    Result<Boolean> nearRealtimeDataCountFragmentsStatis(String dataCountFragmentsMgs);

}
