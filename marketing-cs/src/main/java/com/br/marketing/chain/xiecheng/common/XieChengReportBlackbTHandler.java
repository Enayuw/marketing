package com.br.marketing.chain.xiecheng.common;

import com.br.marketing.chain.xiecheng.AbstractXieChengReportHandler;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.HandlerStageEnum;
import com.br.marketing.enums.XieChengBizMarkEnum;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class XieChengReportBlackbTHandler extends AbstractXieChengReportHandler {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Override
    public String process(XieChengReportContext context) {
        MarketingTransferSyncUser xcTransferBlack =
                marketingTransferSyncUserMapper.getXcTransferNoAdDataByOnlyBlack(
                        context.getTcId(), context.getSha256Tel(), context.getPushConfig().getIsBlackApiCodes());
        if (xcTransferBlack != null) return "命中黑名单";
        return null;
    }
    protected XieChengReportBlackbTHandler() {
        super("xieChengReportBlackbT", XieChengBizMarkEnum.COMMON.name(), HandlerStageEnum.THREAD.name());
    }
}
