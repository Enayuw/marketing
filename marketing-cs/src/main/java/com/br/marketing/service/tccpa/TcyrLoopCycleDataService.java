package com.br.marketing.service.tccpa;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.tccpa.TcyrCpaSuccessMqDTO;
import com.br.marketing.entity.MarketingTcyrCpaLoopCycle;
import com.br.marketing.service.Impl.xc.DataCollidingService;

public interface TcyrLoopCycleDataService {

    Result<Boolean> process(TcyrCpaSuccessMqDTO tcyrCpaSuccessMqDTO);
}
