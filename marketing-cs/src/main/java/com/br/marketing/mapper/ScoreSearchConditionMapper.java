package com.br.marketing.mapper;

import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.vo.ScoreConditionDetailVO;

import java.util.List;

public interface ScoreSearchConditionMapper extends ScoreSearchConditionMapperBase {
    List<ScoreConditionDetailVO> getScoreListBySearch(SearchConditionDTO dto);
}