package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSaleTransferInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface PhoneSaleTransferInfoMapper extends PhoneSaleTransferInfoMapperBase {

    void insertBatch(@Param("list") List<PhoneSaleTransferInfo> list);

    /**
     * 获取案件编号集合
     */
    Set<String> findCusaNumList(@Param("custNums") Set<String> custNums, @Param("info") PhoneSaleTransferInfo info);


}