package com.br.marketing.mapper;


import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.vo.PushDecisionsDetailVO;

import java.util.List;

public interface PushDecisionsMapper extends PushDecisionsMapperBase {

    List<PushDecisionsDetailVO> getDecisionsListBySearch(SearchConditionDTO dto);

}