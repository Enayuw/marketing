package com.br.marketing.origin.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.impl.CustomerTransferCollectDataImpl;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.origin.TransferSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 转化数据集合
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/29 14:45
 */
@Service
public class TransferDataSetImpl implements OriginDataService {

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;


    @Override
    public List<Object> collect(MqFact mqFact, ProcessHandlerContext context) {
        /* 2022/3/31 14:47
         * message结构：
         * {"last": 0,"tcId": tcid,"ids": [id,id1],"apiCode": "code"}
         * eg:
         * {"last": 0,"tcId": 772,"ids": [607772,607771,607770,607769,607768],"apiCode": "7410430"}
         */
        final JSONObject jsonObject = JSONObject.parseObject(mqFact.getMessage());
        final String last = jsonObject.getString("last");
        final List<Long> ids = JSONObject.parseArray(jsonObject.getString("ids"), Long.class);
        final String apiCode = jsonObject.getString("apiCode");
        CustomerTransferCollectDataImpl.CustomerTransferNecessaryData ruleNecessaryData =
                (CustomerTransferCollectDataImpl.CustomerTransferNecessaryData) context.getRuleNecessaryData();
        if (CollectionUtils.isEmpty(ids)) {
            ruleNecessaryData.setLast(last == null ? "1" : last);
            context.setApiCode(apiCode == null ? "3710012" : apiCode);
            return Collections.emptyList();
        }
        final String tcId = jsonObject.getString("tcId");
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andIdIn(ids);
        example.settCid(tcId == null ? "14583" : tcId);
        List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
        ruleNecessaryData.setLast(last);
        context.setApiCode(apiCode == null ? transferList.size() > 0
                ? transferList.get(0).getApiCode() : "3710012" : apiCode);
        return new ArrayList<>(transferList);
    }

    @Override
    public TransferSource source() {
        return TransferSource.TRANSFER_DATA_SET_PROCESS;
    }

}
