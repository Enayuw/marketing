package com.br.marketing.mapper;

import com.br.marketing.dto.SoleRuleSearchDTO;
import com.br.marketing.vo.SoleRuleVO;

import java.util.List;

public interface SoleRuleConfigMapper extends SoleRuleConfigMapperBase {
    List<SoleRuleVO> selectList(SoleRuleSearchDTO dto);
}