package com.br.marketing.rule.ai.policy;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.AiToPolicyRecord;
import com.br.marketing.entity.AiToPolicyRecordExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.common.CommonRuleLabelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 操作类型5策略实现
 * 继承AbstractBaseAiToPolicy，实现AiToPolicyProcessor
 * 重写batchNumber生成、insertRecord
 */
@Component
@Slf4j
public class OperateFiveProcessor extends AbstractBaseAiToPolicy {

    @Autowired
    RedisChgService redisChgService;

    @Override
    public String getOperationType() {
        return "5";
    }

    @Override
    public String generateBatchNumber(MarketingSyncUser syncUser) {
        return syncUser.getReserveField2();
    }

    @Override
    public boolean insertRecord(MarketingSyncUser syncUser) {
        String lockValue = UUID.randomUUID().toString();
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        Integer createDate = Integer.valueOf(yyyyMMdd);
        String apiCode = syncUser.getApiCode();
        String userType = syncUser.getUserType();
        String custNum = syncUser.getCustNum();
        String key = RedisKeyConstant.AI_TOPOLICY_PUSH_COUNTER.concat(String.format("%s:%s:%s:%s:%s", yyyyMMdd, apiCode, userType,
                CommonRuleLabelEnum.AI_TO_POLICY_PATLOAN_OPERATYPE_FIVE.getCode(), custNum));
        String batchNumber;

        try {
            redisChgService.lock(key, lockValue);
            try {
                AiToPolicyRecordExample example = new AiToPolicyRecordExample();
                example.createCriteria().andCreateDateEqualTo(createDate)
                        .andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                        .andRuleLabelEqualTo(CommonRuleLabelEnum.AI_TO_POLICY_PATLOAN_OPERATYPE_FIVE.getCode())
                        .andCustNumEqualTo(custNum);
                int pushCount = aiToPolicyRecordMapperBase.countByExample(example) + 1;
                batchNumber = yyyyMMdd + "-" + apiCode + "-5" + "-" + userType + "-" + pushCount;

                AiToPolicyRecord aiToPolicyRecord = new AiToPolicyRecord();
                aiToPolicyRecord.setFingerprint(syncUser.getFingerprint());
                aiToPolicyRecord.setBatchNumber(batchNumber);
                aiToPolicyRecord.setApiCode(apiCode);
                aiToPolicyRecord.setUserType(userType);
                aiToPolicyRecord.setCustNum(custNum);
                aiToPolicyRecord.setRuleLabel(CommonRuleLabelEnum.AI_TO_POLICY_PATLOAN_OPERATYPE_FIVE.getCode());
                aiToPolicyRecord.setCreateDate(createDate);

                aiToPolicyRecordMapperBase.insertSelective(aiToPolicyRecord);
                syncUser.setReserveField2(batchNumber);
                return true;
            } catch (DuplicateKeyException e) {
                log.warn("AI自动化推决策_操作类型5,数据重复，fingerprint:{}", syncUser.getFingerprint());
                return false;
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(), e.getMessage(), "AI自动化推决策_操作类型5,写去重表db异常："), e);
                return true;
            }
        } catch (Exception e) {
            redisChgService.unlock(key, lockValue);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(), e.getMessage(),
                    "AI自动化推决策_操作类型5,redis加锁异常,需要手动处理,apiCode：" + syncUser.getApiCode() + ",明细表id：" + syncUser.getId() + "。"), e);
            return false;
        } finally {
            redisChgService.unlock(key, lockValue);
        }
    }
}