package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.OffLineCallBackDTO;
import com.br.marketing.dto.TaskSelectSaveDTO;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MarketingTaskResultPreview;
import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.vo.CustomerScoreRuleVO;
import com.br.marketing.vo.MarketingTaskVO;
import com.br.marketing.vo.ResultPreviewVO;
import com.br.marketing.vo.StatisticsDataDayVO;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 跑分任务接口类
 * @Date 2022/5/10 11:56 AM
 * ------------------------------
 */
public interface MarketingTaskService {

    /**
     * 跑分记录列表
     *
     * @param current
     * @param size
     * @param search
     * @param status
     * @param createTimeStart
     * @param createTimeEnd
     * @param updateTimeStart
     * @param updateTimeEnd
     * @param taskStatus
     * @param execType
     * @return
     */
    PageResultReturn list(int current, int size, String search, Integer status, String createTimeStart, String createTimeEnd,
                          String updateTimeStart, String updateTimeEnd, Integer taskStatus, Integer execType);

    ApiResult<Boolean> editPriority(String id, Integer priority);

    boolean updateStatusById(String id, Integer status);

    MarketingTaskVO getTask(String id);

    Long getTaskPercent(String hisFileId, String id);

    List<ScoreRuleConfig> getScoreRules(String apiCode);

    void addTaskPercent(Long fileId, Long number);

    Result<Long> buildScoreTaskOfAutoBuild(CustomerScoreRuleVO vo);
    Result<Long> buildScoreTaskOfAuto(CustomerScoreRuleVO vo);

    Result<Long> buildScoreTaskOfSelect(CustomerScoreRuleVO vo, List<String> userTypeList);

    Result<List<Long>> saveTaskSelectV2(@Valid TaskSelectSaveDTO dto);

    Result<List<Long>> saveTaskSelectByCreateMethod(TaskSelectSaveDTO dto)  throws Exception;

    Result<List<Long>> saveTaskSelect(@Valid TaskSelectSaveDTO dto);

    Result<List<StatisticsDataDayVO>> getStatisticsDataDay(@Valid @NotNull(message = "apiCode不能为空") String apiCode);

    Result<ResultPreviewVO> resultPreview(@Valid @NotNull(message = "taskId不能为空") Long tasId);

    void saveScoreResult(MarketingTaskResultPreview preview);

    Result offLineCallBack(@Valid OffLineCallBackDTO dto);

    Result delTask(Long id);

    Integer getPart(Integer sum,Long index);

    Integer getPart(Integer index);

    Integer getPartNum(Integer sum);

    Result buildCycleTaskBySelect(String startDate, String startTime, List<Long> syncReportIds, CustomerScoreRuleVO datum, String conditionInfo);

    void disableTask(MarketingTask task);
}
