package com.br.marketing.rule.ai.strategy;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.AiToPolicyRecord;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.AiToPolicyRecordMapperBase;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * 操作类型4策略实现
 * 继承AiToPolicyBase，实现AiToPolicyOperationStrategy
 * 重写batchNumber生成、insertRecord和字段映射逻辑
 * 
 * @author AI Assistant
 * @date 2024
 */
@Component
@Slf4j
public class OperateTypeFourStrategy extends AbstractBaseAiToPolicy {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    AiToPolicyRecordMapperBase aiToPolicyRecordMapperBase;

    @Override
    public String getOperationType() {
        return "4";
    }

    @Override
    public String generateBatchNumber(MarketingSyncUser syncUser) {
        String apiCode = syncUser.getApiCode();
        String appletDate = syncUser.getAppletDate().replace("-", "");
        String reserveField1 = syncUser.getReserveField1();
        JSONObject jsonObject = JSONObject.parseObject(reserveField1);
        
        String userType = syncUser.getUserType();
        return ObjectUtil.isNotEmpty(jsonObject.getString("batchNumber"))
                ? jsonObject.getString("batchNumber")
                : (appletDate + "_" + apiCode + "_" + userType);
    }

    @Override
    public boolean insertRecord(MarketingSyncUser syncUser) {
        AiToPolicyRecord aiToPolicyRecord = new AiToPolicyRecord();
        aiToPolicyRecord.setFingerprint(syncUser.getFingerprint());
        aiToPolicyRecord.setUserType(syncUser.getUserType());
        aiToPolicyRecord.setCustNum(syncUser.getCustNum());
        aiToPolicyRecord.setApiCode(syncUser.getApiCode());
        aiToPolicyRecord.setRuleLabel(getOperationType());
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        aiToPolicyRecord.setCreateDate(Integer.valueOf(yyyyMMdd));
        
        try {
            aiToPolicyRecordMapperBase.insertSelective(aiToPolicyRecord);
            return true;
        } catch (DuplicateKeyException e) {
            log.warn("AI自动化推决策_操作类型4,数据重复，fingerprint:{}", syncUser.getFingerprint());
            return false;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(), e.getMessage(), 
                    "AI自动化推决策_操作类型4,写去重表db异常："), e);
            return true;
        }
    }

    @Override
    protected void executeFieldMapping(ProcessHandlerContext context, JSONObject jsonObject) {
        HashMap<String, JSONObject> fieldKeyMapping = marketingCommonConfig.getFieldKeyMapping();
        JSONObject mapping = fieldKeyMapping.get(context.getApiCode());
        if (ObjectUtil.isNotEmpty(mapping)) {
            for (String s : mapping.keySet()) {
                String toKey = mapping.getString(s);
                String oldV = jsonObject.getString(toKey);
                String newV = jsonObject.getString(s);
                if (StringUtils.isBlank(oldV) && StringUtils.isNotBlank(newV)) {
                    jsonObject.put(toKey, newV);
                }
            }
        }
    }
}