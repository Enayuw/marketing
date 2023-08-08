package com.br.marketing.context.impl;

import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.TransferDataValidityPeriodService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * D20230727同程自动化过滤-3710062（营销→外呼）
 * http://c.100credit.cn/pages/viewpage.action?pageId=119723248
 *
 * @author Guo Zeqiang
 * @dateTime 2023/8/1 14:23
 */
@Service
@Slf4j
public class TongChengCollectDataV2Impl extends CommonMethodHandlerService {

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    @SuppressWarnings(value = "all")
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            TongChengCollectDataV2Impl.TongChengRuleNecessaryData tongChengRuleNecessaryData
                    = new TongChengRuleNecessaryData();
            List<MarketingTransferSyncUser> transferList = (List<MarketingTransferSyncUser>) transmitFacts;
            // 删除不是2、3、5场景的转化数据，减少对有效期判断时数据量
            transferList.removeIf(t -> !"2".equals(t.getUserType())
                    && !"3".equals(t.getUserType()) && !"5".equals(t.getUserType()));
            // 获取有效期内的 案件编号+场景
            Map<String, SyncUserValidityPeriodBO> batchFirstVersion
                    = transferDataValidityPeriodService.getValidityPeriodUserTypeBatchFirstVersion(transferList
                    , context.getApiCode(), LocalDate.now().toString());
            tongChengRuleNecessaryData.setCustomerMap(batchFirstVersion);
            context.setRuleNecessaryData(tongChengRuleNecessaryData);
        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.TONG_CHENG_DATA_COLLECTION_V2;
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class TongChengRuleNecessaryData extends RuleNecessaryData {
        /**
         * 存在有效期的上传数据
         */
        private Map<String, SyncUserValidityPeriodBO> customerMap;
    }


}
