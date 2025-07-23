package com.br.marketing.service.Impl.zhongan.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.bo.ZaMarketDataBO;
import com.br.marketing.bo.ZhongAnCollidingDataBO;
import com.br.marketing.chain.zhongan.ZhongAnReportHandler;
import com.br.marketing.chain.zhongan.report.Connect3DaysHandler;
import com.br.marketing.chain.zhongan.report.ConnectOrSmsSend7DaysHandler;
import com.br.marketing.chain.zhongan.report.ConnectOrSmsSendMonthHandler;
import com.br.marketing.chain.zhongan.report.ParallelChainExecutor;
import com.br.marketing.chain.zhongan.report.SmsSend2DaysHandler;
import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.client.zhongan.input.ZaMarketDetail;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.ZhongAnCollidingConfig;
import com.br.marketing.entity.ZhongAnCollidingDataLog;
import com.br.marketing.entity.ZhongAnSmsRosterLockingData;
import com.br.marketing.entity.ZhongAnSmsRosterLockingDataExample;
import com.br.marketing.entity.ZhonganRosterLockingData;
import com.br.marketing.entity.ZhonganRosterLockingDataExample;
import com.br.marketing.mapper.ZhongAnCollidingConfigMapper;
import com.br.marketing.mapper.ZhongAnCollidingDataLogMapper;
import com.br.marketing.mapper.ZhongAnSmsRosterLockingDataMapper;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import com.br.marketing.monkeydata.service.Impl.DistributeSoleProcessor;
import com.br.marketing.service.Impl.zhongan.ZhongAnCollidingDataService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class ZhongAnCollidingDataServiceImpl implements ZhongAnCollidingDataService {

    private final List<String> NOT_CALL_DATA_SOURCE_TYPE = Lists.newArrayList("S");
    private final List<String> NOT_SMS_DATA_SOURCE_TYPE = Lists.newArrayList("C&NS", "NC");

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private ZhongAnCollidingConfigMapper zhongAnCollidingConfigMapper;
    @Resource
    private ParallelChainExecutor executor;
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;
    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private ZhonganRosterLockingDataMapper zhonganRosterLockingDataMapper;
    @Resource
    private ZhongAnSmsRosterLockingDataMapper zhongAnSmsRosterLockingDataMapper;

    @Resource
    private ZhongAnCollidingDataLogMapper zhongAnCollidingDataLogMapper;


    @Resource
    private SmsSend2DaysHandler smsSend2DaysHandler;

    @Resource
    private Connect3DaysHandler connect3DaysHandler;

    @Resource
    private ConnectOrSmsSend7DaysHandler connectOrSmsSend7DaysHandler;

    @Resource
    private ConnectOrSmsSendMonthHandler connectOrSmsSendMonthHandler;

    @Resource
    private DistributeSoleProcessor distributeSoleProcessor;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        TpDynamicExecutor pushPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.ZHONGAN_REPORT_3710048.getName(), 10, 10);

        String apiCode = "3710048";
        String bizDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String parameter = context.getJobParameter();
        if (StringUtils.isNotBlank(parameter)) {
            String[] split = parameter.split("#");
            apiCode = split[0];
            bizDate = split[1];
        }
        JSONObject collidingConfig = marketingCommonConfig.getZhongAnCollidingDataConfig();
        int limit = collidingConfig.getInteger("limit") != null ? collidingConfig.getInteger("limit") : 2000;
        HashMap<String, JSONObject> zhongAnDetailPush = marketingCommonConfig.getZhongAnDetailPush();
        // 获取待上报数据
        List<ZhongAnCollidingConfig> configs = zhongAnCollidingConfigMapper.queryZhongAnCollidingConfigByPriority();
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        for (ZhongAnCollidingConfig config : configs) {
            String configSql = config.getQuerySql();
            String replaceSql = configSql.replace("#{apiCode}", "'" + apiCode + "'").replace("#{bizDate}", "'" + bizDate + "'");
            String completeSql = replaceSql.concat(" limit " + limit);
            String type = config.getDataSourceType();
            Integer isOutbound = NOT_CALL_DATA_SOURCE_TYPE.contains(type) ? 0 : 1;
            Integer isSmsSend = NOT_SMS_DATA_SOURCE_TYPE.contains(type) ? 0 : 1;
            boolean flag = true;
            while (flag) {
                List<ZhongAnCollidingDataBO> collidingDatas = zhongAnCollidingConfigMapper.queryCollidingDataByConfigSql(completeSql);
                if (CollectionUtils.isEmpty(collidingDatas)) {
                    flag = false;
                    continue;
                }
                Set<String> custNumSet = collidingDatas.stream().map(ZhongAnCollidingDataBO::getCaseNum).collect(Collectors.toSet());
                Map<String, SyncUserValidityPeriodsBO> keyToSyncUserBO = transferDataValidityPeriodService
                        .getValidityPeriodsByCustNumAndUserType(custNumSet, collidingDatas.get(0).getUserType(), apiCode, bizDate);
                //单批次内去重
                Map<String, ZhongAnCollidingDataBO> map = collidingDatas.stream().collect(Collectors.toMap(ZhongAnCollidingDataBO::getMobileMd5,
                        Function.identity(), (ZhongAnCollidingDataBO oldVal, ZhongAnCollidingDataBO newVal) -> newVal));
                List<ZhongAnCollidingDataBO> collidingDataBOS = Lists.newArrayList();
                List<Long> nonValidSmsIds = Lists.newArrayList();
                List<Long> nonValidCallIds = Lists.newArrayList();
                List<Long> callFrequencyCapIds = Lists.newArrayList();
                List<Long> smsFrequencyCapIds = Lists.newArrayList();
                for (Map.Entry<String, ZhongAnCollidingDataBO> entry : map.entrySet()) {
                    String cellMd5 = entry.getKey();
                    ZhongAnCollidingDataBO value = entry.getValue();
                    List<ZhongAnReportHandler> handlers = Lists.newArrayList();
                    handlers.add(smsSend2DaysHandler);
                    handlers.add(connect3DaysHandler);
                    handlers.add(connectOrSmsSend7DaysHandler);
                    handlers.add(connectOrSmsSendMonthHandler);
                    boolean result = executor.execute(handlers, cellMd5, bizDate);
                    if (result) {
                        SyncUserValidityPeriodsBO bo = keyToSyncUserBO.get(value.getCaseNum());
                        if (bo == null || CollectionUtils.isEmpty(bo.getSyncUsers())) {
                            log.warn("众安通话明细上报, 未匹配到上传数据, caseNum: {}, userType: {}", value.getCaseNum(), value.getUserType());
                            nonValidSmsIds.add(value.getSmsId());
                            nonValidCallIds.add(value.getCallId());
                            continue;
                        }
                        MarketingSyncUser syncUser = bo.getSyncUsers().get(0);
                        value.setSyncUser(syncUser);
                        collidingDataBOS.add(value);
                    } else {
                        callFrequencyCapIds.add(value.getCallId());
                        smsFrequencyCapIds.add(value.getSmsId());
                    }
                }

                updateCallStatus(nonValidCallIds, null, 4);
                updateSmsStatus(nonValidSmsIds, null, 4);
                updateCallStatus(callFrequencyCapIds, null, 8);
                updateSmsStatus(smsFrequencyCapIds, null, 8);

                if (collidingDataBOS.isEmpty()) {
                    continue;
                }

                JSONObject result = distributeSoleProcessor.processCollidingDataBOS(collidingDataBOS);
                List<Long> notPushCallIds = result.getObject("notPushCallIds", new TypeReference<List<Long>>() {
                });
                if (!notPushCallIds.isEmpty()) {
                    updateCallStatus(notPushCallIds, null, 6);
                }
                List<Long> notPushSmsIds = result.getObject("notPushSmsIds", new TypeReference<List<Long>>() {
                });
                if (!notPushSmsIds.isEmpty()) {
                    updateSmsStatus(notPushSmsIds, null, 6);
                }

                List<ZaMarketDetail> pushList = new ArrayList<>();
                List<Long> pushSmsIds = new ArrayList<>();
                List<Long> pushCallIds = new ArrayList<>();
                int size = collidingDataBOS.size();
                int pushSize = 100;
                int count = 0;
                for (ZhongAnCollidingDataBO collidingDataBO : collidingDataBOS) {
                    ZaMarketDetail detail = new ZaMarketDetail();
                    pushCallIds.add(collidingDataBO.getCallId());
                    pushSmsIds.add(collidingDataBO.getSmsId());
                    String channelCode = zhongAnDetailPush.get(collidingDataBO.getUserType()).getString("channelCode");
                    detail.setBizDate(collidingDataBO.getBizDate());
                    detail.setTaskId(collidingDataBO.getSyncUser().getCusBatch());
                    detail.setChannelCode(channelCode);
                    detail.setTag("MG");
                    detail.setMobileMd5(collidingDataBO.getMobileMd5());
                    detail.setPostbackDate(DateUtil.formatDateTime(new Date()));
                    detail.setIsOutbound(isOutbound);
                    detail.setIsConnect(collidingDataBO.getIsConnect() == null ? 0 : collidingDataBO.getIsConnect());
                    detail.setIsSmsSend(isSmsSend);
                    detail.setIsSmsSendSuccess(collidingDataBO.getSmsSendStatus() == null ? 0 : collidingDataBO.getSmsSendStatus());
                    pushList.add(detail);
                    count++;
                    log.warn("pushList size:{},size:{},count:{}", pushList.size(), size, count);
                    if (pushList.size() == pushSize || size == count) {
                        List<Long> finalPushIds = pushCallIds;
                        List<Long> finalPushSmsIds = pushSmsIds;
                        String finalApiCode = collidingDataBO.getApiCode();
                        updateCallStatus(finalPushIds, 0, null);
                        if (!CollectionUtils.isEmpty(finalPushSmsIds)) {
                            updateSmsStatus(finalPushSmsIds, 0, null);
                        }
                        pushPool.execute(() -> {
                            ZaMarketDataDTO dataDTO = new ZaMarketDataDTO();
                            dataDTO.setData(pushList);
                            methodRetryHandlerService.callZhongAnData(new ZaMarketDataBO(dataDTO
                                    , collidingDataBO.getApiCode(), "MG", finalPushIds, finalPushSmsIds), null);
                            insertCollidingLog(pushList, finalApiCode, config.getDataSourceType());
                        });
                        pushSmsIds = new ArrayList<>();
                        pushCallIds = new ArrayList<>();
                    }
                }
            }
        }
        pushPool.shutdownAndAwaitTermination();
    }

    private void insertCollidingLog(List<ZaMarketDetail> pushList, String apiCode, String dataSourceType) {
        List<ZhongAnCollidingDataLog> collidingDataLogList = Lists.newArrayList();
        pushList.forEach((ZaMarketDetail push) -> {
            ZhongAnCollidingDataLog collidingDataLog = new ZhongAnCollidingDataLog();
            collidingDataLog.setApiCode(apiCode);
            collidingDataLog.setDataSourceType(dataSourceType);
            collidingDataLog.setCell(push.getMobileMd5());
            collidingDataLog.setSmsSendStatus(push.getIsSmsSendSuccess());
            collidingDataLog.setIsConnect(push.getIsConnect());
            collidingDataLog.setReportDate(push.getBizDate());
            collidingDataLogList.add(collidingDataLog);
        });
        log.warn("collidingDataLogList:{}", JSONObject.toJSONString(collidingDataLogList));
        zhongAnCollidingDataLogMapper.batchInsert(collidingDataLogList);
    }

    private void updateSmsStatus(List<Long> ids, Integer pushStatus, Integer updateStatus) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ZhongAnSmsRosterLockingData data = new ZhongAnSmsRosterLockingData();
        if (pushStatus != null) {
            data.setPushStatus(pushStatus);
        }
        if (updateStatus != null) {
            data.setStatus(updateStatus);
        }
        data.setUpdateTime(new Date());
        ZhongAnSmsRosterLockingDataExample example = new ZhongAnSmsRosterLockingDataExample();
        example.createCriteria().andIdIn(ids);
        zhongAnSmsRosterLockingDataMapper.updateByExampleSelective(data, example);
    }

    private void updateCallStatus(List<Long> ids, Integer pushStatus, Integer updateStatus) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ZhonganRosterLockingData data = new ZhonganRosterLockingData();
        if (pushStatus != null) {
            data.setPushStatus(pushStatus);
        }
        if (updateStatus != null) {
            data.setStatus(updateStatus);
        }
        data.setUpdateTime(new Date());
        ZhonganRosterLockingDataExample example = new ZhonganRosterLockingDataExample();
        example.createCriteria().andIdIn(ids);
        zhonganRosterLockingDataMapper.updateByExampleSelective(data, example);
    }
}
