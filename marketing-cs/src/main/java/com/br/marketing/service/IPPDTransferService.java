package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;

import java.time.LocalDate;

public interface IPPDTransferService {

    Result actionPPDToDx(String apiCode);

    /**
     * 2023-02-13 11:01
     * 推送的数据为转化数据，数据源为推送电销的记录表
     *
     * @param now 日期
     */
    long ppdaiOldPeriodicityPushDx(LocalDate now, String... apiCode);


}
