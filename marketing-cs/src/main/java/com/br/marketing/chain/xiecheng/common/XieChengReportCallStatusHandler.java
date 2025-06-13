package com.br.marketing.chain.xiecheng.common;

import com.br.marketing.chain.xiecheng.AbstractXieChengReportHandler;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.HandlerStageEnum;
import com.br.marketing.enums.XieChengBizMarkEnum;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
public class XieChengReportCallStatusHandler extends AbstractXieChengReportHandler {

    private List<Integer> callStatusFail = Arrays.asList(13, 15);

    private List<Integer> callStatusIsBlack = Arrays.asList(12);

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Override
    public String process(XieChengReportContext context) {
        if (callStatusFail.contains(context.getCallRecord().getCallStatus())) {
            return String.format("CallStatus状态是：%d", context.getCallRecord().getCallStatus());
        }
        if(callStatusIsBlack.contains(context.getCallRecord().getCallStatus())){
            MarketingTransferSyncUser syncUser = marketingTransferSyncUserMapper
                    .getXcTransferTodayNoAdDataByOnlyBlack(context.getTcId(), context.getSha256Tel(), context.getPushConfig().getIsBlackApiCodes());
            if (syncUser != null) {
                return String.format("CallStatus状态是：%d,且当天转化isBlack='1'", context.getCallRecord().getCallStatus());
            }
        }
        return null;
    }

    protected XieChengReportCallStatusHandler() {
        super("xieChengReportCallStatus", XieChengBizMarkEnum.COMMON.name(), HandlerStageEnum.THREAD.name());
    }

}
