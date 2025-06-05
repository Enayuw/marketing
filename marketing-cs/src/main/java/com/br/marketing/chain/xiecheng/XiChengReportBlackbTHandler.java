package com.br.marketing.chain.xiecheng;

import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class XiChengReportBlackbTHandler extends AbstractXieChengReportHandler{

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Override
    void process(XieChengReportContext context) {
        MarketingTransferSyncUser xcTransferBlack =
                marketingTransferSyncUserMapper.getXcTransferNoAdDataByOnlyBlack(
                        context.getTcId(), context.getSha256Tel(), context.getPushConfig().getIsBlackApiCodes());
        if (xcTransferBlack != null) {
            context.setError("命中黑名单");
        }
    }
    protected XiChengReportBlackbTHandler() {
        super(3, "common");
    }
}
