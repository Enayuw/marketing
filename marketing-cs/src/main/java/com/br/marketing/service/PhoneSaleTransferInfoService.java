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

    /**
     * 获取案件编号集合
     */
    List<String> findCusaNumList(Set<String> cusaNums, PhoneSaleTransferInfo info);

}
