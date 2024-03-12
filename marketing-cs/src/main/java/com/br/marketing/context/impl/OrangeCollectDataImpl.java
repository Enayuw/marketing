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
 * 桔子转化上下文处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023/3/15 9:45
 */
@Service
@Slf4j
public class OrangeCollectDataImpl extends CommonMethodHandlerService {

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        context.setRuleNecessaryData(new OrangeRuleNecessaryData());
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.ORANGE_DATA_COLLECTION;
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class OrangeRuleNecessaryData extends RuleNecessaryData {
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
