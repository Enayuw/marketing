package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingSyncUser;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IMarketingDataValidService {

    /**
     * 获取指定规则的有效期配置
     *
     * @param apiCode
     * @param validType 配置类型1-区间范围；2-T+N;3-场景和T+N
     * @return 有配置则code返回1，没有则是0
     */
    Result<List<MarketingDataValidConfig>> getDataValidConfigByType(String apiCode, Integer validType);


    /**
     * 根据场景和T+N的有效期配置 判断数据是否有效
     *
     * @param userTypeTN 配置 “场景”：“有效天数”
     * @param syncUser   待运营数据
     * @return
     */
    Boolean isValidByThreeType(Map<String, Integer> userTypeTN, MarketingSyncUser syncUser);


    /**
     * 2024-08-09 15:14
     * 获取有效期内的上传日期
     *
     * @param apiCode
     * @param dateStr 日期，格式：yyyy-MM-dd
     * @return 上传日期集合，格式：yyyy-MM-dd
     */
    Set<String> getAppletDateSet(String apiCode, String dateStr);

}
