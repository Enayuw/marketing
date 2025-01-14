package com.br.marketing.service.Impl.qifu;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.br.common.log.AlertLog;
import com.br.common.util.StringUtils;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomizeDataValidConfigMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * QiFuDataValidityPeriodService
 *
 * @Author zhen.Li1
 * @Date 2025-01-08
 * @Desc 奇富360数据有效期判断service
 */
@Service
@Slf4j
public class QiFuDataValidityPeriodService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private MarketingCustomizeDataValidConfigMapper customizeDataValidConfigMapper;


    /**
     * 判断360上传info数据是否在有效期
     *
     * @param marketingSyncInfo
     * @return
     */
    public boolean syncInfoValidityPeriod(MarketingSyncInfo marketingSyncInfo, Date date) {

        return validConfigJudge(marketingSyncInfo.getApiCode(), marketingSyncInfo.getCusBatch(), null, null, date);


    }


    /**
     * 判断360上传明细数据是否在有效期
     *
     * @param marketingSyncUser
     * @param date
     * @return
     */
    public boolean syncDetailValidityPeriod(MarketingSyncUser marketingSyncUser, Date date) {

        return validConfigJudge(marketingSyncUser.getApiCode(), marketingSyncUser.getCusBatch(), marketingSyncUser.getAppletDate(),
                marketingSyncUser.getUserType(), date);


    }

    public boolean validConfigJudge(String apiCode, String cusBatch, String appletDate, String userType, Date date) {

        MarketingCustomizeDataValidConfigExample example = new MarketingCustomizeDataValidConfigExample();
        MarketingCustomizeDataValidConfigExample.Criteria criteria = example.createCriteria();
        criteria.andApiCodeEqualTo(apiCode).andTaskIdEqualTo(cusBatch).andIsDelEqualTo(1);
        if (StringUtils.isNotEmpty(appletDate)) {
            criteria.andAppletDateEqualTo(appletDate);
        }
        if (StringUtils.isNotEmpty(userType)) {
            criteria.andUserTypeEqualTo(userType);
        }
        long begin = System.currentTimeMillis();
        List<MarketingCustomizeDataValidConfig> configList = new ArrayList<>();
        Long waitMilliseconds = marketingCommonConfig.getQiFuSyncToPolicyValidityCheckDelayTime();
        while (System.currentTimeMillis() - begin < waitMilliseconds) {
            configList = customizeDataValidConfigMapper.selectByExample(example);
            if (!CollectionUtil.isEmpty(configList)) {
                break;
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(), "奇富360促完件上传数据推决策，未查询到有效期配置" +
                        "等待异常"), e);
                Thread.currentThread().interrupt();
            }
        }

        /*List<MarketingCustomizeDataValidConfig> configList = customizeDataValidConfigMapper.selectByExample(example);
        if (CollectionUtil.isEmpty(configList)) {
            *//*log.warn("奇富360上传数据未查询到有效配置，cusBatch:{}，等待 {} 秒后重试", cusBatch,
                    marketingCommonConfig.getQiFuSyncToPolicyValidityCheckDelayTime());*//*
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(), "奇富360上传数据未查询到有效配置,cusBatch:"
                    .concat(cusBatch).concat(marketingCommonConfig.getQiFuSyncToPolicyValidityCheckDelayTime().toString()).concat("秒后重试")));
            try {
                TimeUnit.SECONDS.sleep(marketingCommonConfig.getQiFuSyncToPolicyValidityCheckDelayTime());
            } catch (InterruptedException e) {
                *//*log.error("奇富360促完件上传数据推决策，未查询到有效期配置等待异常", e);*//*
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(), "奇富360促完件上传数据推决策，未查询到有效期配置" +
                        "等待异常"), e);
                Thread.currentThread().interrupt();
            }
            configList = customizeDataValidConfigMapper.selectByExample(example);
        }*/
        if (CollectionUtil.isEmpty(configList) || configList.size() > 1) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(), "奇富360上传数据未查询到有效配置或查询到多条cusBatch:"
                    .concat(cusBatch)));
            return Boolean.FALSE;
        }
        MarketingCustomizeDataValidConfig config = configList.get(0);
        DateTime currentDate = DateTime.of(date);
        DateTime startDate = DateUtil.parse(config.getValidStartDate());
        DateTime endDate = DateUtil.parse(config.getValidEndDate());
        if (currentDate.isAfterOrEquals(startDate) && currentDate.isBeforeOrEquals(endDate)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;

    }


}
