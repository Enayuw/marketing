package com.br.marketing.rule.zhongyuan;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.Impl.CaseUserServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

/**
 * 中原消金通话明细推送人工
 *
 * @author Guo Zeqiang
 * @dateTime 2023-06-08 16:44
 */
public class ZhongYuanCallRecordToDass implements AssembleData<RealTimeUserDataDTO> {

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    RedisChgService redisChgService;


    @Autowired
    CaseUserServiceImpl caseUserService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Override
    public RealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {

        return null;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        if (transmitFact instanceof CallRecordBO) {
            CallRecordBO bo = (CallRecordBO) transmitFact;
            String intentionGrade = bo.getDetail().getIntentionGrade();
            return StringUtils.isNotBlank(intentionGrade) && intentionGrade.contains("A");
        }
        return false;
    }

    @Override
    public String label() {
        return "ZhongYuan_CallRecordData_PhoneSale";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_LOG.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
