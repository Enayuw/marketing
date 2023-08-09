package com.br.marketing.context.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingSyncUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 中邮转化上下文处理
 * @author chenh
 * @dateTime 2023/8/9 9:45
 */
@Service
@Slf4j
public class ZhongYouRuleCollectDataImpl extends CommonMethodHandlerService {

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        context.setRuleNecessaryData(new ZhongYouRuleNecessaryData());
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.ZHONGYOU_DATA_COLLECTION;
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ZhongYouRuleNecessaryData extends RuleNecessaryData {
        /**
         * 2023-03-14 18:23
         * 生效截止时间 格式yyyy-mm-dd HH:mm:ss
         */
        private String expireDate;

        /**
         * 客户上传表信息
         */
        private MarketingSyncUser syncUser;
    }


}
