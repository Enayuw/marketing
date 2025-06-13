package com.br.marketing.chain.xiecheng.cpa;

import com.br.common.log.AlertLog;
import com.br.marketing.chain.xiecheng.AbstractXieChengReportHandler;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.*;
import com.br.marketing.enums.HandlerStageEnum;
import com.br.marketing.enums.XieChengBizMarkEnum;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.XieChengCollidingDataLogMapper;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ValidityPeriodDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Slf4j
@Component
public class XieChengReportCollidingCpaHandler extends AbstractXieChengReportHandler {

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private ValidityPeriodDataService validityPeriodDataService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private XieChengCollidingDataLogMapper xieChengCollidingDataLogMapper;

    @Override
    public String process(XieChengReportContext context) {
        XieChengCollidingDataLog dataLog = xieChengCollidingDataLogMapper.selectlog(context.getSha256Tel());
        if (dataLog == null) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                    , "当前数据在日志表中未查到"));
            return "当前数据在日志表中未查到";
        }
        context.getAdReqDTO().setMktChannel(dataLog.getOrgChannel());
        return null;
    }

    protected XieChengReportCollidingCpaHandler() {
        super("xieChengReportCollidingCpa", XieChengBizMarkEnum.CPA.name(), HandlerStageEnum.THREAD.name());
    }
}
