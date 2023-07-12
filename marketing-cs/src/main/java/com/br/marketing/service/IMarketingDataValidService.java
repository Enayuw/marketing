package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingSyncUser;

import java.util.List;
import java.util.Map;

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
     * 获取指定规则的有效期配置 新版
     *
     * @param apiCode 编码
     * @return 有配置则code返回1，没有则是0 key:userType+appletDate，value:MarketingDataValidConfig
     */
    Map<String, MarketingDataValidConfig> getDataValidConfig(String apiCode);


    /**
     * 根据场景和T+N的有效期配置 判断数据是否有效
     *
     * @param userTypeTN 配置 “场景”：“有效天数”
     * @param syncUser   待运营数据
     * @return
     */
    Boolean isValidByThreeType(Map<String, Integer> userTypeTN, MarketingSyncUser syncUser);

    /**
     * 根据场景和范围的有效期配置 判断数据是否有效
     *
     * @param validConfig 有效期配置
     * @param syncUser    待运营数据
     * @return true 无效，false有效
     */
    boolean isNotValid(MarketingDataValidConfig validConfig, MarketingSyncUser syncUser);

    /**
     * 根据场景和范围的有效期配置 判断数据是否有效
     *
     * @param validConfig 有效期配置
     * @param syncUser    待运营数据
     * @return true 有效，false 无效
     */
    boolean isValid(MarketingDataValidConfig validConfig, MarketingSyncUser syncUser);
}
