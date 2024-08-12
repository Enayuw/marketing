package com.br.marketing.service.rulecenter;

import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.ScoreSearchCondition;
import com.br.marketing.service.rulecenter.enums.RuleCenterDataSourceEnum;

import java.util.List;

/**
 * 筛选模板接口
 */
public interface IRuleCenterFilterTemplateService {

    /**
     * 数据源转化
     * @param sources
     * @return
     */
    String getSource(List<String> sources);


    /**
     * 规则筛选模板绑定数据源
     * @param main
     * @param scoreSearchCondition
     */
    void autoBuildSource(CustomerInfoPushMain main, ScoreSearchCondition scoreSearchCondition);


    /**
     * 规则筛选模板的实现标签
     * @return
     */
    RuleCenterDataSourceEnum sourceLabel();
}
