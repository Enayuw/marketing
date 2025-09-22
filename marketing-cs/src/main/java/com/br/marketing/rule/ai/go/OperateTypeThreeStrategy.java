package com.br.marketing.rule.ai.go;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.AiToPolicyRecord;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.AiToPolicyRecordMapperBase;
import com.br.marketing.rule.common.CommonRuleLabelEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 操作类型3策略实现
 * 继承AiToPolicyBase，实现AiToPolicyOperationStrategy
 * 重写batchNumber生成和insertRecord逻辑
 * 
 * @author AI Assistant
 * @date 2024
 */
@Component
@Slf4j
public class OperateTypeThreeStrategy extends AiToPolicyBase {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    AiToPolicyRecordMapperBase aiToPolicyRecordMapperBase;

    @Override
    public String getOperationType() {
        return "3";
    }

    @Override
    public String generateBatchNumber(MarketingSyncUser syncUser) {
        String apiCode = syncUser.getApiCode();
        String appletDate = syncUser.getAppletDate().replace("-", "");
        String reserveField1 = syncUser.getReserveField1();
        JSONObject jsonObject = JSONObject.parseObject(reserveField1);
        
        List<String> apiCodeOfpushPolicy = marketingCommonConfig.getApiCodeOfpushPolicy();
        
        if (ObjectUtil.isNotEmpty(apiCodeOfpushPolicy) && apiCodeOfpushPolicy.contains(apiCode)) {
            return ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                    ? (appletDate + jsonObject.getString("batchNumber"))
                    : (appletDate + "_" + apiCode);
        } else {
            return ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                    ? jsonObject.getString("batchNumber")
                    : (appletDate + "_" + apiCode);
        }
    }

    @Override
    public boolean insertRecord(MarketingSyncUser syncUser) {
        AiToPolicyRecord aiToPolicyRecord = new AiToPolicyRecord();
        aiToPolicyRecord.setFingerprint(syncUser.getFingerprint());
        aiToPolicyRecord.setUserType(syncUser.getUserType());
        aiToPolicyRecord.setCustNum(syncUser.getCustNum());
        aiToPolicyRecord.setApiCode(syncUser.getApiCode());
        aiToPolicyRecord.setRuleLabel(CommonRuleLabelEnum.TO_POLICY_GENERAL.getCode());
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        aiToPolicyRecord.setCreateDate(Integer.valueOf(yyyyMMdd));
        
        try {
            aiToPolicyRecordMapperBase.insertSelective(aiToPolicyRecord);
            return true;
        } catch (DuplicateKeyException e) {
            log.warn("AI自动化推决策_操作类型3,数据重复，fingerprint:{}", syncUser.getFingerprint());
            return false;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(), e.getMessage(), 
                    "AI自动化推决策_操作类型3,写去重表db异常："), e);
            return true;
        }
    }
}