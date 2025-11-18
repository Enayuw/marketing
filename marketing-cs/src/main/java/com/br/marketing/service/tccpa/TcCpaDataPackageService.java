package com.br.marketing.service.tccpa;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;

public interface TcCpaDataPackageService {

    /**
     * 规则中心 同程CPA跑分待清洗数据包生成
     * @param dto
     * @return
     */
    Result tcDataPackageGen(TcCpDataPackageGenDTO dto);
}
