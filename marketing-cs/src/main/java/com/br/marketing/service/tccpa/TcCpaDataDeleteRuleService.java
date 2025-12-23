package com.br.marketing.service.tccpa;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.vo.tccpa.TcyrCpaDeleteRuleVO;

public interface TcCpaDataDeleteRuleService {

    /**
     * 规则中心 同程CPA跑分待清洗数据包生成
     * @param ruleVO
     * @return
     */
    Result rule(TcyrCpaDeleteRuleVO ruleVO);

    /**
     *分页查询
     *
     * @param page
     * @param pageSize
     * @param deleteRuleName
     * @param enabled
     * @return
     */
    PageResultReturn<TcyrCpaDeleteRuleVO> page(int page, int pageSize, String deleteRuleName, Integer enabled);


    /**
     * 启用禁用
     *
     * @param id
     * @param enabled
     * @return
     */
    Result enable(Long id, Integer enabled);

    /**
     * 删除
     *
     * @param id
     * @return
     */
    Result delete(Long id);
}
