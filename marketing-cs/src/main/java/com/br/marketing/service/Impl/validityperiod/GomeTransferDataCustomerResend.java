package com.br.marketing.service.Impl.validityperiod;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.ValidityPeriodResendType;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferInfoExample;
import com.br.marketing.entity.ValidityPeriodResendRecord;
import com.br.marketing.enums.ValidityPeriodResendEnum;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.Impl.ValidityPeriodDataServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 国美转化数据自动过滤推客服 重推实现
 * @author chenh
 * @date 2023/10/19
 */
@Slf4j
@Service
@ValidityPeriodResendType(resendType = ValidityPeriodResendEnum.GOME_TRANSFERDATA_CUSTOMER_AUTO_FILTRATION)
public class GomeTransferDataCustomerResend implements ValidityPeriodResendStrategy<MarketingTransferInfo> {

    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;
    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private RabbitMqProducter producter;


    @Override
    public String buildResendData(Map<String, Object> params) {
        return null;
    }

    @Override
    public List<MarketingTransferInfo> fetchData(ValidityPeriodResendRecord validityPeriodResendRecord) {
        //获取有效期范围
        Map<String, String> validPeriodRange =
                marketingDataValidConfigMapper.getValidPeriodRangeByApiCodeAndUserType(validityPeriodResendRecord.getValidityPeriodId());
        //开始结束时间范围外扩一天
        String dateStartStr = ValidityPeriodDataServiceImpl.getDateStr(validPeriodRange.get("validStartDate"), -1);
        String dateEndStr = ValidityPeriodDataServiceImpl.getDateStr(validPeriodRange.get("validEndDate"), 1);

        Date validStartDate = DateUtil.beginOfDay(DateUtil.parseDate(dateStartStr));
        Date validEndDate = DateUtil.endOfDay(DateUtil.parseDate(dateEndStr));

        String apiCode = validPeriodRange.get("apiCode");
        //根据时间范围获取全部转化基础数据
        MarketingTransferInfoExample transferInfoExample = new MarketingTransferInfoExample();
        transferInfoExample.createCriteria().andApiCodeEqualTo(apiCode).andCreateTimeGreaterThanOrEqualTo(validStartDate).andCreateTimeLessThanOrEqualTo(validEndDate);
        return marketingTransferInfoMapper.selectByExample(transferInfoExample);
    }

    @Override
    public void resend(List<MarketingTransferInfo> data) {
        long start = System.currentTimeMillis();
        log.info("国美转化数据自动过滤推客服有效期变更重推任务开始");
        // 创建线程池
        ThreadPoolExecutor pool =
                BrExecutors.getThreadPool(marketingCommonConfig.getGoMeTransferDataResendJobThreadNum(),
                        marketingCommonConfig.getGoMeTransferDataResendJobThreadNum());
        data.stream()
                .map(GomeTransferDataCustomerResend::buildMqFact)
                .map(JSONObject::toJSONString)
                .forEach(maFact -> pool.submit(() -> producter.send(MQConstants.ROUTING_KEY_UNIVERSAL_TRANSFER_RECEIVE, maFact)));
        //关闭线程池
        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (Exception e) {
            pool.shutdownNow();
            log.error("线程池关闭异常,直接关闭线程池", e);
        }
        long end = System.currentTimeMillis();
        log.info("国美转化数据自动过滤推客服有效期变更重推任务结束，耗时:{}", end - start);
    }

    private static MqFact buildMqFact(MarketingTransferInfo info) {
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(info.getId());
        mqFact.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
        Set set = new HashSet<>();
        set.add("Gome_TransferData_Customer_Auto_Filtration");
        mqFact.setIncludeRules(set);

        return mqFact;
    }
}
