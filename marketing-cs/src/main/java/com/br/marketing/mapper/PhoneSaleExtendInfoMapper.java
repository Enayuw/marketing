package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSaleExtendInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public  interface PhoneSaleExtendInfoMapper extends PhoneSaleExtendInfoMapperBase{

    /**
     * 批量插入
     * @param list
     */
    void saveBatch(@Param("list") List<PhoneSaleExtendInfo> list);

    /**
     * 批量更新
     * @param set
     */
    void updateBatch(@Param("set") Set<String> set);
}
