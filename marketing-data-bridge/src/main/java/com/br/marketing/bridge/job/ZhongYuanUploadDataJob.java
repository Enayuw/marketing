package com.br.marketing.bridge.job;

import com.alibaba.fastjson2.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.dto.dataclean.mq.MqDataJsonParse;
import com.br.marketing.entity.MarketingCustomerOriginalData;
import com.br.marketing.entity.MarketingCustomerOriginalDataExample;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.github.pagehelper.PageHelper;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ZhongYuanUploadDataJob
 * @Description 中原消金数据上传Job，查询最新数据并发送MQ消息
 * @Author kongbx
 * @Date 2025/11/17 19:54
 */
@Component
@Slf4j
public class ZhongYuanUploadDataJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCustomerOriginalDataMapper marketingCustomerOriginalDataMapper;
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TrackingService trackingService;
    private static final int REDIS_EXPIRE_SECONDS = 86400; // 1天过期时间
    private static final String TITLE = "【中原消金数据上传】";

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        try {
            // 1. 从配置获取中原消金的apiCode
            String apiCode = jobExecutionMultipleShardingContext.getJobParameter();
            if(StringUtils.isEmpty(apiCode)){
                apiCode = getZhongYuanApiCode();
            }

            if (apiCode == null) {
                log.warn("{}Job执行失败：未配置apiCode", TITLE);
                return;
            }
            
            // 2. 查询最新的一条数据
            MarketingCustomerOriginalData latestData = getLatestData(apiCode);
            if (latestData == null || latestData.getId() == null) {
                log.warn("{}Job执行：未查询到数据，apiCode: {}", TITLE, apiCode);
                return;
            }
            
            // 3. 构建Redis key，存储id
            String redisKey = buildRedisKey(apiCode);
            
            // 4. 判断缓存中的id是否是最新id
            String cachedId = redisChgService.get(redisKey);
            Long latestId = latestData.getId();
            
            if (cachedId != null && cachedId.equals(String.valueOf(latestId))) {
                log.warn("{}Job执行：数据id未变化，跳过发送MQ消息，apiCode: {}, id: {}", TITLE, apiCode, latestId);
                return;
            }
            
            // 5. 如果不是最新id，发送MQ消息
            sendMqMessage(apiCode, latestId);
            
            // 6. 缓存最新的id到Redis，过期时间1天
            redisChgService.setex(redisKey, String.valueOf(latestId), REDIS_EXPIRE_SECONDS);
            log.warn("{}Job执行成功：已发送MQ消息并缓存id，apiCode: {}, id: {}", TITLE, apiCode, latestId);

            try {
                String remark = String.format("中原消金上传数据结构变更job，查询最新数据更新字段结构,最新的数据id：%s"
                        , latestId);
                trackingService.trackPointLog(DataFlowDirection.OUT
                        , apiCode
                        , "中原消金上传数据结构变更job"
                        , 1L
                        , remark
                        , TrackingContext.generateBatchId());
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }

        } catch (Exception e) {
            log.error("{}Job执行异常", TITLE, e);
        }
    }
    
    /**
     * 从配置获取中原消金的apiCode
     */
    private String getZhongYuanApiCode() {
        try {
            Map<String, String> zhongYuanIdentity = marketingCommonConfig.getZhongYuanIdentity();
            return zhongYuanIdentity != null ? zhongYuanIdentity.get("apiCode") : null;
        } catch (Exception e) {
            log.error("{}获取apiCode异常", TITLE, e);
            return null;
        }
    }
    
    /**
     * 查询最新的一条数据
     */
    private MarketingCustomerOriginalData getLatestData(String apiCode) {
        try {
            MarketingCustomerOriginalDataExample example = new MarketingCustomerOriginalDataExample();
            example.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andReceiveDateEqualTo(LocalDate.now().toString())
                    .andDataTypeEqualTo(DataProcessEnum.DataTypeEnum.UPLOAD.getCode())
                    .andAcceptTypeEqualTo(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode());
            example.setOrderByClause("create_time DESC");
            
            // 使用PageHelper限制结果数量为1
            PageHelper.startPage(1, 1);
            List<MarketingCustomerOriginalData> dataList = marketingCustomerOriginalDataMapper.selectByExample(example);
            if (!CollectionUtils.isEmpty(dataList)) {
                return dataList.get(0);
            }
            return null;
        } catch (Exception e) {
            log.error("{}查询最新数据异常，apiCode: {}", TITLE, apiCode, e);
            return null;
        }
    }
    
    /**
     * 构建Redis key
     */
    private String buildRedisKey(String apiCode) {
        return RedisKeyConstant.ORIGINAL_DATA_JSON_PARSE
                .concat(apiCode)
                .concat(":")
                .concat(String.valueOf(DataProcessEnum.DataTypeEnum.UPLOAD.getCode()))
                .concat(":")
                .concat(String.valueOf(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode()))
                .concat(":")
                .concat(LocalDate.now().toString());
    }
    
    /**
     * 发送MQ消息
     */
    private void sendMqMessage(String apiCode, Long dataId) {
        try {
            MqDataJsonParse mqDataJsonParse = new MqDataJsonParse();
            mqDataJsonParse.setDataId(dataId);
            mqDataJsonParse.setSystemType(DataProcessEnum.SystemTypeEnum.MARKETING.getCode());
            mqDataJsonParse.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
            mqDataJsonParse.setAcceptType(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode());

            rocketMqSwitch.sendMessage(apiCode, MarketingAssistConstants.TOPIC,
                    MarketingAssistConstants.TAG_MARKETING_CUSTOMER_DATA_JSON_PARSE,
                    JSON.toJSONString(mqDataJsonParse),
                    MQConstants.ROUTING_KEY_MARKETING_CUSTOMER_DATA_JSON_PARSE);
            
            log.warn("{}Job发送MQ消息成功，apiCode: {}, dataId: {}", TITLE, apiCode, dataId);
        } catch (Exception e) {
            log.error("{}Job发送MQ消息异常，apiCode: {}, dataId: {}", TITLE, apiCode, dataId, e);
            throw e;
        }
    }
}
