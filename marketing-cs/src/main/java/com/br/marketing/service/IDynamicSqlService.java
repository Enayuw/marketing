package com.br.marketing.service;


import com.br.marketing.entity.MarketingSyncUser;

import java.util.List;

public interface IDynamicSqlService {
    /**
     * 获取生成任务的数据量
     * @param apiCode
     * @param whereStr
     * @param labelName
     * @return
     */
    Integer countByRuleScoreWithDate(String apiCode,String whereStr, String labelName);

    /**
     * 获取当前条件最小id
     * @param apiCode
     * @param whereStr
     * @param labelName
     * @return
     */
    Long minIdRuleScoreWithDate(String apiCode,String whereStr, String labelName);


    /**
     * 获取当前条件跑分数据
     * @param apiCode
     * @param whereStr
     * @param labelName
     * @return
     */
    List<MarketingSyncUser> selectDataRuleScoreWithDate(String apiCode, String whereStr,
                                                        Long id,Integer pageSize,String labelName,
                                                        Long minUnCompleteId,Long maxId);
}
