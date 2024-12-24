package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.OptConditionDTO;
import com.br.marketing.dto.PushDecisionsDTO;
import com.br.marketing.dto.RunTaskDTO;
import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.vo.PushDecisionsDetailVO;
import com.br.marketing.vo.ReachStrategyVO;
import com.br.marketing.vo.TaskTemplateVO;

import java.util.List;

/**
 * @ClassName PushDecisionsService
 * @Description TODO
 * @Author kongbx
 * @Date 2024/8/9 10:21
 */
public interface PushDecisionsService {
    Result<Long> savePushDecisions(PushDecisionsDTO dto);

    Result<Boolean> deletePushDecisions(Long id);

    Result<PageResultReturn<PushDecisionsDetailVO>> getPushDecisionsList(SearchConditionDTO dto);

    Result<PushDecisionsDetailVO> getPushDecisionsDetails(Long id);

    Result updateStatus(OptConditionDTO dto);

    Result<List<PushDecisionsDetailVO>> getDecisionsByRule(String apiCode);

    Result<Long> updatePushDecisions(PushDecisionsDTO dto);

    Result<List<TaskTemplateVO>> getRunTaskByTemplate(RunTaskDTO dto);

    Result<List<ReachStrategyVO>> getReachStrategyByApiCode(String apiCode);
}
