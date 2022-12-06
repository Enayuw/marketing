package com.br.marketing.rule.xiecheng;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.XieChengDataDTO;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.SecureRandom;

/**
 * 通话明细推送携程
 *
 * @author Guo Zeqiang
 * @dateTime 2022/12/1 16:50
 */
@Service
@Slf4j
public class XieChengCallRecordInsertDBImpl implements AssembleData<XieChengDataDTO> {
    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;
    private final String[] factor = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
            , "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
            , "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};


    @Override
    public XieChengDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO bo = (CallRecordBO) transmitFact;
        XieChengDataDTO xieChengDataDTO = new XieChengDataDTO();
        XieChengData xieChengData = new XieChengData();
        xieChengDataDTO.setXieChengData(xieChengData);
        xieChengData.setApiCode(bo.getApiCode());
        xieChengData.setActionType("IVR");
        xieChengDataDTO.setInitId(bo.getId());
//        xieChengData.setSha256Tel(bo.getCaseNum());
        xieChengData.setClickId(System.currentTimeMillis()
                + randomAlphanumeric(5)
                + bo.getCaseNum());
        return null;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return true;
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

    /**
     * 2022-12-05 15:05
     * 生成任意长度的随机字母+随机数字
     */
    private String randomAlphanumeric(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder str = new StringBuilder();
        int len = factor.length;
        for (int i = 0; i < length; i++) {
            str.append(factor[random.nextInt(len)]);
        }
        return str.toString();
    }
}
