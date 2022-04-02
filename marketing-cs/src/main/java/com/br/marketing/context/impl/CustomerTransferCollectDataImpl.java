package com.br.marketing.context.impl;

import com.br.marketing.context.AbstractRuleCollectDataService;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 客服转化所需数据收集
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/29 14:45
 */
@Service
public class CustomerTransferCollectDataImpl implements AbstractRuleCollectDataService {

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        context.setRuleNecessaryData(new CustomerTransferNecessaryData());
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.CUSTOMER_TRANSFER_DATA_COLLECTION;
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class CustomerTransferNecessaryData extends RuleNecessaryData {
        /**
         * 0:非最后一次，1:最后一次
         */
        private String last;

        public CustomerTransferNecessaryData(String last) {
            this.last = last;
        }

        public CustomerTransferNecessaryData() {
        }
    }
}
