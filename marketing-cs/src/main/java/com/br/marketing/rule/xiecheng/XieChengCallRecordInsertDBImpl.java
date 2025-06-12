package com.br.marketing.rule.xiecheng;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.XieChengDataDTO;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 通话明细推送携程(3710058/3710078)
 *
 * @author Guo Zeqiang
 * @dateTime 2022/12/1 16:50
 */
@Service
@Slf4j
public class XieChengCallRecordInsertDBImpl implements AssembleData<XieChengDataDTO> {

    private List<Integer> callStatusFail = Arrays.asList(13, 15);
    private List<Integer> callStatusIsBlack = Arrays.asList(12);

    @Resource
    private XieChengDataMapper xieChengDataMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;
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
        // 保存通话明细扩展字段
        xieChengData.setExtend(bo.getDetail().getUserProperties());
        return xieChengDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof CallRecordBO) {
            CallRecordBO callRecordBO = (CallRecordBO) transmitFact;
            if (callRecordBO.getDetail() != null && callStatusFail.contains(callRecordBO.getDetail().getCallStatus())) {
                keepRecord(callRecordBO, String.format("CallStatus状态是：%d", callRecordBO.getDetail().getCallStatus()));
                return false;
            }
            if (callRecordBO.getDetail() != null && callStatusIsBlack.contains(callRecordBO.getDetail().getCallStatus())) {
                String tcid = tableCreateService.getTcId(callRecordBO.getApiCode());
                Map<String, JSONObject> pushCondition = marketingCommonConfig.getXieChengCallPushCondition();
                JSONObject condition = pushCondition.get(callRecordBO.getApiCode());
                JSONArray isBlackApiCodes = condition.getJSONArray("isBlackApiCodes");
                MarketingTransferSyncUser xcTransferTodayNoAdDataByOnlyBlack = marketingTransferSyncUserMapper.getXcTransferTodayNoAdDataByOnlyBlack(
                        tcid, callRecordBO.getCaseNum(), isBlackApiCodes);
                if (xcTransferTodayNoAdDataByOnlyBlack != null) {
                    keepRecord(callRecordBO, String.format("CallStatus状态是：%d,且当天转化isBlack='1'", callRecordBO.getDetail().getCallStatus()));
                    return false;
                }
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

    private void keepRecord(CallRecordBO bo, String errorMsg) {
        XieChengData xieChengData = new XieChengData();
        xieChengData.setApiCode(bo.getApiCode());
        xieChengData.setActionType("IVR");
        xieChengData.setSha256Tel(bo.getCaseNum());
        xieChengData.setCreateTime(new Date());
        xieChengData.setCreateDate(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
        xieChengData.setLocalId(bo.getId());
        xieChengData.setPushStatus(1);
        xieChengData.setType("1");
        xieChengData.setDataMessage(errorMsg);
        xieChengData.setStatus(2);
        xieChengDataMapper.insertSelective(xieChengData);
    }
}

