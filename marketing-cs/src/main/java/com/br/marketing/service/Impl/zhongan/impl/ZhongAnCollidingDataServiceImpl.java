package com.br.marketing.service.Impl.zhongan.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.bo.ZaMarketDataBO;
import com.br.marketing.bo.ZhongAnCollidingDataBO;
import com.br.marketing.chain.zhongan.ZhongAnReportHandler;
import com.br.marketing.chain.zhongan.report.ParallelChainExecutor;
import com.br.marketing.chain.zhongan.report.Sms2DayHandler;
import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.client.zhongan.input.ZaMarketDetail;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.ZhongAnCollidingConfig;
import com.br.marketing.entity.ZhongAnSmsRosterLockingData;
import com.br.marketing.entity.ZhongAnSmsRosterLockingDataExample;
import com.br.marketing.entity.ZhonganRosterLockingData;
import com.br.marketing.entity.ZhonganRosterLockingDataExample;
import com.br.marketing.mapper.ZhongAnCollidingConfigMapper;
import com.br.marketing.mapper.ZhongAnSmsRosterLockingDataMapper;
import com.br.marketing.mapper.ZhonganRosterLockingDataMapper;
import com.br.marketing.service.Impl.zhongan.ZhongAnCollidingDataService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class ZhongAnCollidingDataServiceImpl implements ZhongAnCollidingDataService {

    private final ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(24, 24, new SynchronousQueue<>());


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
    private Sms2DayHandler sms2DayHandler;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String apiCode = "3710048";
        String bizDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String parameter = context.getJobParameter();
        if (StringUtils.isNotBlank(parameter)) {
            String[] split = parameter.split("#");
            apiCode = split[0];
            bizDate = split[1];
        }
        Integer limit = marketingCommonConfig.getWuBaCollidingDataSubmitPageSize();
        HashMap<String, JSONObject> zhongAnDetailPush = marketingCommonConfig.getZhongAnDetailPush();
        // 获取待上报数据
        List<ZhongAnCollidingConfig> configs = zhongAnCollidingConfigMapper.queryZhongAnCollidingConfigByPriority();
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        for (ZhongAnCollidingConfig config : configs) {
            String configSql = config.getQuerySql();
            String replaceSql = configSql.replace("#{apiCode}", apiCode).replace("#{bizDate}", bizDate);
            String completeSql = replaceSql.concat(" limit " + limit);
            Integer isOutbound = 1;
            Integer isSmsSend = 1;
            boolean flag = true;
            while (flag) {
                List<ZhongAnCollidingDataBO> collidingDatas = zhongAnCollidingConfigMapper.queryCollidingDataByConfigSql(completeSql);
                if (CollectionUtils.isEmpty(collidingDatas)) {
                    flag = false;
                }
                Set<String> custNumSet = collidingDatas.stream().map(ZhongAnCollidingDataBO::getCaseNum).collect(Collectors.toSet());
                Map<String, SyncUserValidityPeriodsBO> keyToSyncUserBO = transferDataValidityPeriodService
                        .getValidityPeriodsByCustNumAndUserType(custNumSet, collidingDatas.get(0).getUserType(), apiCode, bizDate);
                Map<String, ZhongAnCollidingDataBO> map = collidingDatas.stream().collect(Collectors.toMap(ZhongAnCollidingDataBO::getMobileMd5,
                        Function.identity()));
                List<ZhongAnCollidingDataBO> collidingDataBOS = Lists.newArrayList();
                List<Long> nonValidSmsIds = new ArrayList<>();
                List<Long> nonValidCallIds = new ArrayList<>();
                for (Map.Entry<String, ZhongAnCollidingDataBO> entry : map.entrySet()) {
                    String cellMd5 = entry.getKey();
                    ZhongAnCollidingDataBO value = entry.getValue();
                    List<ZhongAnReportHandler> handlers = Lists.newArrayList();
                    handlers.add(sms2DayHandler);
                    boolean result = executor.execute(handlers, cellMd5);
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
                    }
                }
                if(!nonValidSmsIds.isEmpty()) {
                    updateCallStatus(nonValidCallIds, 4);
                }
                if(!nonValidSmsIds.isEmpty()) {
                    updateSmsStatus(nonValidCallIds, 4);
                }
                if(collidingDataBOS.isEmpty()){
                    continue;
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
                    detail.setIsConnect(collidingDataBO.getIsConnect());
                    detail.setIsSmsSend(isSmsSend);
                    detail.setIsSmsSendSuccess(collidingDataBO.getSmsSendStatus());
                    pushList.add(detail);
                    count++;
                    if (pushList.size() == pushSize || size == count) {
                        List<Long> finalPushIds = pushCallIds;
                        List<Long> finalPushSmsIds= pushSmsIds;
                        pushPool.execute(() -> {
                            ZaMarketDataDTO dataDTO = new ZaMarketDataDTO();
                            dataDTO.setData(pushList);
                            methodRetryHandlerService.callZhongAnData(new ZaMarketDataBO(dataDTO
                                    , collidingDataBO.getApiCode(), "MG", finalPushIds, finalPushSmsIds), null);
                        });
                        pushSmsIds = new ArrayList<>();
                        pushCallIds = new ArrayList<>();
                    }
                }
            }
        }
    }

    private void updateSmsStatus(List<Long> nonValidSmsIds, int updateStatus) {
        ZhongAnSmsRosterLockingData data = new ZhongAnSmsRosterLockingData();
        data.setStatus(updateStatus);
        data.setUpdateTime(new Date());
        ZhongAnSmsRosterLockingDataExample example = new ZhongAnSmsRosterLockingDataExample();
        example.createCriteria().andIdIn(nonValidSmsIds);
        zhongAnSmsRosterLockingDataMapper.updateByExampleSelective(data, example);
    }

    private void updateCallStatus(List<Long> nonValidCallIds, int updateStatus) {
        ZhonganRosterLockingData data = new ZhonganRosterLockingData();
        data.setStatus(updateStatus);
        data.setUpdateTime(new Date());
        ZhonganRosterLockingDataExample example = new ZhonganRosterLockingDataExample();
        example.createCriteria().andIdIn(nonValidCallIds);
        zhonganRosterLockingDataMapper.updateByExampleSelective(data, example);
    }
}
