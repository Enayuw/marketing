package com.br.marketing.service.tccpa;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
import com.br.marketing.dto.tccpa.TcyrCpaCollidingDataPackageVO;
import com.br.marketing.entity.TcyrCpaDeleteRule;

public interface TcCpaDataDeleteRuleService {

    /**
     * 规则中心 同程CPA跑分待清洗数据包生成
     * @param deleteRule
     * @return
     */
    Result tcDataPackageGen(TcyrCpaDeleteRule deleteRule);
}
