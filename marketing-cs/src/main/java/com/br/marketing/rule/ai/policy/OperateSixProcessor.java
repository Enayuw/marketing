package com.br.marketing.rule.ai.policy;

import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.AiToPolicyRecord;
import com.br.marketing.entity.AiToPolicyRecordExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.AiToPolicyRecordMapperBase;
import com.br.marketing.rule.common.CommonRuleLabelEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 操作类型6策略实现
 * 继承AbstractBaseAiToPolicy，实现AiToPolicyProcessor
 * 重写batchNumber生成、insertRecord
 */
@Component
@Slf4j
public class OperateSixProcessor extends AbstractBaseAiToPolicy {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    AiToPolicyRecordMapperBase aiToPolicyRecordMapperBase;

    @Autowired
    RedisChgService redisChgService;

    @Override
    public String getOperationType() {
        return "6";
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
        String cell = syncUser.getCell();
        String key = RedisKeyConstant.AI_TOPOLICY_PUSH_COUNTER.concat(String.format("%s:%s:%s:%s:%s", yyyyMMdd, apiCode, userType,
                CommonRuleLabelEnum.AI_TO_POLICY_PATLOAN_OPERATYPE_SIX.getCode(), cell));
        String batchNumber;

        try {
            redisChgService.lock(key, lockValue);
            try {
                batchNumber = getOrGenerateBatchNumber(syncUser, createDate, apiCode,
                        userType, cell, yyyyMMdd);

                AiToPolicyRecord aiToPolicyRecord = new AiToPolicyRecord();
                aiToPolicyRecord.setFingerprint(syncUser.getFingerprint());
                aiToPolicyRecord.setBatchNumber(batchNumber);
                aiToPolicyRecord.setApiCode(apiCode);
                aiToPolicyRecord.setUserType(userType);
                aiToPolicyRecord.setCustNum(cell);
                aiToPolicyRecord.setRuleLabel(CommonRuleLabelEnum.AI_TO_POLICY_PATLOAN_OPERATYPE_SIX.getCode());
                aiToPolicyRecord.setCreateDate(createDate);

                aiToPolicyRecordMapperBase.insertSelective(aiToPolicyRecord);
                syncUser.setReserveField2(batchNumber);
                return true;
            } catch (DuplicateKeyException e) {
                log.warn("AI自动化推决策_操作类型6,数据重复，fingerprint:{}", syncUser.getFingerprint());
                return false;
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(), e.getMessage(), "AI自动化推决策_操作类型6,写去重表db异常："), e);
                return true;
            }
        } catch (Exception e) {
            redisChgService.unlock(key, lockValue);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DB_ERROR.getCode(), e.getMessage(),
                    "AI自动化推决策_操作类型6,redis加锁异常,需要手动处理,apiCode：" + syncUser.getApiCode() + ",明细表id：" + syncUser.getId() + "。"), e);
            return false;
        } finally {
            redisChgService.unlock(key, lockValue);
        }
    }

    /**
     * 获取或生成batchNumber
     * 判断是否是中原消金定制客户，如果是则使用reserveField1中的zyxj字段，否则生成新的batchNumber
     *
     * @param syncUser  同步用户对象
     * @param createDate 创建日期
     * @param apiCode   商户编号
     * @param userType  用户类型
     * @param cell      手机号
     * @param yyyyMMdd  日期字符串
     * @return batchNumber
     */
    private String getOrGenerateBatchNumber(MarketingSyncUser syncUser, Integer createDate, String apiCode,
                                            String userType, String cell, String yyyyMMdd) {
        // 判断是否是中原消金定制客户
        String reserveField1 = syncUser.getReserveField1();
        JSONObject jsonObject = JSONObject.parseObject(reserveField1);
        String zyxj = jsonObject.getString("zyxj");
        if (StringUtils.isEmpty(zyxj)) {
            // custNum临时存为cell的log加密
            AiToPolicyRecordExample example = new AiToPolicyRecordExample();
            example.createCriteria().andCreateDateEqualTo(createDate)
                    .andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                    .andRuleLabelEqualTo(CommonRuleLabelEnum.AI_TO_POLICY_PATLOAN_OPERATYPE_SIX.getCode())
                    .andCustNumEqualTo(cell);
            int pushCount = aiToPolicyRecordMapperBase.countByExample(example) + 1;
            return getBatchNumber(yyyyMMdd, apiCode, userType, pushCount);
        } else {
            return zyxj;
        }
    }

    /**
     * 生成batchNumber
     * @param yyyyMMdd
     * @param apiCode
     * @param userType
     * @param pushCount
     * @return
     */
    public String getBatchNumber(String yyyyMMdd, String apiCode, String userType, Integer pushCount) {
        return yyyyMMdd + "-" + apiCode + "-6" + "-" + userType + "-" + pushCount;
    }

}