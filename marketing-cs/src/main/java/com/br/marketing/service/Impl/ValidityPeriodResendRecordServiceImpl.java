package com.br.marketing.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.ValidityPeriodResendRecord;
import com.br.marketing.entity.ValidityPeriodResendTypeMapping;
import com.br.marketing.entity.ValidityPeriodResendTypeMappingExample;
import com.br.marketing.enums.ValidityPeriodResendEnum;
import com.br.marketing.mapper.ValidityPeriodResendRecordMapperBase;
import com.br.marketing.mapper.ValidityPeriodResendTypeMappingMapperBase;
import com.br.marketing.service.Impl.validityperiod.ValidityPeriodResendStrategySelector;
import com.br.marketing.service.ValidityPeriodResendRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 有效期重新发送记录Service相关实现
 *
 * @author senyang.zheng
 * @date 2023/10/18
 */
@Service
@Slf4j
public class ValidityPeriodResendRecordServiceImpl implements ValidityPeriodResendRecordService {

    @Resource
    private ValidityPeriodResendTypeMappingMapperBase validityPeriodResendTypeMappingMapper;
    @Resource
    private ValidityPeriodResendRecordMapperBase validityPeriodResendRecordMapper;
    @Resource
    private ValidityPeriodResendStrategySelector selector;

    /**
     * 保存重推记录
     *
     * @param apiCode          apiCode
     * @param userType         场景
     * @param validityPeriodId 有效期配置主键id
     * @author senyang.zheng
     * @date 2023/10/18
     */
    @Override
    public void saveRecord(String apiCode, String userType, Long validityPeriodId) {
        ValidityPeriodResendTypeMappingExample example = new ValidityPeriodResendTypeMappingExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType);
        example.setOrderByClause("create_time desc");
        List<ValidityPeriodResendTypeMapping> validityPeriodResendTypeMappings = validityPeriodResendTypeMappingMapper.selectByExample(example);
        if (CollectionUtil.isEmpty(validityPeriodResendTypeMappings)) {
            log.info("该apiCode:{},该场景:{},未查询到重推规则。", apiCode, userType);
            return;
        }
        ValidityPeriodResendTypeMapping mapping = validityPeriodResendTypeMappings.get(0);
        ValidityPeriodResendEnum resendType = ValidityPeriodResendEnum.getEnumByCode(mapping.getResendType());
        ValidityPeriodResendRecord resendRecord = new ValidityPeriodResendRecord();
        resendRecord.setApiCode(apiCode);
        resendRecord.setValidityPeriodId(validityPeriodId);
        resendRecord.setResendType(mapping.getResendType());
        resendRecord.setResendData(buildResendData(mapping, resendType));
        resendRecord.setResendStatus(0);
        resendRecord.setIsDelete(0);
        resendRecord.setCreateTime(new Date());
        resendRecord.setUpdateTime(new Date());
        validityPeriodResendRecordMapper.insert(resendRecord);
    }

    private String buildResendData(ValidityPeriodResendTypeMapping mapping, ValidityPeriodResendEnum resendType) {
        JSONObject paramJson = new JSONObject();
        paramJson.put("apiCode", mapping.getApiCode());
        paramJson.put("userType", mapping.getUserType());
        JSONObject resendData = selector.buildResendData(paramJson, resendType);
        resendData.putAll(JSONObject.parseObject(mapping.getExtendField()));
        return resendData.toJSONString();
    }
}
