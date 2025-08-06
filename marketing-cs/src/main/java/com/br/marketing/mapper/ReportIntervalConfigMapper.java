package com.br.marketing.mapper;


import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.vo.ScoreDistRuleVo;

import java.util.List;

public interface ReportIntervalConfigMapper extends ReportIntervalConfigMapperBase {

    List<ScoreDistRuleVo> getScoreDistRuleList(SearchConditionDTO dto);
}