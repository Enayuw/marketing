package com.br.marketing.chain.xiecheng;

import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
public class XiChengReportBlackTHandler extends AbstractXieChengReportHandler{

    private List<Integer> callStatusFail = Arrays.asList(13, 15);

    private List<Integer> callStatusIsBlack = Arrays.asList(12);

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Override
    public void process(XieChengReportContext context) {
        if (callStatusFail.contains(context.getCallRecord().getCallStatus())) {
            context.setError(String.format("CallStatus状态是：%d", context.getCallRecord().getCallStatus()));
            return;
        }
        if(callStatusIsBlack.contains(context.getCallRecord().getCallStatus())){
            MarketingTransferSyncUser syncUser = marketingTransferSyncUserMapper
                    .getXcTransferTodayNoAdDataByOnlyBlack(context.getTcId(), context.getSha256Tel(), context.getCallRecord().getApiCode());
            if (syncUser != null) {
                context.setError(String.format("CallStatus状态是：%d,且当天转化isBlack='1'", context.getCallRecord().getCallStatus()));
            }
        }
    }

    protected XiChengReportBlackTHandler() {
        super(1, "common");
    }

}
