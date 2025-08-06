package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.vo.ScoreDistRuleVo;
import com.br.marketing.vo.bi.AxisWrapVO;

import javax.validation.Valid;
import java.util.List;

/**
 * 评分分布规则模板接口
 */
public interface ScoreDistRuleService {

    /**
     * 列表查询
     * @param dto
     * @return
     */
    Result<PageResultReturn<ScoreDistRuleVo>> getScoreDistRuleList(@Valid SearchConditionDTO dto);

    /**
     * 详情查询
     * @param configId
     * @return
     */
    List<AxisWrapVO> getScoreDistRuleDetail(Long configId);

    Result forbScoreDistRule(Long configId);

    Result enableScoreDistRule(Long configId);

    Result deleteScoreDistRule(Long configId);
}
