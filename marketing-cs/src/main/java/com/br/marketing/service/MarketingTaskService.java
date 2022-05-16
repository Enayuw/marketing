package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.TaskSelectSaveDTO;
import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.vo.CustomerScoreRuleVO;
import com.br.marketing.vo.FastTaskRuleDetailVO;
import com.br.marketing.vo.MarketingTaskVO;
import org.springframework.validation.annotation.Validated;

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
     * @param current
     * @param size
     * @param search
     * @param status
     * @param createTimeStart
     * @param createTimeEnd
     * @param updateTimeStart
     * @param updateTimeEnd
     * @param taskStatus
     * @return
     */
    PageResultReturn list(int current, int size, String search, Integer status, String createTimeStart, String createTimeEnd, String updateTimeStart, String updateTimeEnd, Integer taskStatus);

    ApiResult<Boolean> editPriority(String id, Integer priority);

    boolean updateStatusById(String id, Integer status);

    MarketingTaskVO getTask(String id);

    Integer getTaskPercent(String hisFileId,String id);

    List<ScoreRuleConfig> getScoreRules(String apiCode);

    void addTaskPercent(Long fileId,Long number);

    Result<Long> buildScoreTaskOfAuto(CustomerScoreRuleVO vo);

    Result<Long> buildScoreTaskOfSelect(CustomerScoreRuleVO vo);

    Result<List<Long>> saveTaskSelect(@Validated TaskSelectSaveDTO dto);
}
