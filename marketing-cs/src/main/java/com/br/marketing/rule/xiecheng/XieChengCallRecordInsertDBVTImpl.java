package com.br.marketing.rule.xiecheng;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.XieChengDataDTO;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 通话明细推送携程(3710090/3710091)
 *
 * @author chenh
 * @dateTime 2023/09/15 16:50
 */
@Service
@Slf4j
public class XieChengCallRecordInsertDBVTImpl implements AssembleData<XieChengDataDTO> {

    @Override
    public XieChengDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO bo = (CallRecordBO) transmitFact;
        XieChengDataDTO xieChengDataDTO = new XieChengDataDTO();
        XieChengData xieChengData = new XieChengData();
        xieChengDataDTO.setXieChengData(xieChengData);
        xieChengData.setApiCode(bo.getApiCode());
        xieChengData.setActionType("IVR");
        xieChengDataDTO.setInitId(bo.getId());
        xieChengData.setSha256Tel(bo.getCaseNum());
        return xieChengDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MqFact mqFact = context.getMqFact();
        Integer isDelay = mqFact.getIsDelay();
        if (isDelay != null && isDelay == 1) {
            // 是延迟队列且有110且没有106
            return false;
        }
        return transmitFact instanceof CallRecordBO;
    }

    @Override
    public String label() {
        return "XieCheng_CallRecord_Insert_DB_VT";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.XIE_CHENG_CALL_RECORD_INSERT_DB.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

}
