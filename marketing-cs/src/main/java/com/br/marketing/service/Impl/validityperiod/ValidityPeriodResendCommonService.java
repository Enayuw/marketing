package com.br.marketing.service.Impl.validityperiod;

import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.ValidityPeriodResendRecord;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.google.common.collect.Sets;

import shaded.com.google.common.base.Splitter;

public class ValidityPeriodResendCommonService {

    /**
     * 构建消息体
     *
     * @param info 信息
     * @param record 重推记录
     * @return {@link MqFact }
     * @author senyang.zheng
     * @date 2024/01/12
     */
    protected static MqFact buildMqFact(MarketingTransferInfo info, ValidityPeriodResendRecord record) {
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(info.getId());
        mqFact.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
        JSONObject resendData = JSONObject.parseObject(record.getResendData());
        if (resendData != null && StringUtils.isNotEmpty((resendData.getString("includeRules")))) {
            Set<String> includeRules = Sets.newHashSet(Splitter.on(",").splitToList(resendData.getString("includeRules")));
            mqFact.setIncludeRules(includeRules);
        }
        return mqFact;
    }
}
