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

    void insertSelectiveBatch(List<PhoneSaleTransferInfo> list);

    void insertSelectiveBatch(List<PhoneSaleTransferInfo> list, int batchSize);

    /**
     * 获取案件编号集合
     */
    Set<String> findCusaNumList(Set<String> custNums, PhoneSaleTransferInfo info);

}
