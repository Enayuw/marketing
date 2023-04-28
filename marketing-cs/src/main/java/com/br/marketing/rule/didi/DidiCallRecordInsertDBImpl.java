package com.br.marketing.rule.didi;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.DidiCallRecord;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 滴滴通话明细落库
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/4/26 10:01
 */
@Service
@Slf4j
public class DidiCallRecordInsertDBImpl implements AssembleData<DidiCallRecord> {
    @Override
    public DidiCallRecord assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO cbo = (CallRecordBO) transmitFact;
        DidiCallRecord didiCallRecord = new DidiCallRecord();
        didiCallRecord.setCustNum(cbo.getCaseNum());
        didiCallRecord.setApiCode(cbo.getApiCode());
        didiCallRecord.setCreateDate(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
        didiCallRecord.setStatus(0);
        didiCallRecord.setCreateTime(new Date());
        return didiCallRecord;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return transmitFact instanceof CallRecordBO;
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
