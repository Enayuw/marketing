package com.br.marketing.mapper;

import com.br.marketing.entity.HaierData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HaierDataMapper extends HaierDataMapperBase {

    List<HaierData> selectDataLimitId(@Param("day") Integer day, @Param("minId") Long minId);

    Long selectMinId(@Param("localId") Long localId);

    /**
     * 原生方法插入语句会导致拼接的 SQL 过大，出现执行不了的情况（异常）
     * 因此本方法最大支持1000，不满足不会执行插入操作
     */
    void insert1000Batch(@Param("datas") List<HaierData> datas);
}