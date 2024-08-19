package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.vo.bi.param.ReportTaskParam;

import java.util.List;
import java.util.Map;

/**
 * 跑分模型分布 规则选择并保存任务记录
 * 
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-15
 */
public interface ReportScoreRuleService {

    /**
     * 产品集合列表
     * 
     * @param ids 跑分文件对应的主键id
     * @return
     */
    Map getProducts(String ids);

    /**
     * 新增 跑分模型报表任务 方法
     * 
     * @Author yu.xia@brgroup.com
     * @Date 2024/8/15 14:09
     * @param reportTaskParam
     * @return ApiResult<Boolean>
     */
    ApiResult<Boolean> addReportTask(ReportTaskParam reportTaskParam);

    /**
     * 获取报告任务列表
     *
     * @param current 电流
     * @param size 尺寸
     * @param apiCodes apiCodes
     * @return {@link PageResultReturn }
     * @author senyang.zheng
     * @date 2024/08/19
     */
    PageResultReturn getReportTaskList(int current, int size, List<String> apiCodes);
}
