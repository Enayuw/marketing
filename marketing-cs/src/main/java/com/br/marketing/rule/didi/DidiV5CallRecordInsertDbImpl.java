package com.br.marketing.rule.didi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.didi.DidiCallBackDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.customer.CallRecordDetailBO;
import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.DidiCallRecord;
import com.br.marketing.mapper.CallRecordMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
public class DidiV5CallRecordInsertDbImpl implements AssembleData<DidiCallBackDataDTO> {

    @Resource
    private CallRecordMapper callRecordMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public DidiCallBackDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO cbo = (CallRecordBO) transmitFact;
        DidiCallBackDataDTO didiCallRecord = new DidiCallBackDataDTO();
        didiCallRecord.setCell(cbo.getCaseNum());
        didiCallRecord.setApiCode(cbo.getApiCode());
        didiCallRecord.setStatus(1);
        didiCallRecord.setCreateTime(new Date());
        didiCallRecord.setUpdateTime(didiCallRecord.getCreateTime());
        return didiCallRecord;
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
