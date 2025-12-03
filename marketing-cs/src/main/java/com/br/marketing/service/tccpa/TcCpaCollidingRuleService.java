package com.br.marketing.service.tccpa;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.tc.TcCpaCollidingRuleInfoDTO;
import com.br.marketing.dto.tc.TcCpaMagnitudeDistDTO;

import java.time.LocalDate;
import java.util.List;

public interface TcCpaCollidingRuleService {

    /**
     * @description 同程CPA撞库规则基础信息查询
     * @return com.br.marketing.common.commondto.Result
     * @author hedongshuo
     * @date 2025/12/2 16:36
     **/
    Result<TcCpaCollidingRuleInfoDTO> info();

    Result<List<TcCpaMagnitudeDistDTO>> magnitudeDist(List<String> releaseTimes);
}
