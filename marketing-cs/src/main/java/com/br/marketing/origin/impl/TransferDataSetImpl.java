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
        final JSONObject jsonObject = JSONObject.parseObject(mqFact.getMessage());
        String last = jsonObject.getString("last");
        final List<Long> ids = JSONObject.parseArray(jsonObject.getString("ids"), Long.class);
        if (CollectionUtils.isEmpty(ids)) {
            context.setRuleNecessaryData(new CustomerTransferCollectDataImpl.CustomerTransferNecessaryData(
                    last == null ? "1" : last));
            return Collections.emptyList();
        }
        final String tcId = jsonObject.getString("tcId");
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andIdIn(ids);
        example.settCid(tcId);
        List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
        context.setRuleNecessaryData(new CustomerTransferCollectDataImpl.CustomerTransferNecessaryData(
                last == null ? "0" : last));
        return new ArrayList<>(transferList);
    }

    @Override
    public TransferSource source() {
        return TransferSource.TRANSFER_DATA_SET_PROCESS;
    }

}
