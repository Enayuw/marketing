package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.report.RefreshReportRequestDTO;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.vo.CustomerBatchNumVO;
import com.br.marketing.vo.ScoreDetailVo;
import com.br.marketing.vo.bi.IntervalTemplateVO;
import com.br.marketing.vo.bi.ReportTaskVO;
import com.br.marketing.vo.bi.param.BiReportStatisticTransferParam;
import com.br.marketing.vo.bi.param.BiReportTaskParam;
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
     * @param ids       跑分文件对应的主键id
     * @param fieldType
     * @return
     */
    Map getProducts(String ids, String fieldType);

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
     * @param current  电流
     * @param size     尺寸
     * @param name
     * @param apiCodes apiCodes
     * @return {@link PageResultReturn }
     * @author senyang.zheng
     * @date 2024/08/19
     */
    PageResultReturn getReportTaskList(int current, int size, String name, List<String> apiCodes);


    /**
     * 规则中心筛选批次列表
     *
     * @param batchNumVO vo
     * @return List<ScoreDetailVo>
     */
    PageResultReturn<List<ScoreDetailVo>> getBatchInfoList(CustomerBatchNumVO batchNumVO);

    /**
     * Bi报表列表查看（众安）
     * @param reportTaskParam
     * @return
     */
    PageResultReturn getBiReportTaskList(BiReportTaskParam reportTaskParam);

    /**
     * @description 根据评分分布名称和跑分文件id筛选评分分布列表
     * @param name
     * @param ids
     * @return List<ReportTaskVO>
     * @author hedongshuo
     * @date 2024/10/24 16:01
     **/
    List<ReportTaskVO> getReportTaskListForScore(String name, String ids);

    /**
     * 更新报表统计记录
     * @param param
     * @return
     */
    Boolean updateReportRecords(BiReportStatisticTransferParam param);

    ApiResult<Boolean> updateReportName(Long id, String reportName);

    ApiResult<Boolean> deleteReport(Long id);

    /**
     * 刷新自定义区间报表数据
     *
     * @param requestDTO 刷新请求参数
     */
    Result<Boolean> refreshCustomIntervalReport(RefreshReportRequestDTO requestDTO);

    Result<Boolean> saveIntervalTemplate(RefreshReportRequestDTO requestDTO, MarketingUserDetail user);

    Result<List<IntervalTemplateVO>> getIntervalTemplate(String apiCode);

    Result<String> getImageDistribution();
}
