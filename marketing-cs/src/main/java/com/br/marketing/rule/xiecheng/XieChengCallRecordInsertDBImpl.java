package com.br.marketing.rule.xiecheng;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.XieChengDataDTO;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 通话明细推送携程(3710058/3710078)
 *
 * @author Guo Zeqiang
 * @dateTime 2022/12/1 16:50
 */
@Service
@Slf4j
public class XieChengCallRecordInsertDBImpl implements AssembleData<XieChengDataDTO> {

    private List<Integer> callStatusFail = Arrays.asList(13,15);

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
        if(transmitFact instanceof CallRecordBO){
            CallRecordBO callRecordBO = (CallRecordBO) transmitFact;
            if(callStatusFail.contains(callRecordBO.getCaseStatus())){
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String label() {
        return "XieCheng_CallRecord_Insert_DB";
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
