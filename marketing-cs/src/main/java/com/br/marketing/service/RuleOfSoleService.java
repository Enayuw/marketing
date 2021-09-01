package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.SoleRuleSearchDTO;
import com.br.marketing.vo.SoleRuleVO;

import java.util.List;

public interface RuleOfSoleService {

    /**
     * 查询去重规则列表
     * @param dto
     * @return
     */
    Result<List<SoleRuleVO>> list(SoleRuleSearchDTO dto);


}
