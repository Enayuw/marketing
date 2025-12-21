package com.br.marketing.rule.didi;

import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.didi.DidiCallBackDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.customer.CallRecordDetailBO;
import com.br.marketing.dto.customer.SmsCallBackBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 滴滴通话明细落库
 *
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/4/26 10:01
 */
@Service
@Slf4j
public class DidiV5SmsInsertDbImpl implements AssembleData<DidiCallBackDataDTO> {

    @Resource
    private CallRecordMapper callRecordMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public DidiCallBackDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        SmsCallBackBO cbo = (SmsCallBackBO) transmitFact;
        DidiCallBackDataDTO didiCallRecord = new DidiCallBackDataDTO();
        didiCallRecord.setCell(cbo.getCaseNum());
        didiCallRecord.setApiCode(cbo.getApiCode());
        didiCallRecord.setStatus(1);
        didiCallRecord.setCallbackType(2);
        didiCallRecord.setCreateTime(new Date());
        didiCallRecord.setUpdateTime(didiCallRecord.getCreateTime());
        return didiCallRecord;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (!(transmitFact instanceof SmsCallBackBO)) {
            return false;
        }

        SmsCallBackBO bo = (SmsCallBackBO) transmitFact;
        Set<String> custNums = new HashSet<>();
        custNums.add(bo.getCaseNum());

        Map<String, SyncUserValidityPeriodsBO> keyToSyncUserBO = transferDataValidityPeriodService
                .getValidityPeriodsByCustNumAndUserType(custNums, bo.getUserType(), bo.getApiCode(), new Date());

        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = keyToSyncUserBO.get(bo.getCaseNum());
//        if (syncUserValidityPeriodsBO == null) {
//            log.warn("滴滴短信明细回调, 未匹配到上传数据, caseNum: {}, userType: {}", bo.getCaseNum(), bo.getUserType());
//            return false;
//        }
//        List<MarketingSyncUser> syncUsers = syncUserValidityPeriodsBO.getSyncUsers();
//        if (syncUsers == null || syncUsers.isEmpty()) {
//            log.warn("滴滴短信明细回调, 未匹配到上传数据, caseNum: {}, userType: {}", bo.getCaseNum(), bo.getUserType());
//            return false;
//        }
        return true;
    }

    @Override
    public String label() {
        return "DIDI_SMS_CALLBACK";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.DIDI_SMS_CALLBACK.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
