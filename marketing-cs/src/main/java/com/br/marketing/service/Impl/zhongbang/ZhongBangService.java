package com.br.marketing.service.Impl.zhongbang;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 众邦
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-25 18:34
 */
public interface ZhongBangService {

    /**
     * 2023-08-25 18:35
     * 转化数据推送daas与外呼
     */
    void pushTransferToDaasRealTimeUserOneAndCustomer(String apiCode, ThreadPoolExecutor threadPool, String... dateTimeStr);
}
