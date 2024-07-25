package com.br.marketing.rule.wuba;

import com.br.common.util.DateUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.wuba.WuBaSubmitConversionDataDto;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 58新客通话明细入库-3710155
 *
 * @Author lixiang
 * @Date 2024-07-23
 */
@Service
@Slf4j
public class WuBaCallRecordAddToDbFilter implements AssembleData<WuBaSubmitConversionDataDto> {

    @Override
    public WuBaSubmitConversionDataDto assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO bo = (CallRecordBO) transmitFact;
        WubaSubmitConversionData data = new WubaSubmitConversionData();
        data.setApiCode(bo.getApiCode());
        data.setLocalId(0L);
        data.setCell(bo.getCaseNum());
        Date callStartTime = bo.getDetail().getCallStartTime();
        String marketingTime = DateUtils.format(callStartTime, "yyyy-MM-dd HH:mm:ss");
        data.setMarketingTime(marketingTime);
        data.setPushStatus(0);
        data.setStatus(1);
        String curDate = DateUtils.format(new Date(), "yyyyMMdd");
        Integer createDate = Integer.parseInt(curDate);
        data.setCreateDate(createDate);

        WuBaSubmitConversionDataDto dto = new WuBaSubmitConversionDataDto();
        dto.setWubaSubmitConversionData(data);
        return dto;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        return true;
    }

    @Override
    public String label() {
        return "WuBa_CallRecord_Add_DB";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.WUBA_CALL_RECORD_ADD_DB.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
