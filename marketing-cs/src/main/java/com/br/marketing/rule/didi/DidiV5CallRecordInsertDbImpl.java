package com.br.marketing.rule.didi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.didi.DidiCallBackDataDTO;
import com.br.marketing.client.didi.input.DiDiReachBO;
import com.br.marketing.client.didi.input.DiDiReachRequestTO;
import com.br.marketing.client.didi.input.DiDiReqVO;
import com.br.marketing.client.didi.output.DiDiResponseTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.customer.CallRecordDetailBO;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.DidiCallRecord;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 *
 * @Author xiong.luo
 * @Date 2025-12-18
 */
@Service
@Slf4j
public class DidiV5CallRecordInsertDbImpl implements AssembleData<DidiCallBackDataDTO> {

    @Resource
    private CallRecordMapper callRecordMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public DidiCallBackDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO cbo = (CallRecordBO) transmitFact;
        DidiCallBackDataDTO callBackData = new DidiCallBackDataDTO();
        callBackData.setApiCode(cbo.getApiCode());
        callBackData.setCustNum(cbo.getCaseNum());
        callBackData.setCreateDate(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        callBackData.setStatus(0);
        callBackData.setPushStatus(0);
        callBackData.setCallbackType(1);
        callBackData.setIsConnect(cbo.getDetail().getIsConnect());
        callBackData.setPushType(Objects.equals(1, callBackData.getIsConnect()) ? 1 : 0);
        callBackData.setCreateTime(new Date());
        callBackData.setUpdateTime(callBackData.getCreateTime());
        List<CallRecord> callRecordList = callRecordMapper.getLastCallRecordByCustNum(
                Collections.singletonList(cbo.getCaseNum()), String.valueOf(cbo.getCid()));
        String key = "scas";
        CallRecord callRecord = callRecordList.get(0);
        String userProperties = callRecord.getUserProperties();
        if (StringUtils.isBlank(userProperties)) {
            callBackData.setScas(JSON.parseObject(cbo.getDetail().getUserProperties()).getString(key));
        } else {
            JSONObject userPropertiesObj = JSON.parseObject(userProperties);
            callBackData.setScas(userPropertiesObj.containsKey(key) ? userPropertiesObj.getString(key)
                    : JSON.parseObject(cbo.getDetail().getUserProperties()).getString(key));
        }
        callBackData.setExtend(JSON.toJSONString(cbo.getDetail()));
        String custNum = callBackData.getCustNum();
        String apiCode = callBackData.getApiCode();
        Map<String, SyncUserValidityPeriodsBO> validityPeriodsBOMap = transferDataValidityPeriodService
                .getValidityPeriodsByCustNum(Collections.singleton(custNum), apiCode, null);
        SyncUserValidityPeriodsBO bo = validityPeriodsBOMap.get(custNum);
        if (bo != null) {
            List<MarketingSyncUser> syncUsers = bo.getSyncUsers();
            callBackData.setCell(syncUsers.get(0).getCell());
        }
        return StringUtils.isBlank(callBackData.getCell()) ? null : callBackData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return transmitFact instanceof CallRecordBO;
    }

    @Override
    public String label() {
        return "Didi_CallRecord";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.DIDI_CALL_RECORD.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
