package com.br.marketing.service.Impl.validityperiod;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.ValidityPeriodResendType;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.ValidityPeriodResendRecord;
import com.br.marketing.enums.ValidityPeriodResendEnum;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.Impl.ValidityPeriodDataServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import shaded.com.google.common.base.Splitter;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 转化数据执行通用规则重推流程
 *
 * @author senyang.zheng
 * @date 2023/11/13
 */
@Slf4j
@Service
@ValidityPeriodResendType(resendType = ValidityPeriodResendEnum.UNIVERSAL_TRANSFER_PROCESS_RESEND)
public class UniversalTransferProcessResend implements ValidityPeriodResendStrategy<MarketingTransferInfo> {

    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;
    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;
    @Resource
    private RabbitMqProducter rabbitMqProducter;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * 构建重推数据扩展字段
     *
     * @param params params
     * @return {@link JSONObject }
     * @author senyang.zheng
     * @date 2023/11/13
     */
    @Override
    public JSONObject buildResendData(Map<String, Object> params) {
        return new JSONObject();
    }

    /**
     * 获取重推数据
     *
     * @param record   有效期重新发送记录
     * @param page     页码
     * @param pageSize 页大小
     * @return {@link List }<{@link MarketingTransferInfo }>
     * @author senyang.zheng
     * @date 2023/11/20
     */
    @Override
    public List<MarketingTransferInfo> fetchData(ValidityPeriodResendRecord record, int page, int pageSize) {
        //获取有效期范围
        Map<String, String> validPeriodRange = marketingDataValidConfigMapper.getValidPeriodRangeByApiCodeAndUserType(record.getValidityPeriodId());
        //开始结束时间范围外扩一天
        String dateStartStr = ValidityPeriodDataServiceImpl.getDateStr(validPeriodRange.get("validStartDate"), -1);
        String dateEndStr = ValidityPeriodDataServiceImpl.getDateStr(validPeriodRange.get("validEndDate"), 1);

        String apiCode = validPeriodRange.get("apiCode");
        //根据时间范围获取全部转化基础数据
        return marketingTransferInfoMapper.getMarketingTransferInfoIdByValidPeriodRange(apiCode, dateStartStr, dateEndStr,page,pageSize);
    }

    /**
     * 处理重推逻辑
     *
     * @param data   重推数据
     * @param record 重推记录
     * @author senyang.zheng
     * @date 2023/11/13
     */
    @Override
    public void resend(List<MarketingTransferInfo> data, ValidityPeriodResendRecord record) {
        long start = System.currentTimeMillis();
        log.warn("UniversalTransferProcessResend start");
        // 创建线程池
        ThreadPoolExecutor pool =
            BrExecutors.getThreadPool(marketingCommonConfig.getUniversalTransferProcessResendThreadNum(), marketingCommonConfig.getUniversalTransferProcessResendThreadNum());
        data.stream()
            .map(transferInfo -> buildMqFact(transferInfo, record))
            .map(JSONObject::toJSONString)
            .forEach(mqFact -> pool.submit(() -> rabbitMqProducter.send(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE, mqFact)));
        //关闭线程池
        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("UniversalTransferProcessResend 等待线程池结束");
            }
        } catch (Exception e) {
            pool.shutdownNow();
            log.error("UniversalTransferProcessResend 线程池关闭异常,直接关闭线程池", e);
        }
        long end = System.currentTimeMillis();
        log.warn("UniversalTransferProcessResend end，耗时:{}", end - start);
    }

    /**
     * 构建消息体
     *
     * @param info   信息
     * @param record 重推记录
     * @return {@link MqFact }
     * @author senyang.zheng
     * @date 2023/11/13
     */
    protected static MqFact buildMqFact(MarketingTransferInfo info, ValidityPeriodResendRecord record) {
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(info.getId());
        mqFact.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
        JSONObject resendData = JSONObject.parseObject(record.getResendData());
        if (resendData != null && StringUtils.isNotEmpty((resendData.getString("includeRules")))) {
            Set<String> includeRules = Sets.newHashSet(Splitter.on(",").splitToList(resendData.getString("includeRules")));
            mqFact.setIncludeRules(includeRules);
        }
        return mqFact;
    }

}
