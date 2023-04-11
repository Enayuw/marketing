package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;

public interface ICustomerConfigService {
    /**
     * 获取加密类型 1-MD5;2-SHA256;
     * @param apiCode
     * @return
     */
    Result<Integer> getEncryptyType(String apiCode);

    Result updateEncryptyType(String apiCode,Integer type);
}
