package com.br.marketing.check.service;

import java.util.Map;

public interface ShuHeTransferService {


    void pushBlackDataToDaas();

    /**
     * 失效数据过来到Dass转化接口
     *
     * @param
     * @author Guo Zeqiang
     * @dateTime 2022/7/13 10:03
     */
    void invalidDataFilterToDaasTransfer(Map<String, Map<String, String>> typeMap);
}
