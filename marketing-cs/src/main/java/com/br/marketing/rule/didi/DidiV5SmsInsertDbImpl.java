package com.br.marketing.rule.didi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.didi.DidiCallBackDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.SmsCallBackBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public DidiCallBackDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        SmsCallBackBO cbo = (SmsCallBackBO) transmitFact;
        DidiCallBackDataDTO didiCallRecord = new DidiCallBackDataDTO();
        didiCallRecord.setCustNum(cbo.getCaseNum());
        didiCallRecord.setApiCode(cbo.getApiCode());
        didiCallRecord.setStatus(0);
        didiCallRecord.setPushStatus(0);
        didiCallRecord.setCallbackType(2);
        didiCallRecord.setSmsSendStatus(cbo.getSmsSendStatus());
        didiCallRecord.setCreateDate(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        didiCallRecord.setCreateTime(new Date());
        didiCallRecord.setUpdateTime(didiCallRecord.getCreateTime());
        didiCallRecord.setExtend(JSON.toJSONString(cbo));
        String custNum = didiCallRecord.getCustNum();
        String apiCode = didiCallRecord.getApiCode();
        Map<String, SyncUserValidityPeriodsBO> validityPeriodsBOMap = transferDataValidityPeriodService
                .getValidityPeriodsByCustNum(Collections.singleton(custNum), apiCode, null);
        SyncUserValidityPeriodsBO bo = validityPeriodsBOMap.get(custNum);
        if (bo != null) {
            List<MarketingSyncUser> syncUsers = bo.getSyncUsers();
            didiCallRecord.setCell(syncUsers.get(0).getCell());
            JSONObject jsonObject = JSON.parseObject(syncUsers.get(0).getReserveField1());
            didiCallRecord.setScas(jsonObject.getString("scas"));
        }
        return StringUtils.isBlank(didiCallRecord.getCell()) ? null : didiCallRecord;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return transmitFact instanceof SmsCallBackBO;
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
