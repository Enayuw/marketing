package com.br.marketing.rule.didi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.customer.CallRecordDetailBO;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.DidiCallRecord;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.rule.AssembleData;
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

/**
 * 滴滴通话明细落库
 *
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/4/26 10:01
 */
@Service
@Slf4j
public class DidiCallRecordInsertDBImpl implements AssembleData<DidiCallRecord> {

    @Resource
    private CallRecordMapper callRecordMapper;

    @Override
    public DidiCallRecord assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO cbo = (CallRecordBO) transmitFact;
        DidiCallRecord didiCallRecord = new DidiCallRecord();
        didiCallRecord.setCustNum(cbo.getCaseNum());
        didiCallRecord.setApiCode(cbo.getApiCode());
        didiCallRecord.setCreateDate(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
        didiCallRecord.setStatus(1);
        didiCallRecord.setCreateTime(new Date());
        didiCallRecord.setUpdateTime(didiCallRecord.getCreateTime());
        List<CallRecord> callRecordList = callRecordMapper.getLastCallRecordByCustNum(
                Collections.singletonList(cbo.getCaseNum()), String.valueOf(cbo.getCid()));
        String key = "scas";
        CallRecord callRecord = callRecordList.get(0);
        String userProperties = callRecord.getUserProperties();
        if (StringUtils.isBlank(userProperties)) {
            didiCallRecord.setScas(JSON.parseObject(cbo.getDetail().getUserProperties()).getString(key));
        } else {
            JSONObject userPropertiesObj = JSON.parseObject(userProperties);
            didiCallRecord.setScas(userPropertiesObj.containsKey(key) ? userPropertiesObj.getString(key)
                    : JSON.parseObject(cbo.getDetail().getUserProperties()).getString(key));
        }
        return StringUtils.isBlank(didiCallRecord.getScas()) ? null : didiCallRecord;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof CallRecordBO) {
            CallRecordBO cbo = (CallRecordBO) transmitFact;
            CallRecordDetailBO detail;
            Integer isConnect;
            return (detail = cbo.getDetail()) != null && (isConnect = detail.getIsConnect()) != null && isConnect == 1;
        }
        return false;
    }

    @Override
    public String label() {
        return "Didi_CallRecord_Insert_DB";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.DIDI_CALL_RECORD_INSERT_DB.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
