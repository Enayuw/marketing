package com.br.marketing.origin.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
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
        final List<Long> ids = JSONObject.parseArray(jsonObject.getString("ids"), Long.class);
        String last = jsonObject.getString("last");
        context.setLast(last);
        final String apiCode = jsonObject.getString("apiCode");
        if (CollectionUtils.isEmpty(ids)) {
            context.setApiCode(apiCode == null ? "3710012" : apiCode);
            return Collections.emptyList();
        }
        final String tcId = jsonObject.getString("tcId");
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.settCid(tcId == null ? "14583" : tcId);
        List<Object> list = new ArrayList<>();
        int size = ids.size();
        int pageSize = 2000;
        int pageCount = size % pageSize == 0 ? size / pageSize : size / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            example.createCriteria().andIdIn(ids.subList((i - 1) * pageSize, i == pageCount ? size : pageSize * i));
            list.addAll(marketingTransferSyncUserMapper.selectByExample(example));
        }
        context.setApiCode(apiCode == null ? list.size() > 0
                ? ((MarketingTransferSyncUser) list.get(0)).getApiCode() : "3710012" : apiCode);
        if (mqFact.getSourceId() != null){
            context.setTransferInfoId(mqFact.getSourceId());
        }
        return list;
    }

    @Override
    public TransferSource source() {
        return TransferSource.TRANSFER_DATA_SET_PROCESS;
    }

    @Override
    public List<Long> getIdList(List<Object> collect) {
        List<Long> idList = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser)collect.get(i);
            idList.add(transferSyncUser.getId());
        }
        return idList;
    }

}
