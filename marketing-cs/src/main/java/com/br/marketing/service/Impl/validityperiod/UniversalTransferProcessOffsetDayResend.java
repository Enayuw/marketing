package com.br.marketing.service.Impl.validityperiod;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import cn.hutool.core.util.ObjectUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.constants.rocketmq.MarketingTransferConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import com.google.api.client.util.Lists;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.ValidityPeriodResendType;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.ValidityPeriodResendRecord;
import com.br.marketing.enums.ValidityPeriodResendEnum;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.Impl.ValidityPeriodDataServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * 转化数据(T-N有效)执行通用规则重推流程
 *
 * @author senyang.zheng
 * @date 2024/01/12
 */
@Slf4j
@Service
@ValidityPeriodResendType(resendType = ValidityPeriodResendEnum.UNIVERSAL_TRANSFER_PROCESS_OFFSET_DAY_RESEND)
public class UniversalTransferProcessOffsetDayResend extends ValidityPeriodResendCommonService
    implements ValidityPeriodResendStrategy<MarketingTransferInfo> {

    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;
    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;
    @Resource
    private RabbitMqProducter rabbitMqProducter;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * 构建重推数据扩展字段
     *
     * @param params params
     * @return {@link JSONObject }
     * @author senyang.zheng
     * @date 2023/11/08
     */
    @Override
    public JSONObject buildResendData(Map<String, Object> params) {
        return new JSONObject();
    }

    /**
     * 获取重推数据
     *
     * @param record 有效期重新发送记录
     * @param page 页码
     * @param pageSize 页大小
     * @return {@link List }<{@link MarketingTransferInfo }>
     * @author senyang.zheng
     * @date 2024/01/12
     */
    @Override
    public List<MarketingTransferInfo> fetchData(ValidityPeriodResendRecord record, int page, int pageSize) {

        JSONObject resendData = JSONObject.parseObject(record.getResendData());
        Integer offsetDay = resendData.getInteger("offsetDay");
        // 获取有效期范围
        Map<String, String> validPeriodRange = marketingDataValidConfigMapper.
            getValidPeriodRangeByApiCodeAndUserTypeAndOffsetDay(record.getValidityPeriodId(), offsetDay);
        if (ObjectUtil.isEmpty(validPeriodRange)) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(),
                    "转化数据(T-N有效)有效期变更重推失败，未存在有效的有效期，record:" + record));
            return Lists.newArrayList();
        }
        // 开始结束时间范围外扩一天
        String dateStartStr = ValidityPeriodDataServiceImpl.getDateStr(validPeriodRange.get("validStartDate"), -1);
        String dateEndStr = ValidityPeriodDataServiceImpl.getDateStr(validPeriodRange.get("validEndDate"), 1);

        String apiCode = validPeriodRange.get("apiCode");
        // 根据时间范围获取全部转化基础数据
        return marketingTransferInfoMapper.getMarketingTransferInfoIdByValidPeriodRange(apiCode, dateStartStr, dateEndStr, page, pageSize);

    }

    /**
     * 处理重推逻辑
     *
     * @param data 重推数据
     * @param record 有效期重新发送记录
     * @author senyang.zheng
     * @date 2023/11/09
     */
    @Override
    public void resend(List<MarketingTransferInfo> data, ValidityPeriodResendRecord record) {
        // 创建线程池
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(marketingCommonConfig.getUniversalTransferProcessResendThreadNum(),
            marketingCommonConfig.getUniversalTransferProcessResendThreadNum());
        data.stream().map(transferInfo -> buildMqFact(transferInfo, record)).map(JSONObject::toJSONString)
            .forEach((String mqFact) -> pool.submit(() -> {
                if(rocketMqSwitch.rocketMQSwitchFlag(null, MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE)){
                    rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC
                            , MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE, mqFact);
                }else{
                    rabbitMqProducter.send(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE, mqFact);
                }
            }));
        // 关闭线程池
        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("UniversalTransferProcessResend 等待线程池结束");
            }
        } catch (Exception e) {
            pool.shutdownNow();
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.VALIDITY_INTERFACEERROR.getCode(),
                    "UniversalTransferProcessResend 线程池关闭异常,直接关闭线程池"), e);
        }
    }
}
