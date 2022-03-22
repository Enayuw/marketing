package com.br.marketing.context.impl;

import com.br.marketing.context.AbstractRuleCollectDataService;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description :
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/22 13:51
 */
@Service
public class ShuHeRuleCollectDataImpl implements AbstractRuleCollectDataService {


    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        ShuHeRuleNecessaryData shuHeRuleNecessaryData = new ShuHeRuleNecessaryData();
        shuHeRuleNecessaryData.setCreatTime(new Date());
        context.setRuleNecessaryData(shuHeRuleNecessaryData);
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.SHU_HE_RULE_DATA_COLLECTION;
    }


    @Data
    public class ShuHeRuleNecessaryData extends RuleNecessaryData {
        /**
         * 上传数据创建时间
         */
        private Date creatTime;
    }
}
