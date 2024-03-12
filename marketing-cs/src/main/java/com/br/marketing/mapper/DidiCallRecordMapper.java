package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.Set;

public interface DidiCallRecordMapper extends DidiCallRecordMapperBase {

    /**
     * 2023-08-08 9:28
     * 获取调用客户接口成功的案件编号（触达成功）
     * status '推送状态 0 待推送 1 成功  2 异常'
     *
     * @param apiCode    apiCode
     * @param createDate 创建日期，格式yyyyMMdd
     * @param custNumSet 案件编号集合
     * @return 案件编号`集合，已去重
     */
    Set<String> getCustNumByStatusIs1AndCellSet(@Param("apiCode") String apiCode
            , @Param("createDate") int createDate
            , @Param("custNumSet") Set<String> custNumSet);

}