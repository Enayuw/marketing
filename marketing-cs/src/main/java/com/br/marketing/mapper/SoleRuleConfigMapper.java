package com.br.marketing.mapper;

import com.br.marketing.dto.SoleRuleSearchDTO;
import com.br.marketing.entity.SoleRuleConfig;

import java.util.List;

public interface SoleRuleConfigMapper extends SoleRuleConfigMapperBase {
    List<SoleRuleConfig> selectList(SoleRuleSearchDTO dto);
}