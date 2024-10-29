package com.br.marketing.rule.wuba;

import com.br.common.util.DateUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.dto.wuba.WuBaSubmitConversionDataDto;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    private static final String TITLE = "【58新客通话明细入库-3710155】";

    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Override
    public WuBaSubmitConversionDataDto assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO bo = (CallRecordBO) transmitFact;
        String apiCode = bo.getApiCode();
        String cell = bo.getCaseNum();

        MarketingSyncUser marketingSyncUser = marketingSyncUserMapper.findSyncUserByCustNumsAndAppletTime(apiCode, cell);
        if(marketingSyncUser == null){
            return null;
        }
        String userType = marketingSyncUser.getUserType();

        WubaSubmitConversionData data = new WubaSubmitConversionData();
        data.setApiCode(apiCode);
        data.setLocalId(0L);
        data.setCell(cell);
        data.setUserType(userType);

        Date callStartTime = bo.getDetail().getCallStartTime();
        if (callStartTime == null) {
            callStartTime = new Date();
        }
        String marketingTime;
        try {
            marketingTime = DateUtils.format(callStartTime, "yyyy-MM-dd HH:mm:ss");
        } catch(Exception e){
            log.warn(TITLE+"callStartTime格式不正确");
            marketingTime = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        }
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
        if (transmitFact instanceof CallRecordBO) {
            return true;
        }

        return false;
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
