package com.br.marketing.mapper;


import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.PushDecisionsDetailVO;

import java.util.List;

public interface PushDecisionsMapper extends PushDecisionsMapperBase {

    @AddDataAuth
    List<PushDecisionsDetailVO> getDecisionsListBySearch(SearchConditionDTO dto);

}