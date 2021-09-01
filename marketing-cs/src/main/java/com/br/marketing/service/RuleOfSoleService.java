package com.br.marketing.service;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SoleRuleSearchDTO;

public interface RuleOfSoleService {

    /**
     * 查询去重规则列表
     * @param dto
     * @return
     */
    PageResultReturn list(SoleRuleSearchDTO dto, int page, int pageSize);

    /**
     * 判断规则名称是否重复
     * @param soleName
     * @return
     */
    boolean getNameOnly(String soleName);
}
