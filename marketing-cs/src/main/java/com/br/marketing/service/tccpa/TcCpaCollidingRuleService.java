package com.br.marketing.service.tccpa;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tccpa.TcCpaCollidingRuleDTO;
import com.br.marketing.dto.tccpa.TcCpaCollidingRuleInfoDTO;
import com.br.marketing.dto.tccpa.TcCpaCollidingRuleQueryDTO;
import com.br.marketing.dto.tccpa.TcyrFailMsgSupplyGroupDTO;

import java.util.List;

public interface TcCpaCollidingRuleService {

    /**
     * @description 同程CPA撞库规则基础信息查询
     * @return com.br.marketing.common.commondto.Result
     * @author hedongshuo
     * @date 2025/12/2 16:36
     **/
    Result<TcCpaCollidingRuleInfoDTO> info();

    Result<List<TcyrFailMsgSupplyGroupDTO>> magnitudeDist(String releaseTimes, Long taskId);

    Result rule(TcCpaCollidingRuleDTO ruleDTO);

    PageResultReturn list(TcCpaCollidingRuleQueryDTO dto);

    Result update(TcCpaCollidingRuleDTO ruleDTO);

    Result enable(Long taskId, Integer enabled);

}
