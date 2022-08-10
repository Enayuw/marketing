package com.br.marketing.service;

import com.br.marketing.entity.PhoneSaleTransferInfo;

import java.util.List;
import java.util.Set;

/**
 * 电销转化接口
 *
 * @author Guo Zeqiang
 * @dateTime 2022/7/14 20:13
 */
public interface PhoneSaleTransferInfoService {

    void insertBatch(List<PhoneSaleTransferInfo> list);

    void insertBatch(List<PhoneSaleTransferInfo> list, int batchSize);

    /**
     * 获取案件编号集合
     */
    Set<String> findCusaNumList(Set<String> custNums, PhoneSaleTransferInfo info);

}
